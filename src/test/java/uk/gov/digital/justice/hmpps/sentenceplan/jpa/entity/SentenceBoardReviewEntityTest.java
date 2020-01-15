package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SentenceBoardReviewEntityTest {

    private static final String comments = "Any Comment";
    private static final String attendees = "Any User";
    private static final LocalDate dateOfBoard = LocalDate.now();

    @Test
    public void shouldCreateSentenceBoardReviewEntity() {


        var sentenceReview = new SentenceBoardReviewEntity(comments, attendees,  dateOfBoard, null);

        assertThat(sentenceReview.getComments()).isEqualTo(comments);
        assertThat(sentenceReview.getAttendees()).isEqualTo(attendees);
        assertThat(sentenceReview.getDateOfBoard()).isEqualTo(dateOfBoard);

    }

}