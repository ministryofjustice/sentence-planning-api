package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ActionStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ActionProgressEntityTest {

    private static final ActionStatus status = ActionStatus.IN_PROGRESS;
    private static final LocalDateTime created = LocalDateTime.of(2019,6, 1, 9,0);
    private static final UUID motivation = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final YearMonth targetDate = YearMonth.of(2019,9);
    private static final String comment = "a comment";
    private static final String createdBy = "Me";


    @Test
    public void shouldCreateAction() {

        var progressEntity = new ProgressEntity(status, targetDate, motivation,comment,created,createdBy);

        assertThat(progressEntity.getStatus()).isEqualTo(status);
        assertThat(progressEntity.getCreated()).isEqualToIgnoringSeconds(created);
        assertThat(progressEntity.getComment()).isEqualToIgnoringCase(comment);
        assertThat(progressEntity.getTargetDate()).isEqualTo(targetDate);
        assertThat(progressEntity.getMotivationUUID()).isEqualTo(motivation);
        assertThat(progressEntity.getCreatedBy()).isEqualTo(createdBy);
    }
}