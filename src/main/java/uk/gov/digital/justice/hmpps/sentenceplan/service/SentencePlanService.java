package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
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

@Service
@Slf4j
public class SentencePlanService {
    private SentencePlanRepository sentencePlanRepository;
    private OffenderService offenderService;
    private AssessmentService assessmentService;
    private MotivationRefService motivationRefService;

    public SentencePlanService(SentencePlanRepository sentencePlanRepository, OffenderService offenderService, AssessmentService assessmentService, MotivationRefService motivationRefService) {
        this.sentencePlanRepository = sentencePlanRepository;
        this.offenderService = offenderService;
        this.assessmentService = assessmentService;
        this.motivationRefService = motivationRefService;
    }

    @Transactional
    public SentencePlan createSentencePlan(String offenderId, OffenderReferenceType offenderReferenceType) {
        var offender = offenderService.getOffenderByType(offenderId, offenderReferenceType);

        if(getCurrentSentencePlanForOffender(offender.getUuid()).isPresent()){
            throw new CurrentSentencePlanForOffenderExistsException("Offender already has current sentence plan");
        }

        var sentencePlan = new SentencePlanEntity(offender);
        assessmentService.addLatestAssessmentNeedsToPlan(sentencePlan);
        sentencePlanRepository.save(sentencePlan);
        log.info("Created Sentence Plan {}", sentencePlan.getUuid(), value(EVENT,SENTENCE_PLAN_CREATED));
        return SentencePlan.from(sentencePlan);
    }


    public SentencePlan getSentencePlanFromUuid(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan {}", sentencePlanUuid, value(EVENT,SENTENCE_PLAN_RETRIEVED));
        return SentencePlan.from(getSentencePlanEntity(sentencePlanUuid));
    }

    public Optional<SentencePlanEntity> getCurrentSentencePlanForOffender(UUID offenderUUID) {
        log.info("Retrieving Sentence Plan for offender {}", offenderUUID, value(EVENT,SENTENCE_PLAN_RETRIEVED));
        return Optional.ofNullable(sentencePlanRepository.findByOffenderUuid(offenderUUID));
    }

    @Transactional
    public List<Step> addStep(UUID sentencePlanUUID, StepOwner owner, String ownerOther, String strength, String description, String intervention, List<UUID> needs) {
        var stepEntity = new StepEntity(owner,ownerOther,description,strength, StepStatus.NOT_IN_PROGRESS, needs, intervention);
        var sentencePlan =  sentencePlanRepository.findByUuid(sentencePlanUUID);

        if(sentencePlan.getStatus().equals(PlanStatus.DRAFT) && sentencePlan.getData().getSteps().isEmpty()) {
            sentencePlan.setStatus(PlanStatus.STARTED);
            log.info("Update Sentence Plan {} status to STARTED", sentencePlan.getUuid(), value(EVENT,SENTENCE_PLAN_STARTED));
        }
        sentencePlan.addStep(stepEntity);

        log.info("Created Sentence Plan Step {}", sentencePlan.getUuid(), value(EVENT,SENTENCE_PLAN_STEP_CREATED));
        return Step.from(sentencePlan.getData().getSteps(), sentencePlan.getNeeds());
    }

    @Transactional
    public void updateStep(UUID sentencePlanUuid, UUID stepUuid, StepOwner owner, String ownerOther, String strength, String description, String intervention, List<UUID> needs, StepStatus status) {
        var stepEntity = getStepEntity(sentencePlanUuid, stepUuid);
        stepEntity.updateStep(owner, ownerOther, description, strength, status, needs, intervention);

        log.info("Updated Step {} on Sentence Plan {} Motivations", stepUuid, sentencePlanUuid, value(EVENT, SENTENCE_PLAN_STEP_UPDATED));

    }

    public List<Step> getSentencePlanSteps(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan Steps {}", sentencePlanUuid, value(EVENT,SENTENCE_PLAN_STEPS_RETRIEVED));
        var sentencePlanEntity = getSentencePlanEntity(sentencePlanUuid);
        return Step.from(sentencePlanEntity.getData().getSteps(), sentencePlanEntity.getNeeds());
    }

    public Step getSentencePlanStep(UUID sentencePlanUuid, UUID stepId) {
        log.info("Retrieving Sentence Plan {} Step {}",sentencePlanUuid, stepId, value(EVENT,SENTENCE_PLAN_STEP_RETRIEVED));
        return Step.from(getStepEntity(sentencePlanUuid, stepId));
    }

    public List<Need> getSentencePlanNeeds(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan Needs {}", sentencePlanUuid, value(EVENT,SENTENCE_PLAN_NEEDS_RETRIEVED));
        return Need.from(getSentencePlanEntity(sentencePlanUuid).getNeeds());
    }

    @Transactional
    public void updateMotivations(UUID sentencePlanUuid, Map<UUID, UUID> newMotivations){
        if(newMotivations.size() > 0) {
            var sentencePlan = getSentencePlanEntity(sentencePlanUuid);
            var planNeeds = sentencePlan.getNeeds().stream().collect(Collectors.toMap(NeedEntity::getUuid, need -> need));
            var motivationRefs = motivationRefService.getAllMotivations();
            newMotivations.forEach((key, value) -> planNeeds.computeIfPresent(key, (k, v) -> updateMotivation(v, value, motivationRefs)));
            sentencePlanRepository.save(sentencePlan);
            log.info("Updated Sentence Plan {} Motivations", sentencePlanUuid, value(EVENT, SENTENCE_PLAN_MOTIVATIONS_UPDATED));
        }
    }

    private StepEntity getStepEntity(UUID sentencePlanUuid, UUID stepUuid) {
        return getSentencePlanEntity(sentencePlanUuid).getData().getSteps().stream()
                .filter(s->s.getId().equals(stepUuid)).findAny()
                .orElseThrow(() -> new EntityNotFoundException(String.format("Step %s not found", stepUuid)));
    }

    private SentencePlanEntity getSentencePlanEntity(UUID sentencePlanUuid) {
        return Optional.ofNullable(sentencePlanRepository.findByUuid(sentencePlanUuid))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Sentence Plan %s not found", sentencePlanUuid)));
    }


}
