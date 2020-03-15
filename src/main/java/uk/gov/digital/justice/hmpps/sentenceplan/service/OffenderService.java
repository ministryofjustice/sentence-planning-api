package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
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

    public void updateOasysOffender(SentencePlanEntity sentencePlanEntity) {
        if(sentencePlanEntity.getOffender().getOasysOffenderLastImportedOn() == null || sentencePlanEntity.getOffender().getOasysOffenderLastImportedOn().getDayOfYear() < LocalDateTime.now(clock).getDayOfYear()) {
            var offenderId = sentencePlanEntity.getOffender().getOasysOffenderId();
            var offender = getOasysOffender(offenderId);
            sentencePlanEntity.getOffender().updateIdentityDetails(offender);
        }
    }

    public OffenderEntity getOasysOffender(Long offenderId) {
        return getOffenderByType(offenderId);
    }


    public OffenderEntity getOffenderByType(Long offenderId) {
        return retrieveOasysOffender(offenderId);
    }

    private OffenderEntity retrieveOasysOffender(long offenderId) {
         return offenderRespository.findByOasysOffenderId(offenderId).orElseGet(
                 () -> saveOASysOffender(oasysAssessmentAPIClient.getOffenderById(offenderId)
                         .orElseThrow(() -> new EntityNotFoundException(String.format("Offender %s not found", offenderId)))));
    }

    private OffenderEntity saveOASysOffender(OasysOffender oasysOffender) {
        var offender = new OffenderEntity(oasysOffender.getOasysOffenderId(), oasysOffender.getNomisId(), oasysOffender.getBookingNumber());
        offenderRespository.save(offender);
        return offender;
    }

    public OffenderEntity getSentencePlanOffender(UUID sentencePlanUuid) {
        return Optional.ofNullable(offenderRespository.findOffenderBySentencePlanUuid(sentencePlanUuid))
                .orElseThrow(() -> new EntityNotFoundException(String.format("Sentence Plan %s not found", sentencePlanUuid)));
    }

}
