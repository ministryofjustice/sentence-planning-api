package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.StepOwner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CommentEntityTest {

    private static String comment = "Any Comment";
    private static StepOwner owner = StepOwner.SERVICE_USER;
    private static String createdBy = "Any User";

    @Test
    public void shouldCreateCommentEntity() {

        var commentEntity = new CommentEntity(comment, owner, createdBy);

        assertThat(commentEntity.getComment()).isEqualTo(comment);
        assertThat(commentEntity.getOwner()).isEqualTo(owner);
        assertThat(commentEntity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(commentEntity.getCreated()).isEqualToIgnoringSeconds(LocalDateTime.now());
    }

}