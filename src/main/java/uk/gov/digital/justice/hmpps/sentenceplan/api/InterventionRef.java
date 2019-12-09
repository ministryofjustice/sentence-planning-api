package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Intervention Reference Data")
public class InterventionRef {

    @JsonProperty("uuid")
    private UUID uuid;
    @JsonProperty("shortDescription")
    private String shortDescription;
    @JsonProperty("longDescription")
    private String longDescription;

    public static InterventionRef from(InterventionRefEntity interventionRefEntity) {
        return new InterventionRef(interventionRefEntity.getUuid(),
                interventionRefEntity.getShortDescription(),
                interventionRefEntity.getDescription());
    }
}
