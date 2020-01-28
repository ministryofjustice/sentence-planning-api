package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.SentenceBoardReviewEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Sentence Plan Review")
public class SentenceBoardReviewDto {

    @JsonProperty("id")
    private UUID id;
    @JsonProperty("comments")
    private String comments;
    @JsonProperty("attendees")
    private String attendees;
    @JsonProperty("dateOfBoard")
    private LocalDate dateOfBoard;

    public static SentenceBoardReviewDto from(SentenceBoardReviewEntity review) {
        return new SentenceBoardReviewDto(review.getUuid(), review.getComments(), review.getAttendees(), review.getDateOfBoard());
    }

    public static List<SentenceBoardReviewDto> from(Collection<SentenceBoardReviewEntity> reviews) {
        return reviews.stream().map(SentenceBoardReviewDto::from).collect(Collectors.toList());
    }
}
