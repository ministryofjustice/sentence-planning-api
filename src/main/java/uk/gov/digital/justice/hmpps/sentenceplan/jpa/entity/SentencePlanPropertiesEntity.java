package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SentencePlanPropertiesEntity implements Serializable {

    private String serviceUserComments;
    private String practitionerComments;
    List<ActionEntity> actions;

    public SentencePlanPropertiesEntity() {
        actions = new ArrayList<>();
    }
}
