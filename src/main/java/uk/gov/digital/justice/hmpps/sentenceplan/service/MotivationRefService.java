package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.MotivationRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.MotivationRefDataRespository;

import java.util.List;

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

}
