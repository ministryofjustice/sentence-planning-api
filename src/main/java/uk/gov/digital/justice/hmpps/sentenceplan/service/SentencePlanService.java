package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.application.ValidationException;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.CurrentSentencePlanForOffenderExistsException;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity.updateMotivation;
import static uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.StepEntity.updatePriority;

@Service
@Slf4j
public class SentencePlanService {
    private SentencePlanRepository sentencePlanRepository;
    private OffenderService offenderService;
    private AssessmentService assessmentService;
    private MotivationRefService motivationRefService;
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    public SentencePlanService(SentencePlanRepository sentencePlanRepository, OffenderService offenderService, AssessmentService assessmentService, MotivationRefService motivationRefService, OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
        this.sentencePlanRepository = sentencePlanRepository;
        this.offenderService = offenderService;
        this.assessmentService = assessmentService;
        this.motivationRefService = motivationRefService;
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
    }

    @Transactional
    public SentencePlan createSentencePlan(String offenderId, OffenderReferenceType offenderReferenceType) {
        var offender = offenderService.getOffenderByType(offenderId, offenderReferenceType);

        if (getCurrentSentencePlanForOffender(offender.getUuid()).isPresent()) {
            throw new CurrentSentencePlanForOffenderExistsException("Offender already has current sentence plan");
        }

        var sentencePlan = new SentencePlanEntity(offender);
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlan);
        sentencePlanRepository.save(sentencePlan);
        log.info("Created Sentence Plan {}", sentencePlan.getUuid(), value(EVENT, SENTENCE_PLAN_CREATED));
        return SentencePlan.from(sentencePlan);
    }

    @Transactional
    public SentencePlan getSentencePlanFromUuid(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        return SentencePlan.from(sentencePlanEntity);
    }

    private Optional<SentencePlanEntity> getCurrentSentencePlanForOffender(UUID offenderUUID) {
        log.info("Retrieving Sentence Plan for offender {}", offenderUUID, value(EVENT, SENTENCE_PLAN_RETRIEVED));
        var sentencePlans = sentencePlanRepository.findByOffenderUuid(offenderUUID);
        return sentencePlans.stream().filter(s -> s.getEndDate() == null).findFirst();
    }

    @Transactional
    public List<Step> addStep(UUID sentencePlanUUID, StepOwner owner, String ownerOther, String strength, String description, String intervention, List<UUID> needs) {
        var stepEntity = new StepEntity(owner, ownerOther, description, strength, StepStatus.IN_PROGRESS, needs, intervention);
        var sentencePlan = getSentencePlanEntity(sentencePlanUUID);

        if (sentencePlan.getStatus().equals(PlanStatus.DRAFT) && sentencePlan.getData().getSteps().isEmpty()) {
            sentencePlan.setStatus(PlanStatus.STARTED);
            log.info("Update Sentence Plan {} status to STARTED", sentencePlan.getUuid(), value(EVENT, SENTENCE_PLAN_STARTED));
        }

        // Set the priority to lowest
        var steps = sentencePlan.getData().getSteps();
        stepEntity.setPriority(steps.size());
        // Map to a set to get a unique set of values
        var uniqueValues = steps.stream().map(StepEntity::getPriority).collect(Collectors.toSet());
        if (uniqueValues.size() < steps.size()) {
            throw new ValidationException("Steps with duplicate priority found.");
        }

        sentencePlan.addStep(stepEntity);

        log.info("Created Sentence Plan Step {}", sentencePlan.getUuid(), value(EVENT, SENTENCE_PLAN_STEP_CREATED));
        return Step.from(sentencePlan.getData().getSteps(), sentencePlan.getNeeds());
    }

    @Transactional
    public void updateStep(UUID sentencePlanUuid, UUID stepUuid, StepOwner owner, String ownerOther, String strength, String description, String intervention, List<UUID> needs, StepStatus status) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        var stepEntity = getStepEntity(sentencePlanEntity, stepUuid);
        stepEntity.updateStep(owner, ownerOther, description, strength, status, needs, intervention);
        sentencePlanRepository.save(sentencePlanEntity);
        log.info("Updated Step {} on Sentence Plan {} Motivations", stepUuid, sentencePlanUuid, value(EVENT, SENTENCE_PLAN_STEP_UPDATED));

    }

    public List<Step> getSentencePlanSteps(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan Steps {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_STEPS_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        return Step.from(sentencePlanEntity.getData().getSteps(), sentencePlanEntity.getNeeds());
    }

    public Step getSentencePlanStep(UUID sentencePlanUuid, UUID stepId) {
        log.info("Retrieving Sentence Plan {} Step {}", sentencePlanUuid, stepId, value(EVENT, SENTENCE_PLAN_STEP_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        return Step.from(getStepEntity(sentencePlanEntity, stepId), sentencePlanEntity.getNeeds());
    }

    @Transactional
    public List<Need> getSentencePlanNeeds(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan Needs {}", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_NEEDS_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntityWithUpdatedNeeds(sentencePlanUuid);
        return Need.from(sentencePlanEntity.getNeeds());
    }

    @Transactional
    public void updateMotivations(UUID sentencePlanUuid, Map<UUID, UUID> newMotivations) {
        if (newMotivations.size() > 0) {
            var sentencePlan = getSentencePlanEntity(sentencePlanUuid);
            var planNeeds = sentencePlan.getNeeds().stream().collect(Collectors.toMap(NeedEntity::getUuid, need -> need));
            var motivationRefs = motivationRefService.getAllMotivations();
            newMotivations.forEach((key, value) -> planNeeds.computeIfPresent(key, (k, v) -> updateMotivation(v, value, motivationRefs)));
            sentencePlanRepository.save(sentencePlan);
            log.info("Updated Sentence Plan {} Motivations", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_MOTIVATIONS_UPDATED));
        }
    }

    @Transactional
    public void updateStepPriorities(UUID sentencePlanUuid, Map<UUID, Integer> priorities) {
        if (priorities.size() > 0) {

            // Map to a set to get a unique set of values
            Set<Integer> uniqueValues = new HashSet<>(priorities.values());
            if (uniqueValues.size() < priorities.size()) {
                throw new ValidationException("Steps with duplicate priority found.");
            }

            var sentencePlan = getSentencePlanEntity(sentencePlanUuid);
            var planSteps = sentencePlan.getData().getSteps().stream().collect(Collectors.toMap(StepEntity::getId, step -> step));

            // We also need to check that we're updating all the steps otherwise they will get out of step (no pun intended).
            if (planSteps.size() != priorities.size()) {
                throw new ValidationException("Need to update the priority for all steps.");
            }

            priorities.forEach((key, value) -> planSteps.computeIfPresent(key, (k, v) -> updatePriority(v, value)));
            sentencePlanRepository.save(sentencePlan);
            log.info("Updated Sentence Plan {} Step priority", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_STEP_PRIORITY_UPDATED));
        }
    }

    @Transactional
    public void progressStep(UUID sentencePlanUuid, UUID stepId, StepStatus status, String practitionerComments) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        var stepEntity = getStepEntity(sentencePlanEntity, stepId);
        // TODO: Presumably createdBy comes from the Auth headers?
        var progressEntity = new ProgressEntity(status, practitionerComments, "ANONYMOUS");
        stepEntity.addProgress(progressEntity);
        sentencePlanRepository.save(sentencePlanEntity);

    }

    @Transactional
    public void setServiceUserComments(UUID sentencePlanUuid, String serviceUserComments) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        sentencePlanEntity.getData().setServiceUserComments(serviceUserComments);
        sentencePlanRepository.save(sentencePlanEntity);

    }

    @Transactional
    public void addSentencePlanComments(UUID sentencePlanUUID, List<AddCommentRequest> comments) {
        if (comments.size() > 0) {
            var sentencePlanEntity = sentencePlanRepository.findByUuid(sentencePlanUUID);

            // TODO: Presumably createdBy comes from the Auth headers?
            comments.forEach(comment -> sentencePlanEntity.addComment(new CommentEntity(comment.getComments(), comment.getOwner(), "ANONYMOUS")));

            log.info("Added Comments {}", sentencePlanEntity.getUuid(), value(EVENT, SENTENCE_PLAN_COMMENTS_CREATED));
            sentencePlanRepository.save(sentencePlanEntity);
        }
    }

    public List<Comment> getSentencePlanComments(UUID sentencePlanUuid) {
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

    public OasysSentencePlan getLegacySentencePlan(Long oasysOffenderId, String sentencePlanId) {
        var oasysSentencePlans = oasysAssessmentAPIClient.getSentencePlansForOffender(oasysOffenderId);
        return oasysSentencePlans.stream().filter(s -> s.getOasysSetId().equals(Long.valueOf(sentencePlanId))).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("OASys sentence plan does not exist for offender."));
    }

    private StepEntity getStepEntity(SentencePlanEntity sentencePlanEntity, UUID stepUuid) {
        return sentencePlanEntity.getData().getSteps().stream()
                .filter(s -> s.getId().equals(stepUuid)).findAny()
                .orElseThrow(() -> new EntityNotFoundException(String.format("Step %s not found", stepUuid)));
    }

    private SentencePlanEntity getSentencePlanEntityWithUpdatedNeeds(UUID sentencePlanUuid) {
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlanEntity);
        return sentencePlanEntity;
    }

    private SentencePlanEntity getSentencePlanEntity(UUID sentencePlanUuid) {
        return Optional.ofNullable(sentencePlanRepository.findByUuid(sentencePlanUuid))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Sentence Plan %s not found", sentencePlanUuid)));
    }

}
