package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MotivationEntityTest {

    @Test
    public void shouldCreateMotivation() {
        UUID needUUID = UUID.randomUUID();
        UUID motivationUUID = UUID.randomUUID();

        var motivation = new MotivationEntity(needUUID,motivationUUID);

        assertThat(motivation.getUuid()).isNotNull();
        assertThat(motivation.getNeedUuid()).isEqualTo(needUUID);
        assertThat(motivation.getMotivationRefUuid()).isEqualTo(motivationUUID);
        assertThat(motivation.getStart()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(motivation.getEnd()).isNull();
        assertThat(motivation.isEnded()).isFalse();

    }

    @Test
    public void shouldEndMotivation() {
        UUID needUUID = UUID.randomUUID();
        UUID motivationUUID = UUID.randomUUID();

        var motivation = new MotivationEntity(needUUID, motivationUUID);

        assertThat(motivation.getEnd()).isNull();
        assertThat(motivation.isEnded()).isFalse();

        motivation.end();

        assertThat(motivation.getEnd()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(motivation.isEnded()).isTrue();

    }
}