package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.NeedEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "A need")
public class NeedDto {
    @JsonProperty("id")
    private UUID id;
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

    public static NeedDto from(NeedEntity need) {
        return new NeedDto(need.getUuid(),
                need.getDescription(),
                need.getOverThreshold(),
                need.getHarmRisk(),
                need.getReoffendingRisk(),
                need.getLowScoreRisk(),
                need.getActive());
    }

    public static List<NeedDto> from(List<NeedEntity> needs) {
        return needs.stream().map(NeedDto::from).collect(Collectors.toList());
    }
}
