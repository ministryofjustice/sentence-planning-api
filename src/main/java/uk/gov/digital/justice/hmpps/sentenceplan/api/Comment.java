package uk.gov.digital.justice.hmpps.sentenceplan.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.CommentEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Comment {

    @JsonProperty("comments")
    private String comments;

    @JsonProperty("author")
    private StepOwner author;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("createdBy")
    private String createdBy;


    public static Comment from(CommentEntity commentEntity) {
        return new Comment(commentEntity.getComment(), commentEntity.getAuthor(), commentEntity.getCreated(), commentEntity.getCreatedBy());
    }

    public static List<Comment> from(List<CommentEntity> commentEntities) {
        return commentEntities.stream().map(Comment::from).collect(Collectors.toList());
    }
}
