package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.MotivationRefEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Motivation Reference Data")
public class MotivationRefDto {
    @JsonProperty("uuid")
    private UUID UUID;
    @JsonProperty("motivationText")
    private String motivationText;

    public static MotivationRefDto from(MotivationRefEntity motivation) {
        return new MotivationRefDto(motivation.getUuid(),
                motivation.getMotivationText());
    }

    public static List<MotivationRefDto> from(List<MotivationRefEntity> motivations) {
        return motivations.stream().map(MotivationRefDto::from).collect(Collectors.toList());
    }
}
