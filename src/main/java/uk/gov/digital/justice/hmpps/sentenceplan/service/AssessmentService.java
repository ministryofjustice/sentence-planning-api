package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.NoOffenderAssessmentException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AssessmentService {
    private static final int UPDATE_INTERVAL_MINUTES = 10;
    private final OASYSAssessmentAPIClient oasysAssessmentAPIClient;
    private final Clock clock;

    public AssessmentService(OASYSAssessmentAPIClient oasysAssessmentAPIClient, Clock clock) {
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
        this.clock = clock;
    }

    public void addLatestAssessmentNeedsToPlan(SentencePlanEntity sentencePlanEntity) {

        if(sentencePlanEntity.getAssessmentNeedsLastImportedOn() == null || sentencePlanEntity.getAssessmentNeedsLastImportedOn().isBefore(LocalDateTime.now(clock).minusMinutes(UPDATE_INTERVAL_MINUTES))) {
            log.info("Adding new assessment needs to sentence plan {}", sentencePlanEntity.getUuid());
            var oasysAssessment = oasysAssessmentAPIClient.getLatestLayer3AssessmentForOffender(
                    sentencePlanEntity.getOffender().getOasysOffenderId())
                    .orElseThrow(NoOffenderAssessmentException::new);

            sentencePlanEntity.updateNeeds(getNeedsFromOasysAssessment(oasysAssessment, sentencePlanEntity));
            sentencePlanEntity.setAssessmentNeedsLastImportedOn(LocalDateTime.now(clock));
            sentencePlanEntity.setSafeguardingRisks(oasysAssessment.getChildSafeguardingIndicated());
        }
    }

    private List<NeedEntity> getNeedsFromOasysAssessment(OasysAssessment assessment, SentencePlanEntity sentencePlanEntity) {
     List<AssessmentNeed> needs = assessment.getNeeds() == null ? Collections.emptyList() : assessment.getNeeds();
      return needs.stream()
              .map(n-> new NeedEntity(n.getSection(), n.getName(),n.getOverThreshold(), n.getRiskOfReoffending(), n.getRiskOfHarm(), n.getFlaggedAsNeed(),
                      true, sentencePlanEntity)).collect(Collectors.toList());
    }
}
