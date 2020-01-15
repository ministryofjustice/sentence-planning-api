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
@ApiModel(description = "Associate a motivation from the reference data with a need")
public class AssociateMotivationNeedRequest {

    @ApiModelProperty(required = true, value = "The Need UUID ", example = "true")
    @NotNull
    @JsonProperty("needUUID")
    private UUID needUUID;

    @ApiModelProperty(required = true, value = "The Motivation UUID", example = "true")
    @NotNull
    @JsonProperty("motivationUUID")
    private UUID motivationUUID;

}
