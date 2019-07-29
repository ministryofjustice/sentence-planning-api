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
        assertThat(need.getCurrentMotivation().get().getUuid()).isEqualTo(motivation.getUuid());
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
        assertThat(need.getCurrentMotivation().get().getUuid()).isEqualTo(motivation2.getUuid());

        assertThat(need.getMotivationHistory()).hasSize(1);
    }
}