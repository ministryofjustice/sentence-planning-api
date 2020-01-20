package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.CommentType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CommentEntityTest {

    private static String comment = "Any Comment";
    private static final CommentType type = CommentType.LIAISON_ARRANGEMENTS;
    private static final String createdBy = "Any User";

    @Test
    public void shouldCreateCommentEntity() {

        var commentEntity = new CommentEntity(comment, type, createdBy);

        assertThat(commentEntity.getComment()).isEqualTo(comment);
        assertThat(commentEntity.getCommentType()).isEqualTo(type);
        assertThat(commentEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(commentEntity.getCreated()).isEqualToIgnoringSeconds(LocalDateTime.now());
    }

}