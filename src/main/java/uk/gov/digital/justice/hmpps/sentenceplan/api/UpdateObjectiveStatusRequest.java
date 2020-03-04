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
@ApiModel(description = "Update Objective Status")
public class UpdateObjectiveStatusRequest {

    @ApiModelProperty(required = true, value = "Comment for the update")
    @JsonProperty("comment")
    private String comment;
}
