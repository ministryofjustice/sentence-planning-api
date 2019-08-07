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
@ApiModel(description = "Set the priority of a step.")
public class UpdateStepPriorityRequest {

    @ApiModelProperty(required = true, value = "The Step UUID ", example = "true")
    @NotNull
    @JsonProperty("stepUUID")
    private UUID stepUUID;

    @ApiModelProperty(required = true, value = "The step priority", example = "true")
    @NotNull
    @JsonProperty("priority")
    private Integer priority;

}
