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
@ApiModel(description = "Add a new Sentence Plan Action")
public class AddSentencePlanActionRequest {

    @ApiModelProperty(required = true, value = "The intervention ID, if applicable")
    @JsonProperty("interventionUUID")
    private UUID interventionUUID;

    @ApiModelProperty(required = true, value = "Description for the Action")
    @JsonProperty("description")
    private String description;

    @ApiModelProperty(required = true, value = "Target date for the Action")
    @JsonProperty("targetDate")
    private YearMonth targetDate;

    @ApiModelProperty(required = true, value = "The motivation for the Action")
    @JsonProperty("motivationUUID")
    private UUID motivationUUID;

    @ApiModelProperty(required = true, value = "The owner of the Action")
    @NotNull
    @JsonProperty("owner")
    private List<ActionOwner> owner;

    @ApiModelProperty(required = true, value = "Other owner (if other type used)")
    @JsonProperty("ownerOther")
    private String ownerOther;

    @ApiModelProperty(required = true, value = "The status for the Action")
    @JsonProperty("status")
    private ActionStatus status;

}
