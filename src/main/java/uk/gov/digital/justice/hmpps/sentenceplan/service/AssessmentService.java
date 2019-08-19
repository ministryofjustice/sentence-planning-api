package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.NoOffenderAssessmentException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AssessmentService {
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;


    public AssessmentService(OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
    }

    public void addLatestAssessmentNeedsToPlan(SentencePlanEntity sentencePlanEntity) {
        log.info("Adding new assessment needs to sentence plan {}", sentencePlanEntity.getUuid());
        var oasysAssessment = oasysAssessmentAPIClient.getLatestLayer3AssessmentForOffender(
                sentencePlanEntity.getOffender().getOasysOffenderId())
                .orElseThrow(NoOffenderAssessmentException::new);

        sentencePlanEntity.setSafeguardingRisks(oasysAssessment.getChildSafeguardingIndicated(), oasysAssessment.getComplyWithChildProtectionPlanIndicated());
        sentencePlanEntity.addNeeds(getNeedsFromOasysAssessment(oasysAssessment, sentencePlanEntity));
    }
    private List<NeedEntity> getNeedsFromOasysAssessment(OasysAssessment assessment, SentencePlanEntity sentencePlanEntity) {
     List<AssessmentNeed> needs = assessment.getNeeds() == null ? Collections.emptyList() : assessment.getNeeds();
      return needs.stream()
              .map(n-> new NeedEntity(n.getName(),n.getOverThreshold(), n.getRiskOfReoffending(), n.getRiskOfHarm(), n.getFlaggedAsNeed(),
                      true, sentencePlanEntity)).collect(Collectors.toList());
    }
}
