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
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.NoOffenderAssessmentException;

import javax.transaction.Transactional;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;

@Service
@Slf4j
public class SentencePlanService {
    private SentencePlanRepository sentencePlanRepository;
    private OffenderService offenderService;
    private AssessmentService assessmentService;
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    public SentencePlanService(SentencePlanRepository sentencePlanRepository, OffenderService offenderService, AssessmentService assessmentService, MotivationRefService motivationRefService, OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
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
        log.info("Retrieving Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        return SentencePlan.from(sentencePlanEntity);
    }

    @Transactional
    public void addAction(UUID sentencePlanUUID, UUID interventionUUID, String description, YearMonth targetDate, UUID motivationUUID, List<ActionOwner> owner, String ownerOther, ActionStatus status) {
        var sentencePlan = getSentencePlanEntity(sentencePlanUUID);

        var actionEntity = new ActionEntity(interventionUUID, description, targetDate, motivationUUID, owner, ownerOther, status);

        sentencePlan.addAction(actionEntity);

        log.info("Created Sentence Plan Action {}", sentencePlan.getUuid(), value(EVENT, SENTENCE_PLAN_ACTION_CREATED));
    }

    public List<Action> getSentencePlanActions(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan Actions {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_ACTIONS_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        return Action.from(sentencePlanEntity.getData().getActions(), sentencePlanEntity.getNeeds());
    }

    public Action getSentencePlanAction(UUID sentencePlanUuid, UUID actionId) {
        log.info("Retrieving Sentence Plan {} Action {}", sentencePlanUuid, actionId, value(EVENT, SENTENCE_PLAN_ACTION_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        return Action.from(getActionEntity(sentencePlanEntity, actionId), sentencePlanEntity.getNeeds());
    }

    @Transactional
    public List<Need> getSentencePlanNeeds(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan Needs {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_NEEDS_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        return Need.from(sentencePlanEntity.getNeeds());
    }

    @Transactional
    public void updateActionPriorities(UUID sentencePlanUuid, Map<UUID, Integer> newPriorities) {
        if (newPriorities.size() > 0) {

            var sentencePlan = getSentencePlanEntity(sentencePlanUuid);
            var planActions = sentencePlan.getData().getActions().stream().collect(Collectors.toMap(ActionEntity::getId, action -> action));

            planActions.forEach((key, value) -> value.setPriority(newPriorities.get(value.getId())));

            sentencePlanRepository.save(sentencePlan);
            log.info("Updated Sentence Plan {} Action priority", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_ACTION_PRIORITY_UPDATED));
        }
    }

    @Transactional
    public void progressAction(UUID sentencePlanUuid, UUID actionId, ActionStatus status, UUID motivationUUID) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        var actionEntity = getActionEntity(sentencePlanEntity, actionId);

        // TODO: Presumably createdBy comes from the Auth headers?
        var progressEntity = new ProgressEntity(status, motivationUUID,"ANONYMOUS");
        actionEntity.addProgress(progressEntity);
        sentencePlanRepository.save(sentencePlanEntity);

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
        var offenderUuid = offenderService.getOffenderByType(Long.toString(oasysOffenderId), OffenderReferenceType.OASYS).getUuid();
        var newSentencePlans = sentencePlanRepository.findByOffenderUuid(offenderUuid);

        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);

        var sentencePlanSummaries = new ArrayList<SentencePlanSummary>();

        newSentencePlans.stream().forEach(s ->
                sentencePlanSummaries.add(new SentencePlanSummary(s.getUuid().toString(), s.getCreatedOn(), s.getEndDate(), false))
        );

        oasysSentencePlans.stream().forEach(s ->
                sentencePlanSummaries.add(new SentencePlanSummary(Long.toString(s.getOasysSetId()), s.getCreatedDate(), s.getCompletedDate(), true))
        );
        log.info("Returning {} sentence plans for Offender {}", sentencePlanSummaries.size(), oasysOffenderId, value(EVENT, SENTENCE_PLANS_RETRIEVED));
        return sentencePlanSummaries.stream().sorted(Comparator.comparing(SentencePlanSummary::getCreatedDate).reversed()).collect(Collectors.toList());

    }

    private Optional<SentencePlanEntity> getCurrentSentencePlanForOffender(UUID offenderUUID) {
        log.info("Retrieving Sentence Plan for offender {}", offenderUUID, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        var sentencePlans = sentencePlanRepository.findByOffenderUuid(offenderUUID);
        return sentencePlans.stream().filter(s -> s.getEndDate() == null).findFirst();
    }

    public OasysSentencePlan getLegacySentencePlan(Long oasysOffenderId, String sentencePlanId) {
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);
        return oasysSentencePlans.stream().filter(s -> s.getOasysSetId().equals(Long.valueOf(sentencePlanId))).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("OASys sentence plan does not exist for offender."));
    }

    private ActionEntity getActionEntity(SentencePlanEntity sentencePlanEntity, UUID actionUuid) {
        return sentencePlanEntity.getData().getActions().stream()
                .filter(s -> s.getId().equals(actionUuid)).findAny()
                .orElseThrow(() -> new EntityNotFoundException(String.format("Action %s not found", actionUuid)));
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
