package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.NoOffenderAssessmentException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AssessmentService {
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;


    public AssessmentService(OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
    }

    public void addLatestAssessmentNeedsToPlan(SentencePlanEntity sentencePlanEntity) {
        var oasysAssessment = oasysAssessmentAPIClient.getLatestLayer3AssessmentForOffender(
                sentencePlanEntity.getOffender().getOasysOffednerId())
                .orElseThrow(NoOffenderAssessmentException::new);

        sentencePlanEntity.addNeeds(getNeedsFromOasysAssessment(oasysAssessment, sentencePlanEntity));
    }
    private List<NeedEntity> getNeedsFromOasysAssessment(OasysAssessment assessment, SentencePlanEntity sentencePlanEntity) {
      return assessment.getNeeds().stream()
              .map(n-> new NeedEntity(n.getName(),n.getOverThreshold(), n.getRiskOfReoffending(), n.getRiskOfHarm(), n.getFlaggedAsNeed(),
                      true, sentencePlanEntity)).collect(Collectors.toList());
    }
}
