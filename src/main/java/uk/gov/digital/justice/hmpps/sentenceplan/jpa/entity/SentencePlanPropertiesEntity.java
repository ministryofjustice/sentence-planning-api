package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.Comment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentencePlanPropertiesEntity implements Serializable {

    private String serviceUserComments;
    private Boolean childSafeguardingIndicated;
    private Boolean complyWithChildProtectionPlanIndicated;
    private List<StepEntity> steps = new ArrayList<>();
    private List<CommentEntity> comments = new ArrayList<>();

    public void addStep(StepEntity stepEntity) {
        steps.add(stepEntity);
    }

    public void addComment(CommentEntity commentEntity) {
        comments.add(commentEntity);
    }
}
