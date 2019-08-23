package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CommentEntity implements Serializable {

    private String comment;
    private StepOwner owner;
    private LocalDateTime created;
    private String createdBy;

    public CommentEntity(String comment, StepOwner owner, String createdBy) {
        this.comment = comment;
        this.owner = owner;
        this.created = LocalDateTime.now();
        this.createdBy = createdBy;
    }
}
