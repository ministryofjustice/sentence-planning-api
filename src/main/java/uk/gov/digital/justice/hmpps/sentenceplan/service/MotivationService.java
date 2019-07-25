package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.Motivation;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.MotivationRespository;

import java.util.List;

@Service
@Slf4j
public class MotivationService {

private MotivationRespository motivationRespository;

    public MotivationService(MotivationRespository motivationRespository) {
        this.motivationRespository = motivationRespository;
    }

    public List<Motivation> getActiveMotivations() {

        return Motivation.from(motivationRespository.findAllByDeletedIsNull());
    }

}
