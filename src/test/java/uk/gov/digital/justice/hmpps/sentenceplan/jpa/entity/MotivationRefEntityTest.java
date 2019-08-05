package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MotivationRefEntityTest {

    @Test
    public void shouldCreateMotivation() {
        UUID needUUID = UUID.randomUUID();
        var needEntity = NeedEntity.builder().uuid(needUUID).build();
        UUID motivationUUID = UUID.randomUUID();
        var motivationEntity = MotivationRefEntity.builder()
                .motivationText("test")
                .friendlyText("test")
                .uuid(motivationUUID).build();

        var motivation = new MotivationEntity(needEntity,motivationEntity);

        assertThat(motivation.getNeed().getUuid()).isEqualTo(needUUID);
        assertThat(motivation.getMotivationRef().getUuid()).isEqualTo(motivationUUID);
        assertThat(motivation.getStart()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(motivation.getEnd()).isNull();
        assertThat(motivation.isEnded()).isFalse();

    }

    @Test
    public void shouldEndMotivation() {
        UUID needUUID = UUID.randomUUID();
        var needEntity = NeedEntity.builder().uuid(needUUID).build();

        UUID motivationUUID = UUID.randomUUID();
        var motivationEntity = MotivationRefEntity.builder()
                .motivationText("test")
                .friendlyText("test")
                .uuid(motivationUUID).build();

        var motivation = new MotivationEntity(needEntity, motivationEntity);

        assertThat(motivation.getEnd()).isNull();
        assertThat(motivation.isEnded()).isFalse();

        motivation.end();

        assertThat(motivation.getEnd()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(motivation.isEnded()).isTrue();

    }
}