package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysIdentifiers;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.OffenderRespository;

import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OffenderServiceTest {

    @Mock
    OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    @Mock
    OffenderRespository offenderRespository;

    OffenderService offenderService;

    @Before
    public void setup() {
        offenderService = new OffenderService(offenderRespository,oasysAssessmentAPIClient);
    }

    @Test
    public void shouldStoreOffenderMetaDataIfNotExists() {

        var offender = new OasysOffender(123456L,"Mr", "John", "Smith","","",new OasysIdentifiers("12345"));

        when(offenderRespository.findByOasysOffednerId("123456")).thenReturn(Optional.empty());


        when(oasysAssessmentAPIClient.getOffenderById("123456"))
                .thenReturn(Optional.ofNullable(offender));

        offenderService.getOffenderByType("123456", OffenderReferenceType.OASYS);


        verify( offenderRespository, times(1)).save(any());

    }

    @Test
    public void shouldNotGetOffenderFromOasysIfExistsInRepository() {

        var offender = new OffenderEntity(1L, UUID.randomUUID(), "123456","", "", EMPTY_LIST);

        when(offenderRespository.findByOasysOffednerId("123456")).thenReturn(Optional.ofNullable(offender));

        offenderService.getOffenderByType("123456", OffenderReferenceType.OASYS);

        verify( offenderRespository, never()).save(any());
        verify( oasysAssessmentAPIClient, never()).getOffenderById(any());
    }

    @Test
    public void shouldThrowExceptionWhenOffenderNotFound() {
        when(offenderRespository.findByOasysOffednerId("123456")).thenReturn(Optional.empty());
        when(oasysAssessmentAPIClient.getOffenderById("123456"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> {  offenderService.getOffenderByType("123456",OffenderReferenceType.OASYS);})
                .isInstanceOf(EntityNotFoundException.class);
    }
}