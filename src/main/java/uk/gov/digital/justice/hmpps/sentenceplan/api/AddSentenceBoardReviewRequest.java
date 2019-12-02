package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Add a new Sentence Board Review")
public class AddSentenceBoardReviewRequest {

    @ApiModelProperty(required = true, value = "Comments from the review")
    @NotNull
    @JsonProperty("comments")
    private String comments;

    @ApiModelProperty(required = true, value = "Attendees of the review")
    @NotNull
    @JsonProperty("attendees")
    private String attendees;

    @ApiModelProperty(required = true, value = "Date of the review")
    @NotNull
    @JsonProperty("dateOfBoard")
    private LocalDate dateOfBoard;


}
