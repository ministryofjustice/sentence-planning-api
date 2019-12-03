package uk.gov.digital.justice.hmpps.sentenceplan.api;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.InterventionRefEntity;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ApiModel(description = "Intervention")
public class InterventionRef {

    private UUID uuid;
    private String shortDescription;
    private String longDescription;

    public static InterventionRef from(InterventionRefEntity interventionRefEntity) {
        return new InterventionRef(interventionRefEntity.getUuid(),
                interventionRefEntity.getShortDescription(),
                interventionRefEntity.getDescription());
    }
}
