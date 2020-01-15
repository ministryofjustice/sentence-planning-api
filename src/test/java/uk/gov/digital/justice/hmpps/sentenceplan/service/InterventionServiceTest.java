package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysRefElement;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.InterventionRespository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class InterventionServiceTest {

    @Mock
    OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    InterventionRefService interventionRefService;

    @Mock
    InterventionRespository interventionRespository;

    final List<OasysRefElement> oasysInterventions = List.of(
            new OasysRefElement("INT1" ,"Int 1", "Intervention 1"),
            new OasysRefElement("INT2" ,"Int 2", "Intervention 2"),
            new OasysRefElement("INT3" ,"Int 3", "Intervention 3")
    );

    final List<InterventionRefEntity> interventions = List.of(
            new InterventionRefEntity(1L, UUID.randomUUID(),  "INT2" ,"Int 2", "Intervention 2", true),
            new InterventionRefEntity(1L, UUID.randomUUID(),  "INT3" ,"Int 3", "Intervention 3", true),
            new InterventionRefEntity(1L, UUID.randomUUID(),  "INT4" ,"Int 4", "Intervention 4", true)
    );


    @Before
    public void setup() {
        interventionRefService = new InterventionRefService(interventionRespository,oasysAssessmentAPIClient);
    }

    @Test
    public void shouldGetupdatedInterventionsFromOasysOnRefresh() {
       when(oasysAssessmentAPIClient.getInterventionRefData()).thenReturn(oasysInterventions);
       when(interventionRespository.findAll()).thenReturn(interventions);
       interventionRefService.refreshInterventions();
       verify(oasysAssessmentAPIClient, times(1)).getInterventionRefData();
    }

    @Test
    public void shouldSaveNewInterventions() {
        when(oasysAssessmentAPIClient.getInterventionRefData()).thenReturn(oasysInterventions);
        when(interventionRespository.findAll()).thenReturn(interventions);
        interventionRefService.refreshInterventions();
        verify(interventionRespository, times(3)).save(any());
    }

    @Test
    public void shouldSetInterventionsNotInOASysUpdateToInActive() {
        when(oasysAssessmentAPIClient.getInterventionRefData()).thenReturn(oasysInterventions);
        when(interventionRespository.findAll()).thenReturn(interventions);
        interventionRefService.refreshInterventions();

        assertThat(interventions).extracting("externalReference", "active")
                .contains(tuple("INT4", false),tuple("INT3", true),tuple("INT2", true));
    }

    @Test
    public void shouldGetActiveInterventions() {
        when(interventionRespository.findAllByActiveIsTrue()).thenReturn(interventions);
        var result = interventionRefService.getActiveInterventions();
        assertThat(result).hasSize(3);
        verify(interventionRespository, times(1)).findAllByActiveIsTrue();

    }

    @Test
    public void shouldGetAllInterventions() {
        when(interventionRespository.findAll()).thenReturn(interventions);
        var result = interventionRefService.getAllInterventions();
        assertThat(result).hasSize(3);
        verify(interventionRespository, times(1)).findAll();

    }
}