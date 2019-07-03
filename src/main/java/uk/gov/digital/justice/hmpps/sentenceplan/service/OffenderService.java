package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.OffenderRespository;


@Service
@Slf4j
public class OffenderService {

private OffenderRespository offenderRespository;

    public OffenderService( OffenderRespository offenderRespository) {
        this.offenderRespository = offenderRespository;
    }

    public OffenderEntity getOffenderByType(String offenderId, OffenderReferenceType offenderReferenceType) {
        return null;
    }

}
