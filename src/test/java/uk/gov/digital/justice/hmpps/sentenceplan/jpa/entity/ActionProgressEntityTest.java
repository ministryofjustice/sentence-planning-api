package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ActionProgressEntityTest {

    private static final ActionStatus status = ActionStatus.IN_PROGRESS;
    private static final LocalDateTime created = LocalDateTime.now();
    private static final String createdBy = "Me";


    @Test
    public void shouldCreateAction() {

        var progressEntity = new ProgressEntity(status, createdBy);

        assertThat(progressEntity.getStatus()).isEqualTo(status);
        assertThat(progressEntity.getCreated()).isEqualToIgnoringSeconds(created);
        assertThat(progressEntity.getCreatedBy()).isEqualTo(createdBy);
    }
}