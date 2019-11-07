package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Add a new Sentence Plan Objective")
public class AddSentencePlanObjective {

    @ApiModelProperty(required = true, value = "Description of the Objective")
    @JsonProperty("description")
    private String description;

    @ApiModelProperty(required = true, value = "A list of Need IDs (UUID) associated with the Objective")
    @JsonProperty("needs")
    private List<UUID> needs;

}
