package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.SentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.application.ApplicationExceptions;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.AssessmentEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.*;

@Service
@Slf4j
public class SentencePlanService {
    private SentencePlanRepository sentencePlanRepository;
    private OffenderService offenderService;
    private AssessmentService assessmentService;

    public SentencePlanService(SentencePlanRepository sentencePlanRepository, OffenderService offenderService, AssessmentService assessmentService) {
        this.sentencePlanRepository = sentencePlanRepository;
        this.offenderService = offenderService;
        this.assessmentService = assessmentService;
    }


    @Transactional
    public SentencePlan createSentencePlan(String offenderId, OffenderReferenceType offenderReferenceType) {
        var offender = offenderService.getOffenderByType(offenderId, offenderReferenceType);
        var assessment = assessmentService.getLatestAssessmentForOffender(offender);
        var sentencePlan = new SentencePlanEntity(offender, assessment);
        sentencePlanRepository.save(sentencePlan);
        log.info("Created Sentence Plan {}", sentencePlan.getUuid(), value(EVENT,SENTENCE_PLAN_CREATED));
        return SentencePlan.from(sentencePlan);
    }


    public SentencePlan getSentencePlanFromUuid(UUID sentencePlanUuid) {
        log.info("Retrieving Sentence Plan {}", sentencePlanUuid, value(EVENT,SENTENCE_PLAN_RETRIEVED));
        return SentencePlan.from(Optional.ofNullable(sentencePlanRepository.findByUuid(sentencePlanUuid))
                .orElseThrow(() -> new ApplicationExceptions.EntityNotFoundException(String.format("Sentence Plan %s not found", sentencePlanUuid))));
    }




}
