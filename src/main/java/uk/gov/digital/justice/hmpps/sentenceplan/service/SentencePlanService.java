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
    public UUID createSentencePlan(String offenderId, OffenderReferenceType offenderReferenceType) {
        var offender = offenderService.getOffenderByType(offenderId, offenderReferenceType);

        if (getCurrentSentencePlanForOffender(offender.getUuid()).isPresent()) {
            throw new CurrentSentencePlanForOffenderExistsException("Offender already has current sentence plan");
        }

        var sentencePlan = new SentencePlanEntity(offender);
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlan);
        sentencePlanRepository.save(sentencePlan);
        log.info("Created Sentence Plan {}", sentencePlan.getUuid(), value(EVENT, SENTENCE_PLAN_CREATED));
        return sentencePlan.getUuid();
    }

    public SentencePlan getSentencePlanFromUuid(UUID sentencePlanUuid) {
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        log.info("Retrieved Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        return SentencePlan.from(sentencePlanEntity);
    }

    @Transactional
    public void addAction(UUID sentencePlanUUID, UUID objectiveUUID, UUID interventionUUID, String description, YearMonth targetDate, UUID motivationUUID, List<ActionOwner> owner, String ownerOther, ActionStatus status) {
        var objectiveEntity = getObjectiveEntity(sentencePlanUUID, objectiveUUID);
        var actionEntity = new ActionEntity(interventionUUID, description, targetDate, motivationUUID, owner, ownerOther, status);
        objectiveEntity.addAction(actionEntity);
        log.info("Created Action for Sentence Plan {} Objective {}", sentencePlanUUID, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_CREATED));
    }

    public Action getSentencePlanAction(UUID sentencePlanUuid, UUID objectiveUUID, UUID actionId) {
        var actionEntity = getActionEntity(sentencePlanUuid, objectiveUUID, actionId);
        log.info("Retrieved Action {} for Sentence Plan {} Objective {}", sentencePlanUuid, objectiveUUID, actionId, value(EVENT, SENTENCE_PLAN_ACTION_RETRIEVED));
        return Action.from(actionEntity);
    }

    @Transactional
    public List<Need> getSentencePlanNeeds(UUID sentencePlanUuid) {
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        var needs = Need.from(sentencePlanEntity.getNeeds());
        log.info("Retrieving Needs for Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_NEEDS_RETRIEVED));
        return needs;
    }

    @Transactional
    public void updateActionPriorities(UUID sentencePlanUuid, UUID objectiveUUID, Map<UUID, Integer> newPriorities) {
        var planActions = getObjectiveEntity(sentencePlanUuid,objectiveUUID).getActions();
        planActions.forEach((key, value) -> value.setPriority(newPriorities.get(value.getId())));
        log.info("Updated Action priority for Sentence Plan {} Objective {}", sentencePlanUuid, objectiveUUID, value(EVENT, SENTENCE_PLAN_ACTION_PRIORITY_UPDATED));
    }

    @Transactional
    public void progressAction(UUID sentencePlanUuid, UUID objectiveUUID, UUID actionId, ActionStatus status) {
        var actionEntity = getActionEntity(sentencePlanUuid, objectiveUUID, actionId);
        // TODO: Presumably createdBy comes from the Auth headers?
        var progressEntity = new ProgressEntity(status,"ANONYMOUS");
        actionEntity.addProgress(progressEntity);
    }

    @Transactional
    public void addSentencePlanComments(UUID sentencePlanUUID, List<AddCommentRequest> comments) {
        if (!comments.isEmpty()) {
            var sentencePlanEntity = sentencePlanRepository.findByUuid(sentencePlanUUID);

            // TODO: Presumably createdBy comes from the Auth headers?
            comments.forEach(comment -> sentencePlanEntity.addComment(new CommentEntity(comment.getComment(), comment.getCommentType(), "ANONYMOUS")));

            log.info("Added Comments {}", sentencePlanEntity.getUuid(), value(EVENT, SENTENCE_PLAN_COMMENTS_CREATED));
            sentencePlanRepository.save(sentencePlanEntity);
        }
    }

    public Map<CommentType, Comment> getSentencePlanComments(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan Comments {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_COMMENTS_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        return Comment.from(sentencePlanEntity.getData().getComments());
    }

    public List<SentencePlanSummary> getSentencePlansForOffender(Long oasysOffenderId) {
        var offender = offenderService.getOffenderByType(Long.toString(oasysOffenderId), OffenderReferenceType.OASYS).getUuid();

        var newSentencePlans = sentencePlanRepository.findByOffenderUuid(offender);
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);

        var sentencePlanSummaries = Stream.concat(
                newSentencePlans.stream().map(s -> new SentencePlanSummary(s.getUuid().toString(), s.getCreatedOn(), s.getCompletedDate(), false)),
                oasysSentencePlans.stream().map(s -> new SentencePlanSummary(Long.toString(s.getOasysSetId()), s.getCreatedDate(), s.getCompletedDate(), true))
        ).collect(Collectors.toList());

        log.info("Returning {} sentence plans for Offender {}", sentencePlanSummaries.size(), oasysOffenderId, value(EVENT, SENTENCE_PLANS_RETRIEVED));
        return sentencePlanSummaries.stream().sorted(Comparator.comparing(SentencePlanSummary::getCreatedDate).reversed()).collect(Collectors.toList());

    }

    private Optional<SentencePlanEntity> getCurrentSentencePlanForOffender(UUID offenderUUID) {
        log.info("Retrieving Sentence Plan for offender {}", offenderUUID, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        var sentencePlans = sentencePlanRepository.findByOffenderUuid(offenderUUID);
        return sentencePlans.stream().filter(s -> s.getCompletedDate() == null).findFirst();
    }

    public OasysSentencePlan getLegacySentencePlan(Long oasysOffenderId, String sentencePlanId) {
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);
        return oasysSentencePlans.stream().filter(s -> s.getOasysSetId().equals(Long.valueOf(sentencePlanId))).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("OASys sentence plan does not exist for offender."));
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

}
