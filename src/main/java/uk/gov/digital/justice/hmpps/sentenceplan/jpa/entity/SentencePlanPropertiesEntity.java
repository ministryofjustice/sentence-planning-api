package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class SentencePlanPropertiesEntity implements Serializable {

    private String serviceUserComments;
    private Boolean childSafeguardingIndicated;
    private Boolean complyWithChildProtectionPlanIndicated;
    List<StepEntity> steps;

    public SentencePlanPropertiesEntity() {
        steps = new ArrayList<>();
    }
}
