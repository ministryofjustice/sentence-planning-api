package uk.gov.digital.justice.hmpps.sentenceplan.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public
class AssessmentNeed {
    @JsonProperty("name")
    private String name;
    @JsonProperty("overThreshold")
    private Boolean overThreshold;
    @JsonProperty("riskOfHarm")
    private Boolean riskOfHarm;
    @JsonProperty("riskOfReoffending")
    private Boolean riskOfReoffending;
    @JsonProperty("flaggedAsNeed")
    private Boolean flaggedAsNeed;
}
