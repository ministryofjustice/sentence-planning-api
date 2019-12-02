package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.ObjectiveEntity;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentenceBoardReviewEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SentenceBoardReview {

    @JsonProperty("id")
    private UUID id;
    @JsonProperty("comments")
    private String comments;
    @JsonProperty("attendees")
    private String attendees;
    @JsonProperty("dateOfBoard")
    private LocalDate dateOfBoard;

    public static SentenceBoardReview from(SentenceBoardReviewEntity review) {
        return new SentenceBoardReview(review.getUuid(), review.getComments(), review.getAttendees(), review.getDateOfBoard());
    }

    public static List<SentenceBoardReview> from(Collection<SentenceBoardReviewEntity> reviews) {
        return reviews.stream().map(SentenceBoardReview::from).collect(Collectors.toList());
    }
}
