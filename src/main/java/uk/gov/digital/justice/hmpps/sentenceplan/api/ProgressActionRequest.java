package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Progress an Action")
public class ProgressActionRequest {

    @ApiModelProperty(required = true, value = "Action status", example = "true")
    @NotNull
    @JsonProperty("status")
    private ActionStatus status;

    @ApiModelProperty(required = true, value = "Target date for the Action")
    @JsonProperty("targetDate")
    private YearMonth targetDate;

    @ApiModelProperty(required = true, value = "Action motivation", example = "true")
    @NotNull
    @JsonProperty("motivationUUID")
    private UUID motivationUUID;

    @ApiModelProperty(required = true, value = "The owner of the Action")
    @NotNull
    @JsonProperty("owner")
    private List<ActionOwner> owner;

    @ApiModelProperty(required = true, value = "Other owner (if other type used)")
    @JsonProperty("ownerOther")
    private String ownerOther;

    @ApiModelProperty(required = true, value = "Comment for the update")
    @JsonProperty("comment")
    private String comment;
}
