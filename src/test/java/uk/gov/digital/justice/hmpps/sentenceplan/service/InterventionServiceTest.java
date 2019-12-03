package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.AssessmentNeed;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysAssessment;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysRefElement;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.InterventionRespository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.NoOffenderAssessmentException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class InterventionServiceTest {

    @Mock
    OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    InterventionRefService interventionRefService;

    @Mock
    InterventionRespository interventionRespository;

    List<OasysRefElement> oasysInterventions = List.of(
            new OasysRefElement("INT1" ,"Int 1", "Intervention 1"),
            new OasysRefElement("INT2" ,"Int 2", "Intervention 2"),
            new OasysRefElement("INT3" ,"Int 3", "Intervention 3")
    );

    List<InterventionRefEntity> interventions = List.of(
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
        assertThat(interventions.stream().filter(i->i.getExternalReference().equals("INT4")).findFirst().get().isActive()).isFalse();
        assertThat(interventions.stream().filter(i->i.getExternalReference().equals("INT3")).findFirst().get().isActive()).isTrue();
        assertThat(interventions.stream().filter(i->i.getExternalReference().equals("INT2")).findFirst().get().isActive()).isTrue();
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