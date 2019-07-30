package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NeedEntityTest {

    @Test
    public void shouldCreateNeedWithNoMotivations() {
        List<MotivationEntity> motivations = new ArrayList<>();

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isFalse();
        assertThat(need.getMotivationHistory()).hasSize(0);
    }

    @Test
    public void shouldGetOnlyMotivation() {
        var motivation = new MotivationEntity(null, null);

        List<MotivationEntity> motivations = List.of(motivation);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRefUuid()).isEqualTo(motivation.getMotivationRefUuid());
        assertThat(need.getMotivationHistory()).hasSize(0);
    }

    @Test
    public void shouldGetOnlyMotivationButEnded() {
        var motivation = new MotivationEntity(null, null);
        motivation.end();

        List<MotivationEntity> motivations = List.of(motivation);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isFalse();
        assertThat(need.getMotivationHistory()).hasSize(1);
    }

    @Test
    public void shouldGetAllMotivations() {
        var motivation1 = new MotivationEntity(null, null);
        motivation1.end();
        var motivation2 = new MotivationEntity(null, null);

        List<MotivationEntity> motivations = List.of(motivation1,motivation2);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRefUuid()).isEqualTo(motivation2.getMotivationRefUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);
    }

    @Test
    public void shouldUpdateMotivations() {
        var motivation1 = new MotivationEntity(null, UUID.randomUUID());
        motivation1.end();
        var motivation2 = new MotivationEntity(null, UUID.randomUUID());

        // list.of is immutable;
        List<MotivationEntity> motivations = new ArrayList<>();
        motivations.add(motivation1);
        motivations.add(motivation2);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRefUuid()).isEqualTo(motivation2.getMotivationRefUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

        var motivation3 = new MotivationEntity(null, UUID.randomUUID());
        NeedEntity.updateMotivation(need, motivation3.getMotivationRefUuid());

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRefUuid()).isEqualTo(motivation3.getMotivationRefUuid());
        assertThat(need.getMotivationHistory()).hasSize(2);

    }

    @Test
    public void shouldUpdateMotivationsEmpty() {

        var need = new NeedEntity();

        var motivation3 = new MotivationEntity(null, UUID.randomUUID());
        NeedEntity.updateMotivation(need, motivation3.getMotivationRefUuid());

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRefUuid()).isEqualTo(motivation3.getMotivationRefUuid());
        assertThat(need.getMotivationHistory()).hasSize(0);

    }


    @Test
    public void shouldUpdateMotivationsSame() {
        var motivation1 = new MotivationEntity(null, UUID.randomUUID());
        motivation1.end();
        var motivation2 = new MotivationEntity(null, UUID.randomUUID());

        // list.of is immutable;
        List<MotivationEntity> motivations = new ArrayList<>();
        motivations.add(motivation1);
        motivations.add(motivation2);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRefUuid()).isEqualTo(motivation2.getMotivationRefUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

        NeedEntity.updateMotivation(need, motivation2.getMotivationRefUuid());

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRefUuid()).isEqualTo(motivation2.getMotivationRefUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

    }
}