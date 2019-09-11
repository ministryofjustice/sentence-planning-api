package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysIdentifiers;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.OffenderRespository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus.DRAFT;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner.PRACTITIONER;

@RunWith(MockitoJUnitRunner.class)
public class OffenderServiceTest {

    @Mock
    OASYSAssessmentAPIClient oasysAssessmentAPIClient;

    @Mock
    OffenderRespository offenderRespository;

    Clock clock = Clock.systemDefaultZone();

    OffenderService offenderService;

    private final UUID sentencePlanUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Before
    public void setup() {
        offenderService = new OffenderService(offenderRespository,oasysAssessmentAPIClient, clock);
    }

    @Test
    public void shouldStoreOffenderMetaDataIfNotExists() {

        var offender = new OasysOffender(123456L,"Mr", "John", "Smith","","",new OasysIdentifiers("12345", 123L));

        when(offenderRespository.findByOasysOffenderId(123456L)).thenReturn(Optional.empty());


        when(oasysAssessmentAPIClient.getOffenderById(123456L))
                .thenReturn(Optional.ofNullable(offender));

        offenderService.getOffenderByType("123456", OffenderReferenceType.OASYS);


        verify( offenderRespository, times(1)).save(any());

    }

    @Test
    public void shouldNotGetOffenderFromOasysIfExistsInRepository() {

        var offender = new OffenderEntity(1L, UUID.randomUUID(), 123456L,"", "", 123L, LocalDateTime.now(), EMPTY_LIST);

        when(offenderRespository.findByOasysOffenderId(123456L)).thenReturn(Optional.ofNullable(offender));

        offenderService.getOffenderByType("123456", OffenderReferenceType.OASYS);

        verify( offenderRespository, never()).save(any());
        verify( oasysAssessmentAPIClient, never()).getOffenderById(123456L);
    }

    @Test
    public void shouldThrowExceptionWhenOffenderNotFound() {
        when(offenderRespository.findByOasysOffenderId(123456L)).thenReturn(Optional.empty());
        when(oasysAssessmentAPIClient.getOffenderById(123456L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> {  offenderService.getOffenderByType("123456",OffenderReferenceType.OASYS);})
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void shouldUpdateBookingNumberIfNotUpdatedToday() {
        var sentencePlan = getSentencePlanWithOffender();
        OasysOffender offender = new OasysOffender(1L, null, null, null, null, null, new OasysIdentifiers("Nomis", 3456L));
        when(oasysAssessmentAPIClient.getOffenderById(1L)).thenReturn(Optional.ofNullable(offender));
        when(offenderRespository.save(any())).thenReturn(null);

        sentencePlan.getOffender().setOasysOffenderLastImportedOn(LocalDateTime.now(clock).minusDays(2));

        offenderService.updateOasysOffender(sentencePlan);
        verify(oasysAssessmentAPIClient, times(1)).getOffenderById(1L);
    }

    @Test
    public void shouldNotUpdateBookingNumberIfUpdatedToday() {
        var sentencePlan = getSentencePlanWithOffender();
        OasysOffender offender = new OasysOffender(1L, null, null, null, null, null, new OasysIdentifiers("Nomis", 3456L));

        sentencePlan.getOffender().setOasysOffenderLastImportedOn(LocalDateTime.now(clock).minusDays(0));

        offenderService.updateOasysOffender(sentencePlan);
        verify(oasysAssessmentAPIClient, times(0)).getOffenderById(1L);
    }

    private SentencePlanEntity getSentencePlanWithOffender() {

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var sentencePlanProperty = new SentencePlanPropertiesEntity();
        sentencePlanProperty.addStep(new StepEntity(PRACTITIONER, null, "a description", "a strength", StepStatus.PAUSED, needs, null));
        return SentencePlanEntity.builder()
                .createdOn(LocalDateTime.of(2019,6,1, 11,00))
                .status(DRAFT)
                .uuid(sentencePlanUuid)
                .offender(new OffenderEntity(1L, "two", 3L))
                .needs(List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").motivations(EMPTY_LIST).build()))
                .data(sentencePlanProperty).build();
    }
}