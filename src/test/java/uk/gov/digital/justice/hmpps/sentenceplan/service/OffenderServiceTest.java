package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.client.OASYSAssessmentAPIClient;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysIdentifiers;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.OffenderRespository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.EMPTY_LIST;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.SERVICE_USER;

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

        var offender = new OasysOffender(123456L, "John", "Smith","","",new OasysIdentifiers("12345", 123L));

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
        var offender = new OasysOffender(1L, null, null, null, null, new OasysIdentifiers("Nomis", 3456L));
        when(oasysAssessmentAPIClient.getOffenderById(1L)).thenReturn(Optional.ofNullable(offender));
        when(offenderRespository.save(any())).thenReturn(null);

        sentencePlan.getOffender().setOasysOffenderLastImportedOn(LocalDateTime.now(clock).minusDays(2));

        offenderService.updateOasysOffender(sentencePlan);
        verify(oasysAssessmentAPIClient, times(1)).getOffenderById(1L);
    }

    @Test
    public void shouldNotUpdateBookingNumberIfUpdatedToday() {
        var sentencePlan = getSentencePlanWithOffender();

        sentencePlan.getOffender().setOasysOffenderLastImportedOn(LocalDateTime.now(clock).minusDays(0));

        offenderService.updateOasysOffender(sentencePlan);
        verify(oasysAssessmentAPIClient, times(0)).getOffenderById(1L);
    }

    private SentencePlanEntity getSentencePlanWithOffender() {

        var needs = List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"));
        var sentencePlanProperty = new SentencePlanPropertiesEntity();
        var objective = new ObjectiveEntity("Objective 1", needs);
        var action = new ActionEntity(null,"Action 1", YearMonth.of(2019,8), UUID.fromString("11111111-1111-1111-1111-111111111111"), List.of(SERVICE_USER), null, ActionStatus.NOT_STARTED);
        objective.addAction(action);
        sentencePlanProperty.setObjectives(Map.of(objective.getId(), objective));
        return SentencePlanEntity.builder()
                .createdDate(LocalDateTime.of(2019,6,1, 11,00))
                .startedDate(LocalDateTime.of(2019,7,1, 11,00))
                .uuid(sentencePlanUuid)
                .offender(new OffenderEntity(1L, "two", 3L))
                .needs(List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").build()))
                .data(sentencePlanProperty).build();
    }


}