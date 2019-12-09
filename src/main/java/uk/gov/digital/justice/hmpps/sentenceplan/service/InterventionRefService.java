package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.InterventionRespository;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InterventionRefService {

    private InterventionRespository interventionRespository;
    private OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    public InterventionRefService(InterventionRespository interventionRespository, OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
        this.interventionRespository = interventionRespository;
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
    }

    public List<InterventionRefEntity> getActiveInterventions() {
        return interventionRespository.findAllByActiveIsTrue();
    }

    public List<InterventionRefEntity> getAllInterventions() {
        return interventionRespository.findAll();
    }

    @Transactional
    public void refreshInterventions() {
        var updatedInterventions = oasysAssessmentAPIClient.getInterventionRefData();
        var existingInterventions = interventionRespository.findAll();

        existingInterventions.stream().forEach(i -> i.setActive(false));

        //add new and update existing interventions
        updatedInterventions.stream().forEach(
                updatedIntervention -> {
                    var intervention = existingInterventions.stream()
                            .filter(i -> i.getExternalReference().equals(updatedIntervention.getCode())).findFirst()
                            .orElse(new InterventionRefEntity());

                    intervention.setDescription(updatedIntervention.getDescription());
                    intervention.setShortDescription(updatedIntervention.getShortDescription());
                    intervention.setActive(true);
                    intervention.setExternalReference(updatedIntervention.getCode());
                    interventionRespository.save(intervention);
                });
    }
}
