package uk.gov.digital.justice.hmpps.sentenceplan.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.justice.hmpps.sentenceplan.api.InterventionRefDto;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.InterventionRespository;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.EVENT;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.INTERVENTIONS_UPDATED;

@Service
@Slf4j
public class InterventionRefService {

    private final InterventionRespository interventionRespository;
    private final OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    public InterventionRefService(InterventionRespository interventionRespository, OASYSAssessmentAPIClient oasysAssessmentAPIClient) {
        this.interventionRespository = interventionRespository;
        this.oasysAssessmentAPIClient = oasysAssessmentAPIClient;
    }

    public List<InterventionRefDto> getActiveInterventions() {
        return interventionRespository.findAllByActiveIsTrue().stream()
                .map(InterventionRefDto::from).collect(Collectors.toList());
    }

    public List<InterventionRefDto> getAllInterventions() {
        return interventionRespository.findAll().stream()
                .map(InterventionRefDto::from).collect(Collectors.toList());
    }

    @Transactional
    public void refreshInterventions() {
        var updatedInterventions = oasysAssessmentAPIClient.getInterventionRefData();
        var existingInterventions = interventionRespository.findAll();

        existingInterventions.forEach(i -> i.setActive(false));

        //add new and update existing interventions
        updatedInterventions.forEach(
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
        log.info("Updated Interventions", value(EVENT, INTERVENTIONS_UPDATED));
    }
}
