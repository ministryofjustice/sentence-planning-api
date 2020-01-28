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
public class MotivationRef {
    @JsonProperty("uuid")
    private UUID UUID;
    @JsonProperty("motivationText")
    private String motivationText;

    public static MotivationRef from(MotivationRefEntity motivation) {
        return new MotivationRef(motivation.getUuid(),
                motivation.getMotivationText());
    }

    public static List<MotivationRef> from(List<MotivationRefEntity> motivations) {
        return motivations.stream().map(MotivationRef::from).collect(Collectors.toList());
    }
}
