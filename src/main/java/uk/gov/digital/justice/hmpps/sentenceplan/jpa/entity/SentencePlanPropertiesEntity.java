package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType;

import java.io.Serializable;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentencePlanPropertiesEntity implements Serializable {

    private Boolean childSafeguardingIndicated;
    private Boolean complyWithChildProtectionPlanIndicated;
    private Map<UUID, ObjectiveEntity> objectives = new HashMap<>(0);
    private EnumMap<CommentType, CommentEntity> comments = new EnumMap<>(CommentType.class);

    void addComment(CommentEntity commentEntity) {
        comments.put(commentEntity.getCommentType(), commentEntity);
    }
}
