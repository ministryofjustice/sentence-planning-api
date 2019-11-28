package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.CurrentSentencePlanForOffenderExistsException;

import javax.transaction.Transactional;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;

@Service
@Slf4j
public class SentencePlanService {
    private SentencePlanRepository sentencePlanRepository;
    private OffenderService offenderService;
    private AssessmentService assessmentService;
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    public SentencePlanService(SentencePlanRepository sentencePlanRepository, OffenderService offenderService, AssessmentService assessmentService, OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
        this.sentencePlanRepository = sentencePlanRepository;
        this.offenderService = offenderService;
        this.assessmentService = assessmentService;
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
    }

    @Transactional
    public SentencePlanEntity createSentencePlan(String offenderId, OffenderReferenceType offenderReferenceType) {
        var offender = offenderService.getOffenderByType(offenderId, offenderReferenceType);

        if (getCurrentSentencePlan(offender.getUuid()).isPresent()) {
            throw new CurrentSentencePlanForOffenderExistsException("Offender already has a current sentence plan");
        }

        var sentencePlan = new SentencePlanEntity(offender);
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlan);
        sentencePlanRepository.save(sentencePlan);
        log.info("Created Sentence Plan {}", sentencePlan.getUuid(), value(EVENT, SENTENCE_PLAN_CREATED));
        return sentencePlan;
    }

    public SentencePlanEntity getSentencePlanFromUuid(UUID sentencePlanUuid) {
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        log.info("Retrieved Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        return sentencePlanEntity;
    }

    @Transactional
    public void addObjective(UUID sentencePlanUUID, String description, List<UUID> needs) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        var objectiveEntity = new ObjectiveEntity(description, needs);
        sentencePlanEntity.addObjective(objectiveEntity);
        log.info("Created Objective for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVE_CREATED));
    }

    @Transactional
    public void updateObjective(UUID sentencePlanUUID, UUID objectiveUUID, String description, List<UUID> needs) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        objectiveEntity.updateObjective(description, needs);
        log.info("Updated Objective {} for Sentence Plan {}", objectiveUUID, sentencePlanUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVE_UPDATED));
    }

    public ObjectiveEntity getObjective(UUID sentencePlanUUID, UUID objectiveUUID) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        log.info("Retrieved Objective {} for Sentence Plan {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVE_RETRIEVED));
        return objectiveEntity;
    }

    @Transactional
    public void addAction(UUID sentencePlanUUID, UUID objectiveUUID, UUID interventionUUID, String description, YearMonth targetDate, UUID motivationUUID, List<ActionOwner> owner, String ownerOther, ActionStatus status) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        var actionEntity = new ActionEntity(interventionUUID, description, targetDate, motivationUUID, owner, ownerOther, status);
        objectiveEntity.addAction(actionEntity);
        log.info("Created Action for Sentence Plan {} Objective {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_CREATED));
    }

    @Transactional
    public void updateAction(UUID sentencePlanUUID, UUID objectiveUUID, UUID actionUUID, UUID interventionUUID, String description, YearMonth targetDate, UUID motivationUUID, List<ActionOwner> owner, String ownerOther, ActionStatus status) {
        var actionEntity = getActionEntity(sentencePlanUUID, objectiveUUID, actionUUID);
        actionEntity.updateAction(interventionUUID, description, targetDate, motivationUUID, owner, ownerOther, status);
        log.info("Created Action for Sentence Plan {} Objective {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_UPDATED));
    }

    public ActionEntity getAction(UUID sentencePlanUuid, UUID objectiveUUID, UUID actionId) {
        var actionEntity = getActionEntity(sentencePlanUuid, objectiveUUID, actionId);
        log.info("Retrieved Action {} for Sentence Plan {} Objective {}", sentencePlanUuid, objectiveUUID, actionId, value(EVENT, SENTENCE_PLAN_ACTION_RETRIEVED));
        return actionEntity;
    }

    public List<ActionEntity> getActions(UUID sentencePlanUuid, UUID objectiveUuid) {
        return new ArrayList<>(getObjectiveEntity(sentencePlanUuid, objectiveUuid).getActions().values());
    }

    @Transactional
    public List<NeedEntity> getSentencePlanNeeds(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUUID);
        var needs = sentencePlanEntity.getNeeds();
        log.info("Retrieving Needs for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_NEEDS_RETRIEVED));
        return needs;
    }

    @Transactional
    public void updateObjectivePriorities(UUID sentencePlanUuid,  Map<UUID, Integer> newPriorities) {
        var planObjectives = getSentencePlanEntity(sentencePlanUuid).getObjectives();
        planObjectives.forEach((key, value) -> value.setPriority(Optional.ofNullable(newPriorities.get(value.getId())).orElse(value.getPriority())));
        log.info("Updated Objective priority for Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_OBJECTIVE_PRIORITY_UPDATED));
    }

    @Transactional
    public void updateActionPriorities(UUID sentencePlanUuid, UUID objectiveUUID, Map<UUID, Integer> newPriorities) {
        var planActions = getObjectiveEntity(sentencePlanUuid,objectiveUUID).getActions();
        planActions.forEach((key, value) -> value.setPriority(Optional.ofNullable(newPriorities.get(value.getId())).orElse(value.getPriority())));
        log.info("Updated Action priority for Sentence Plan {} Objective {}", sentencePlanUuid, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_PRIORITY_UPDATED));
    }

    @Transactional
    public void progressAction(UUID sentencePlanUUID, UUID objectiveUUID, UUID actionId, ActionStatus status, YearMonth targetDate, UUID motivationUUID, String comment) {
        var actionEntity = getActionEntity(sentencePlanUUID, objectiveUUID, actionId);
        // TODO: Presumably createdBy comes from the Auth headers?
        var progressEntity = new ProgressEntity(status, targetDate, motivationUUID, comment, "ANONYMOUS");
        actionEntity.addProgress(progressEntity);
        log.info("Progressed Action for Sentence Plan {} Objective {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_PROGRESSED));
    }

    @Transactional
    public void startSentencePlan(UUID sentencePlanUUID) {
        var sentencePlanEntity = sentencePlanRepository.findByUuid(sentencePlanUUID);
        sentencePlanEntity.start();
        log.info("Sentence Plan {} Started", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_STARTED));
    }

    @Transactional
    public void endSentencePlan(UUID sentencePlanUUID) {
        var sentencePlanEntity = sentencePlanRepository.findByUuid(sentencePlanUUID);
        sentencePlanEntity.end();
        log.info("Sentence Plan {} Ended", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_ENDED));
    }

    @Transactional
    public void addSentencePlanComments(UUID sentencePlanUUID, List<AddCommentRequest> comments) {
        var sentencePlanEntity = sentencePlanRepository.findByUuid(sentencePlanUUID);

        // TODO: Presumably createdBy comes from the Auth headers?
        comments.forEach(comment -> sentencePlanEntity.addComment(new CommentEntity(comment.getComment(), comment.getCommentType(), "ANONYMOUS")));
        log.info("Added Comments to Sentence Plan {}", sentencePlanEntity.getUuid(), value(EVENT, SENTENCE_PLAN_COMMENTS_CREATED));
    }

    public Collection<CommentEntity> getSentencePlanComments(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        var comments = sentencePlanEntity.getData().getComments().values();
        log.info("Retrieved Comments for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_COMMENTS_RETRIEVED));
        return comments;
    }

    public List<SentencePlanSummary> getSentencePlansForOffender(Long oasysOffenderId) {
        var offender = offenderService.getOasysOffender(Long.toString(oasysOffenderId)).getUuid();

        var newSentencePlans = sentencePlanRepository.findByOffenderUuid(offender);
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);

        var sentencePlanSummaries = Stream.concat(
                newSentencePlans.stream().map(s -> new SentencePlanSummary(s.getUuid().toString(), s.getCreatedDate(), s.getStartedDate(), false)),
                oasysSentencePlans.stream().map(s -> new SentencePlanSummary(Long.toString(s.getOasysSetId()), s.getCreatedDate(), s.getCompletedDate(), true))
        ).collect(Collectors.toList());

        log.info("Returned {} sentence plans for Offender {}", sentencePlanSummaries.size(), oasysOffenderId, value(EVENT, SENTENCE_PLANS_RETRIEVED));
        return sentencePlanSummaries.stream().sorted(Comparator.comparing(SentencePlanSummary::getCreatedDate).reversed()).collect(Collectors.toList());

    }

    public OasysSentencePlan getLegacySentencePlan(Long oasysOffenderId, String sentencePlanId) {
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);
        return oasysSentencePlans.stream().filter(s -> s.getOasysSetId().equals(Long.valueOf(sentencePlanId))).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("OASys sentence plan does not exist for offender."));
    }

    public SentencePlanEntity getCurrentSentencePlanForOffender(String offenderId) {
        var offender = offenderService.getOasysOffender(offenderId);
        var activeSentencePlan = getCurrentSentencePlan(offender.getUuid());
        if(activeSentencePlan.isPresent()) {
            log.info("Retrieved Sentence Plan {} for offender {}", activeSentencePlan.get().getId(), offender.getUuid(), value(EVENT, SENTENCE_PLAN_RETRIEVED));
            return activeSentencePlan.get();
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

    private ActionEntity getActionEntity(UUID sentencePlanUUID, UUID objectiveUUID, UUID actionUuid) {
        var objective = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        var action = objective.getAction(actionUuid);

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

    private SentencePlanEntity getSentencePlanEntity(UUID sentencePlanUuid) {
        return Optional.ofNullable(sentencePlanRepository.findByUuid(sentencePlanUuid))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Sentence Plan %s not found", sentencePlanUuid)));
    }


    public Collection<ObjectiveEntity> getSentencePlanObjectives(UUID sentencePlanUUID) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUUID);
        var objectives = sentencePlanEntity.getData().getObjectives().values();
        log.info("Retrieved Objectives for Sentence Plan {}", sentencePlanUUID, value(EVENT, SENTENCE_PLAN_OBJECTIVES_RETRIEVED));
        return objectives;
    }
}
