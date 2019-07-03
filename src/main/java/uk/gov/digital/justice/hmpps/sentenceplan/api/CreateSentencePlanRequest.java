package uk.gov.digital.justice.hmpps.sentenceplan.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderReferenceType;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Getter
@ApiModel(description = "Create Sentence Plan Offender details")
public class CreateSentencePlanRequest {

    @ApiModelProperty(required = true, value = "The Offender's ID", example = "true")
    @NotNull
    private String offenderId;

    @ApiModelProperty(required = true, value = "The System the Offender ID relates to", example = "true")
    @NotNull
    private OffenderReferenceType offenderReferenceType;
}
