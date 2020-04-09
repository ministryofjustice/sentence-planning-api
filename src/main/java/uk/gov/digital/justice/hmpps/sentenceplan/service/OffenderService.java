package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.OffenderRespository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
public class OffenderService {
private final OffenderRespository offenderRespository;
private final OASYSAssessmentAPIClient oasysAssessmentAPIClient;
private final Clock clock;

    public OffenderService(OffenderRespository offenderRespository, OASYSAssessmentAPIClient oasysAssessmentAPIClient, Clock clock) {
        this.offenderRespository = offenderRespository;
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
        this.clock = clock;
    }

    @Transactional
    public OffenderEntity getOasysOffender(Long offenderId) {
        var offender = offenderRespository.findByOasysOffenderId(offenderId).orElseGet(() -> saveOASysOffender(getOasysOffenderFromOasys(offenderId)));
        return updateOasysOffender(offender);
    }

    @Transactional
    public OffenderEntity getSentencePlanOffender(UUID sentencePlanUuid) {
        var offender =  Optional.ofNullable(offenderRespository.findOffenderBySentencePlanUuid(sentencePlanUuid))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Sentence Plan %s not found", sentencePlanUuid)));
        return updateOasysOffender(offender);
    }

    public OasysOffender getOasysOffenderFromOasys(Long offenderId) {
        return oasysAssessmentAPIClient.getOffenderById(offenderId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Offender %s not found", offenderId)));
    }

    private OffenderEntity saveOASysOffender(OasysOffender oasysOffender) {
        var offender = new OffenderEntity(
                oasysOffender.getOasysOffenderId(),
                oasysOffender.getNomisId(),
                oasysOffender.getBookingNumber(),
                oasysOffender.getCrn());
        offenderRespository.save(offender);
        return offender;
    }

    private OffenderEntity updateOasysOffender(OffenderEntity offenderEntity) {
        if(offenderEntity.getOasysOffenderLastImportedOn() == null || offenderEntity.getOasysOffenderLastImportedOn().getDayOfYear() < LocalDateTime.now(clock).getDayOfYear()) {
            var oasysOffender = getOasysOffenderFromOasys(offenderEntity.getOasysOffenderId());
            offenderEntity.updateIdentityDetails(
                    oasysOffender.getOasysOffenderId(),
                    oasysOffender.getNomisId(),
                    oasysOffender.getBookingNumber(),
                    oasysOffender.getCrn());
        }
        return offenderEntity;
    }
}
