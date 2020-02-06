package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SentenceBoardReviewEntityTest {

    private static final String comments = "Any Comment";
    private static final String attendees = "Any User";
    private static final LocalDate dateOfBoard = LocalDate.now();

    @Test
    public void shouldCreateSentenceBoardReviewEntity() {

        var offender = new OffenderEntity();
        offender.setOasysOffenderId(123456L);
        var sentencePlan = new SentencePlanEntity(offender);
        sentencePlan.setUuid(UUID.fromString("11111111-1111-1111-1111-111111111111"));

        var sentenceReview = new SentenceBoardReviewEntity(comments, attendees,  dateOfBoard, sentencePlan);

        assertThat(sentenceReview.getComments()).isEqualTo(comments);
        assertThat(sentenceReview.getAttendees()).isEqualTo(attendees);
        assertThat(sentenceReview.getDateOfBoard()).isEqualTo(dateOfBoard);

    }

}