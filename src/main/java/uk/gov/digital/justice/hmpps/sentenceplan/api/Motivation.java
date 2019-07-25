package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Motivation {
    @JsonProperty("uuid")
    private UUID UUID;
    @JsonProperty("motivationText")
    private String motivationText;
    @JsonProperty("friendlyText")
    private String friendlyText;


    public static Motivation from(MotivationRefEntity motivation) {
        return new Motivation(motivation.getUuid(),
                motivation.getMotivationText(),
                motivation.getFriendlyText());
    }

    public static List<Motivation> from(List<MotivationRefEntity> motivations) {
        return motivations.stream().map(Motivation::from).collect(Collectors.toList());
    }
}
