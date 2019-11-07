package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
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

    @ApiModelProperty(required = true, value = "Action motivation", example = "true")
    @NotNull
    @JsonProperty("motivationUUID")
    private UUID motivationUUID;

}
