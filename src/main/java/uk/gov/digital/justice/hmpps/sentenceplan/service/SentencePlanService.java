package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlanDto;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.BusinessRuleViolationException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.CurrentSentencePlanForOffenderExistsException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;

@Service
@Slf4j
public class SentencePlanService {
    private final SentencePlanRepository sentencePlanRepository;
    private final OffenderService offenderService;
    private final AssessmentService assessmentService;
    private final TimelineService timelineService;
    private final OASYSAssessmentAPIClient oasysAssessmentAPIClient;
    private final RequestData requestData;

    public SentencePlanService(SentencePlanRepository sentencePlanRepository, OffenderService offenderService, AssessmentService assessmentService, TimelineService timelineService, OASYSAssessmentAPIClient oasysAssessmentAPIClient, RequestData requestData) {
        this.sentencePlanRepository = sentencePlanRepository;
        this.offenderService = offenderService;
        this.assessmentService = assessmentService;
        this.timelineService = timelineService;
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
        this.requestData = requestData;
    }

    @Transactional
    public SentencePlanDto createSentencePlan(Long offenderId) {
        var offender = offenderService.getOffenderByType(offenderId);
        if (getCurrentSentencePlan(offender.getUuid()).isPresent()) {
            throw new CurrentSentencePlanForOffenderExistsException("Offender already has a current sentence plan");
        }
        var sentencePlanEntity = new SentencePlanEntity(offender);
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlanEntity);
        sentencePlanRepository.save(sentencePlanEntity);
        timelineService.createTimelineEntry(sentencePlanEntity.getUuid(), SENTENCE_PLAN_CREATED);
        log.info("Created Sentence Plan {}", sentencePlanEntity.getUuid(), value(EVENT, SENTENCE_PLAN_CREATED));
        return SentencePlanDto.from(sentencePlanEntity);
    }

    public SentencePlanDto getSentencePlanFromUuid(UUID sentencePlanUuid) {
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        log.info("Retrieved Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        return SentencePlanDto.from(sentencePlanEntity);
    }

    @Transactional
    public ObjectiveDto addObjective(UUID sentencePlanUUID, AddSentencePlanObjectiveRequest objectiveRequest) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        var objectiveEntity = new ObjectiveEntity(objectiveRequest.getDescription(), objectiveRequest.getNeeds(), objectiveRequest.isMeetsChildSafeguarding());
        sentencePlanEntity.addObjective(objectiveEntity);
        timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_OBJECTIVE_CREATED, objectiveEntity);
        log.info("Created Objective for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVE_CREATED));
        return ObjectiveDto.from(objectiveEntity);
    }

    @Transactional
    public void updateObjective(UUID sentencePlanUUID, UUID objectiveUUID, AddSentencePlanObjectiveRequest objectiveRequest) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        objectiveEntity.updateObjective(objectiveRequest.getDescription(), objectiveRequest.getNeeds(), objectiveRequest.isMeetsChildSafeguarding());
        timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_OBJECTIVE_UPDATED, objectiveEntity);
        log.info("Updated Objective {} for Sentence Plan {}", objectiveUUID, sentencePlanUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVE_UPDATED));
    }

    public ObjectiveDto getObjective(UUID sentencePlanUUID, UUID objectiveUUID) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        log.info("Retrieved Objective {} for Sentence Plan {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVE_RETRIEVED));
        return ObjectiveDto.from(objectiveEntity);
    }

    @Transactional
    public void addAction(UUID sentencePlanUUID, UUID objectiveUUID, AddSentencePlanActionRequest actionRequest) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        var actionEntity = new ActionEntity(actionRequest.getInterventionUUID(), actionRequest.getDescription(), actionRequest.getTargetDate(), actionRequest.getMotivationUUID(), actionRequest.getOwner(), actionRequest.getOwnerOther(), actionRequest.getStatus());
        objectiveEntity.addAction(actionEntity);
        timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_ACTION_CREATED, objectiveEntity);
        log.info("Created Action for Sentence Plan {} Objective {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_CREATED));
    }

    @Transactional
    public void updateAction(UUID sentencePlanUUID, UUID objectiveUUID, UUID actionUUID, AddSentencePlanActionRequest actionRequest) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        if(!sentencePlanEntity.isDraft()){
          throw new BusinessRuleViolationException("Cannot update Action, Sentence Plan is not a draft");
        }
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        var actionEntity = getActionEntity(objectiveEntity, actionUUID);
        actionEntity.updateAction(actionRequest.getInterventionUUID(), actionRequest.getDescription(), actionRequest.getTargetDate(), actionRequest.getMotivationUUID(), actionRequest.getOwner(), actionRequest.getOwnerOther(), actionRequest.getStatus());
        timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_ACTION_UPDATED, objectiveEntity);
        log.info("Updated Action {} for Sentence Plan {} Objective {}", actionUUID, sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_UPDATED));
    }

    public ActionDto getAction(UUID sentencePlanUUID, UUID objectiveUUID, UUID actionId) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        var actionEntity = getActionEntity(objectiveEntity, actionId);
        log.info("Retrieved Action {} for Sentence Plan {} Objective {}", sentencePlanUUID, objectiveUUID, actionId, value(EVENT, SENTENCE_PLAN_ACTION_RETRIEVED));
        return ActionDto.from(actionEntity);
    }

    @Transactional
    public Collection<NeedDto> getSentencePlanNeeds(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUUID);
        var needs = sentencePlanEntity.getNeeds();
        log.info("Retrieving Needs for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_NEEDS_RETRIEVED));
        return NeedDto.from(needs);
    }

    @Transactional
    public void updateObjectivePriorities(UUID sentencePlanUuid,  List<UpdateObjectivePriorityRequest> request) {
        var objectivePriorities = request.stream().collect(Collectors.toMap(UpdateObjectivePriorityRequest::getObjectiveUUID, UpdateObjectivePriorityRequest::getPriority));
        var planObjectives = getSentencePlanEntity(sentencePlanUuid).getObjectives();
        planObjectives.forEach((key, value) -> value.setPriority(Optional.ofNullable(objectivePriorities.get(value.getId())).orElse(value.getPriority())));
        log.info("Updated Objective priority for Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_OBJECTIVE_PRIORITY_UPDATED));
    }

    @Transactional
    public void updateActionPriorities(UUID sentencePlanUuid, UUID objectiveUUID, List<UpdateActionPriorityRequest> request) {
        var actionPriorities = request.stream().collect(Collectors.toMap(UpdateActionPriorityRequest::getActionUUID, UpdateActionPriorityRequest::getPriority));
        var planActions = getObjectiveEntity(sentencePlanUuid,objectiveUUID).getActions();
        planActions.forEach((key, value) -> value.setPriority(Optional.ofNullable(actionPriorities.get(value.getId())).orElse(value.getPriority())));
        log.info("Updated Action priority for Sentence Plan {} Objective {}", sentencePlanUuid, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_PRIORITY_UPDATED));
    }

    @Transactional
    public void progressAction(UUID sentencePlanUUID, UUID objectiveUUID, UUID actionId, ProgressActionRequest request) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        var actionEntity = getActionEntity(objectiveEntity, actionId);
        var progressEntity = new ProgressEntity(request.getStatus(), request.getTargetDate(), request.getMotivationUUID(), request.getComment(), request.getOwner(), request.getOwnerOther(), requestData.getUsername());
        actionEntity.addProgress(progressEntity);
        timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_ACTION_CREATED, objectiveEntity);
        log.info("Progressed Action for Sentence Plan {} Objective {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_PROGRESSED));
    }

    @Transactional
    public void startSentencePlan(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        sentencePlanEntity.start();
        timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_STARTED);
        log.info("Sentence Plan {} Started", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_STARTED));
    }

    @Transactional
    public void endSentencePlan(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        sentencePlanEntity.end();
        timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_ENDED);
        log.info("Sentence Plan {} Ended", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_ENDED));
    }

    @Transactional
    public void addSentencePlanComments(UUID sentencePlanUUID, List<AddCommentRequest> comments) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        for(AddCommentRequest comment : comments) {
            var commentEntity = new CommentEntity(comment.getComment(), comment.getCommentType(), requestData.getUsername());
            sentencePlanEntity.addComment(commentEntity);
            timelineService.createTimelineEntry(sentencePlanUUID, SENTENCE_PLAN_COMMENTS_CREATED, commentEntity);
        }
        log.info("Added Comments to Sentence Plan {}", sentencePlanEntity.getUuid(), value(EVENT, SENTENCE_PLAN_COMMENTS_CREATED));
    }

    public Collection<CommentDto> getSentencePlanComments(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        var comments = sentencePlanEntity.getData().getComments().values();
        log.info("Retrieved Comments for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_COMMENTS_RETRIEVED));
        return CommentDto.from(comments);
    }

    public List<SentencePlanSummaryDto> getSentencePlansForOffender(Long oasysOffenderId) {
        var offender = offenderService.getOasysOffender(oasysOffenderId).getUuid();

        var newSentencePlans = sentencePlanRepository.findByOffenderUuid(offender);
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);

        var sentencePlanSummaries = Stream.concat(
                newSentencePlans.stream().map(s -> new SentencePlanSummaryDto(s.getUuid().toString(), s.getCreatedOn(), s.getCompletedDate(), false, s.isDraft())),
                oasysSentencePlans.stream().map(s -> new SentencePlanSummaryDto(Long.toString(s.getOasysSetId()), s.getCreatedDate(), s.getCompletedDate(), true, false))
        ).collect(Collectors.toList());

        log.info("Returned {} sentence plans for Offender {}", sentencePlanSummaries.size(), oasysOffenderId, value(EVENT, SENTENCE_PLANS_RETRIEVED));
        return sentencePlanSummaries.stream().sorted(Comparator.comparing(SentencePlanSummaryDto::getCreatedDate).reversed()).collect(Collectors.toList());

    }

    public OasysSentencePlanDto getLegacySentencePlan(Long oasysOffenderId, String sentencePlanId) {
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);
        return oasysSentencePlans.stream().filter(s -> s.getOasysSetId().equals(Long.valueOf(sentencePlanId))).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("OASys sentence plan does not exist for offender."));
    }

    public SentencePlanDto getCurrentSentencePlanForOffender(Long offenderId) {
        var offender = offenderService.getOasysOffender(offenderId);
        var activeSentencePlan = getCurrentSentencePlan(offender.getUuid());
        if(activeSentencePlan.isPresent()) {
            log.info("Retrieved Sentence Plan {} for offender {}", activeSentencePlan.get().getId(), offender.getUuid(), value(EVENT, SENTENCE_PLAN_RETRIEVED));
            return SentencePlanDto.from(activeSentencePlan.get());
        } else {
            throw new EntityNotFoundException("No current sentence plan for offender");
        }
    }

    private Optional<SentencePlanEntity> getCurrentSentencePlan(UUID offenderUUID) {
        var sentencePlans = sentencePlanRepository.findByOffenderUuid(offenderUUID);
        return sentencePlans.stream().filter(s -> s.getCompletedDate() == null).findFirst();
    }

    private ObjectiveEntity getObjectiveEntity(UUID sentencePlanUUID, UUID objectiveUUID) {
        var sentencePlan = getSentencePlanEntity(sentencePlanUUID);
        var objective = sentencePlan.getObjective(objectiveUUID);

        if(objective == null) {
            throw new EntityNotFoundException("Objective not found!");
        }
        return objective;
    }

    private ActionEntity getActionEntity(ObjectiveEntity objectiveEntity, UUID actionUuid) {
        var action = objectiveEntity.getAction(actionUuid);

        if(action == null) {
            throw new EntityNotFoundException("Action not found!");
        }
        return action;
    }

    private SentencePlanEntity getSentencePlanEntityWithUpdatedNeeds(UUID sentencePlanUuid) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlanEntity);
        offenderService.updateOasysOffender(sentencePlanEntity);
        return sentencePlanEntity;
    }

    public SentencePlanEntity getSentencePlanEntity(UUID sentencePlanUuid) {
        return Optional.ofNullable(sentencePlanRepository.findByUuid(sentencePlanUuid))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Sentence Plan %s not found", sentencePlanUuid)));
    }

    public Collection<ObjectiveDto> getSentencePlanObjectives(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        var objectives = sentencePlanEntity.getData().getObjectives().values();
        log.info("Retrieved Objectives for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVES_RETRIEVED));
        return ObjectiveDto.from(objectives);
    }

    public List<Revision<Integer, SentencePlanEntity>> getSentencePlanRevisions(UUID sentencePlanUuid) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        var revisions = sentencePlanRepository.findRevisions(sentencePlanEntity.getId());
        return revisions.getContent();
    }

    @Transactional
    public void closeObjective(UUID sentencePlanUuid, UUID objectiveUUID) {
        var objective = getObjectiveEntity(sentencePlanUuid,objectiveUUID);
        objective.getActions().forEach((key, action) -> action.abandon());
        log.info("Closed objective {} for Sentence Plan {}", objectiveUUID, sentencePlanUuid, value(EVENT, SENTENCE_PLAN_OBJECTIVE_CLOSED));
        timelineService.createTimelineEntry(sentencePlanUuid, SENTENCE_PLAN_OBJECTIVE_CLOSED, objective);
    }
}
