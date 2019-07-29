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
public class MotivationRefServiceTest {


    @Mock
    MotivationRefDataRespository motivationRefDataRespository;

    MotivationRefService motivationRefService;

    @Before
    public void setup() {
        motivationRefService = new MotivationRefService(motivationRefDataRespository);
    }

    /**
     * Should return an empty list (not null) if there are no database results
     */
    @Test
    public void shouldReturnEmptyListIfNotDataExists() {

        when(motivationRefDataRespository.findAllByDeletedIsNull()).thenReturn(new ArrayList<>(0));

        List<MotivationRef> motivationRefs = motivationRefService.getActiveMotivations();


        verify(motivationRefDataRespository, times(1)).findAllByDeletedIsNull();
        verifyNoMoreInteractions(motivationRefDataRespository);

        assertThat(motivationRefs).hasSize(0);
    }

    /**
     * Should return if there are database results
     */
    @Test
    public void shouldReturnIfNotExists() {

        when(motivationRefDataRespository.findAllByDeletedIsNull()).thenReturn(List.of(new MotivationRefEntity("MotivationRef", "Friendly")));

        List<MotivationRef> motivationRefs = motivationRefService.getActiveMotivations();

        verify(motivationRefDataRespository, times(1)).findAllByDeletedIsNull();
        verifyNoMoreInteractions(motivationRefDataRespository);

        assertThat(motivationRefs).hasSize(1);

        MotivationRef motivationRef = motivationRefs.get(0);
        assertThat(motivationRef.getMotivationText()).isEqualTo("MotivationRef");
        assertThat(motivationRef.getFriendlyText()).isEqualTo("Friendly");

    }

}