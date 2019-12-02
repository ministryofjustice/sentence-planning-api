package uk.gov.digital.justice.hmpps.sentenceplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;
import uk.gov.digital.justice.hmpps.sentenceplan.api.AddCommentRequest;
import uk.gov.digital.justice.hmpps.sentenceplan.api.SentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.SentenceBoardReviewRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.CurrentSentencePlanForOffenderExistsException;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityNotFoundException;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.*;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.PRACTITIONER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionOwner.SERVICE_USER;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus.NOT_STARTED;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType.LIASON_ARRANGEMENTS;
import static uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType.YOUR_SUMMARY;

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
        String comments = "any comments";
        String attendees = "any attendees";
        LocalDate dateOfBoard = LocalDate.now();
        SentencePlanEntity sentencePlanEntity = getNewSentencePlan(sentencePlanUuid);

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

        service.getSentenceBoardReviewBySentencePlanUUID(sentencePlanUuid, sentenceBoardReviewUuid);

        verify(sentenceBoardReviewRepository,times(1)).findByUuid(sentenceBoardReviewUuid);
    }

    @Test
    public void shouldGetSentenceBoardReviewNotFound() {
        when(sentenceBoardReviewRepository.findByUuid(sentenceBoardReviewUuid)).thenReturn(null);

        var exception = catchThrowable(() -> { service.getSentenceBoardReviewBySentencePlanUUID(sentencePlanUuid, sentenceBoardReviewUuid); });
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Sentence Board Review " + sentenceBoardReviewUuid + " not found");
    }

    private SentencePlanEntity getNewSentencePlan(UUID uuid) {

        return SentencePlanEntity.builder()
                .createdDate(LocalDateTime.of(2019,6,1, 11,00))
                .startedDate(null)
                .uuid(uuid)
                .needs(List.of(NeedEntity.builder().uuid(UUID.fromString("11111111-1111-1111-1111-111111111111")).description("description").build()))
                .data(new SentencePlanPropertiesEntity()).build();
    }
}