package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.MotivationRefEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.MotivationRefDataRespository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MotivationServiceTest {


    @Mock
    MotivationRefDataRespository motivationRefDataRespository;

    MotivationService motivationService;

    @Before
    public void setup() {
        motivationService = new MotivationService(motivationRefDataRespository);
    }

    /**
     * Should return an empty list (not null) if there are no database results
     */
    @Test
    public void shouldReturnEmptyListIfNotDataExists() {

        when(motivationRefDataRespository.findAllByDeletedIsNull()).thenReturn(new ArrayList<>(0));

        List<Motivation> motivations = motivationService.getActiveMotivations();


        verify(motivationRefDataRespository, times(1)).findAllByDeletedIsNull();
        verifyNoMoreInteractions(motivationRefDataRespository);

        assertThat(motivations).hasSize(0);
    }

    /**
     * Should return if there are database results
     */
    @Test
    public void shouldReturnIfNotExists() {

        when(motivationRefDataRespository.findAllByDeletedIsNull()).thenReturn(List.of(new MotivationRefEntity("Motivation", "Friendly")));

        List<Motivation> motivations = motivationService.getActiveMotivations();

        verify(motivationRefDataRespository, times(1)).findAllByDeletedIsNull();
        verifyNoMoreInteractions(motivationRefDataRespository);

        assertThat(motivations).hasSize(1);

        Motivation motivation = motivations.get(0);
        assertThat(motivation.getMotivationText()).isEqualTo("Motivation");
        assertThat(motivation.getFriendlyText()).isEqualTo("Friendly");

    }

}