package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.MotivationRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.MotivationRefDataRespository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MotivationRefService {

private MotivationRefDataRespository motivationRefDataRespository;

    public MotivationRefService(MotivationRefDataRespository motivationRefDataRespository) {
        this.motivationRefDataRespository = motivationRefDataRespository;
    }

    public List<MotivationRefEntity> getActiveMotivations() {

        return motivationRefDataRespository.findAllByDeletedIsNull();
    }

    public MotivationRefEntity getMotivationById(UUID motivationId) {
        return motivationRefDataRespository.findByUuid(motivationId);
    }


}
