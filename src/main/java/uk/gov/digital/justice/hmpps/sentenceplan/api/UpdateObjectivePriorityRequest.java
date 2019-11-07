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
@ApiModel(description = "Set the priority of an Objective.")
public class UpdateObjectivePriorityRequest {

    @ApiModelProperty(required = true, value = "The Objective UUID ", example = "true")
    @NotNull
    @JsonProperty("objectiveUUID")
    private UUID objectiveUUID;

    @ApiModelProperty(required = true, value = "The Objective priority", example = "true")
    @NotNull
    @JsonProperty("priority")
    private Integer priority;

}
