package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SentenceBoardReviewEntityTest {

    private static String comments = "Any Comment";
    private static String attendees = "Any User";
    private static LocalDate dateOfBoard = LocalDate.now();

    @Test
    public void shouldCreateSentenceBoardReviewEntity() {


        var sentenceReview = new SentenceBoardReviewEntity(comments, attendees,  dateOfBoard, null);

        assertThat(sentenceReview.getComments()).isEqualTo(comments);
        assertThat(sentenceReview.getAttendees()).isEqualTo(attendees);
        assertThat(sentenceReview.getDateOfBoard()).isEqualTo(dateOfBoard);

    }

}