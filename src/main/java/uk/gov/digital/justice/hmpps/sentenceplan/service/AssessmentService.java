package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.AssessmentEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;

@Service
@Slf4j
public class AssessmentService {
    public AssessmentEntity getLatestAssessmentForOffender(OffenderEntity offender) {
        return null;
    }
}
