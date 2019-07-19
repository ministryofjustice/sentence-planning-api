package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderReferenceType;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Create Sentence Plan Offender details")
public class CreateSentencePlanRequest {

    @ApiModelProperty(required = true, value = "The Offender's ID", example = "true")
    @NotNull
    @JsonProperty("offenderId")
    private String offenderId;

    @ApiModelProperty(required = true, value = "The System the Offender ID relates to", example = "true")
    @NotNull
    @JsonProperty("offenderReferenceType")
    private OffenderReferenceType offenderReferenceType;
}
