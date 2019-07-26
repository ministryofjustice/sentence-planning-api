package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.Motivation;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.MotivationRefDataRespository;

import java.util.List;

@Service
@Slf4j
public class MotivationService {

private MotivationRefDataRespository motivationRefDataRespository;

    public MotivationService(MotivationRefDataRespository motivationRefDataRespository) {
        this.motivationRefDataRespository = motivationRefDataRespository;
    }

    public List<Motivation> getActiveMotivations() {

        return Motivation.from(motivationRefDataRespository.findAllByDeletedIsNull());
    }

}
