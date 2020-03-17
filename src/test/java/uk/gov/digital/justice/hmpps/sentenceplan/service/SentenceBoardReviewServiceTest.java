package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.client.SectionHeader;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentenceBoardReviewRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SentenceBoardReviewServiceTest {

    @Mock
    private SentencePlanService sentencePlanService;

    @Mock
    private SentenceBoardReviewRepository sentenceBoardReviewRepository;

    private SentenceBoardReviewService service;

    private final UUID sentencePlanUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID sentenceBoardReviewUuid = UUID.randomUUID();


    @Before
    public void setup() {
        service = new SentenceBoardReviewService(sentenceBoardReviewRepository, sentencePlanService);
    }

    @Test
    public void shouldCreateSentenceBoardReview() {
        var comments = "any comments";
        var attendees = "any attendees";
        var dateOfBoard = LocalDate.now();
        var sentencePlanEntity = getNewSentencePlan(sentencePlanUuid);

        when(sentencePlanService.getSentencePlanEntity(sentencePlanUuid)).thenReturn(sentencePlanEntity);

        service.addSentenceBoardReview(sentencePlanUuid, comments, attendees, dateOfBoard);

        verify(sentencePlanService,times(1)).getSentencePlanEntity(sentencePlanUuid);
        verify(sentenceBoardReviewRepository,times(1)).save(any());

    }

    @Test
    public void shouldGetSentenceBoardReviews() {
        when(sentenceBoardReviewRepository.findAllBySentencePlanUUID(sentencePlanUuid)).thenReturn(new ArrayList<>());

        service.getSentenceBoardReviewsBySentencePlanUUID(sentencePlanUuid);

        verify(sentenceBoardReviewRepository,times(1)).findAllBySentencePlanUUID(sentencePlanUuid);
    }

    @Test
    public void shouldGetSentenceBoardReview() {
        when(sentenceBoardReviewRepository.findByUuid(sentenceBoardReviewUuid)).thenReturn(new SentenceBoardReviewEntity());

        service.getSentenceBoardReviewBySBRUUID(sentenceBoardReviewUuid);

        verify(sentenceBoardReviewRepository,times(1)).findByUuid(sentenceBoardReviewUuid);
    }

    @Test
    public void shouldGetSentenceBoardReviewNotFound() {
        when(sentenceBoardReviewRepository.findByUuid(sentenceBoardReviewUuid)).thenReturn(null);

        var exception = catchThrowable(() -> service.getSentenceBoardReviewBySBRUUID(sentenceBoardReviewUuid));
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sentence Board Review " + sentenceBoardReviewUuid + " not found");
    }

    private SentencePlanEntity getNewSentencePlan(UUID uuid) {
        var needs = List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).header(SectionHeader.DRUG_MISUSE).description("description").build());
        var plan = new  SentencePlanEntity();
        plan.setUuid(uuid);
        plan.setNeeds(needs);
        return plan;
    }
}