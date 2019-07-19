package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.application.EntityNotFoundException;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.OffenderEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentencePlanEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentencePlanRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus.DRAFT;

@RunWith(MockitoJUnitRunner.class)
public class SentencePlanServiceTest {

    @Mock
    private SentencePlanRepository sentencePlanRepository;

    @Mock
    private OffenderService offenderService;

    @Mock
    private AssessmentService assessmentService;

    private final String oasysOffenderId = "123456789";

    private SentencePlanService service;

    private final UUID sentencePlanUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Before
    public void setup() {
        service = new SentencePlanService(sentencePlanRepository, offenderService, assessmentService);
    }

    @Test
    public void createSentencePlanShouldRetrieveOffenderAndAssessmentAndSavePlan() {
        var offender = mock(OffenderEntity.class);;

        when(offenderService.getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS)).thenReturn(offender);
        when(sentencePlanRepository.save(any())).thenReturn(getNewSentencePlan());

        service.createSentencePlan(oasysOffenderId, OffenderReferenceType.OASYS);

        verify(offenderService,times(1)).getOffenderByType(oasysOffenderId,  OffenderReferenceType.OASYS);
        verify(sentencePlanRepository,times(1)).save(any());
    }


    @Test
    public void getSentencePlanShouldRetrievePlanFromRepository() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan());
        service.getSentencePlanFromUuid(sentencePlanUuid);
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void getSentencePlanShouldReturnSentencePlanFromEntity() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(getNewSentencePlan());
        var result =  service.getSentencePlanFromUuid(sentencePlanUuid);
        assertThat(result.getUuid()).isEqualTo(sentencePlanUuid);
        assertThat(result.getCreatedOn()).isEqualTo(LocalDateTime.of(2019,6,1, 11,00));
        assertThat(result.getStatus()).isEqualTo(DRAFT);
        verify(sentencePlanRepository,times(1)).findByUuid(sentencePlanUuid);
    }

    @Test
    public void getSentencePlanShouldThrowNotFoundException() {
        when(sentencePlanRepository.findByUuid(sentencePlanUuid)).thenReturn(null);
        var exception = catchThrowable(() -> { service.getSentencePlanFromUuid(sentencePlanUuid); });
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sentence Plan " + sentencePlanUuid   + " not found");

    }

    private SentencePlanEntity getNewSentencePlan() {
        return SentencePlanEntity.builder()
                .createdOn(LocalDateTime.of(2019,6,1, 11,00))
                .status(DRAFT)
                .uuid(sentencePlanUuid)
                .needs(new ArrayList<>()).build();
    }

}