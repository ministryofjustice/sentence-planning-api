package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Need {
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
    @JsonProperty("active")
    private Boolean active;


    public static Need from(NeedEntity need) {
        return new Need(need.getDescription(),
                need.getOverThreshold(),
                need.getHarmRisk(),
                need.getReoffendingRisk(),
                need.getLowScoreRisk(),
                need.getActive());
    }

    public static List<Need> from(List<NeedEntity> needs) {
        return needs.stream().map(Need::from).collect(Collectors.toList());
    }
}
