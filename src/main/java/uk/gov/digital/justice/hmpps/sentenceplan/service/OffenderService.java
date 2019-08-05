package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.OffenderRespository;


@Service
@Slf4j
public class OffenderService {

private OffenderRespository offenderRespository;
private OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    public OffenderService(OffenderRespository offenderRespository, OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
        this.offenderRespository = offenderRespository;
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
    }

    public OffenderEntity getOffenderByType(String offenderId, OffenderReferenceType offenderReferenceType) {
        switch (offenderReferenceType) {
            case OASYS:
                return retrieveOasysOffender(offenderId);
        }
        throw new RuntimeException("Unknown offender reference type");
    }

    private OffenderEntity retrieveOasysOffender(String offenderId) {
         return offenderRespository.findByOasysOffednerId(offenderId).orElseGet(
                 () -> saveOASysOffender(oasysAssessmentAPIClient.getOffenderById(offenderId)
                         .orElseThrow(() -> new EntityNotFoundException(String.format("Offender %s not found", offenderId)))));
    }

    private OffenderEntity saveOASysOffender(OasysOffender oasysOffender) {
        var offender = new OffenderEntity(oasysOffender.getOasysOffenderId().toString(), oasysOffender.getIdentifiers().getNomisId());
        offenderRespository.save(offender);
        return offender;
    }


}
