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
    private StepOwner author;
    private LocalDateTime created;
    private String createdBy;

    public CommentEntity(String comment, StepOwner author, String createdBy) {
        this.comment = comment;
        this.author = author;
        this.created = LocalDateTime.now();
        this.createdBy = createdBy;
    }
}
