package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Progress a step")
public class ProgressStepRequest {

    @ApiModelProperty(required = true, value = "Step status", example = "true")
    @NotNull
    @JsonProperty("status")
    private StepStatus status;

    @ApiModelProperty(required = true, value = "The comments from the practitioner", example = "true")
    @NotNull
    @JsonProperty("comments")
    private String practitionerComments;

}
