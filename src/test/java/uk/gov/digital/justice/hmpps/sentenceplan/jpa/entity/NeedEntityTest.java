package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class NeedEntityTest {


    private List<MotivationRefEntity> motivationsRefs;

    @Before
    public void setup() {
        motivationsRefs = List.of(MotivationRefEntity.builder().friendlyText("motivation 1").motivationText("motivations 1").uuid(UUID.randomUUID()).build(),
                MotivationRefEntity.builder().friendlyText("motivation 2").motivationText("motivations 2").uuid(UUID.randomUUID()).build(),
                MotivationRefEntity.builder().friendlyText("motivation 3").motivationText("motivations 3").uuid(UUID.randomUUID()).build());
    }

    @Test
    public void shouldCreateNeedWithNoMotivations() {
        List<MotivationEntity> motivations = new ArrayList<>();

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isFalse();
        assertThat(need.getMotivationHistory()).hasSize(0);
    }

    @Test
    public void shouldGetOnlyMotivation() {
        var motivation = new MotivationEntity(null, motivationsRefs.get(0));

        var motivations = List.of(motivation);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(0);
    }

    @Test
    public void shouldGetOnlyMotivationButEnded() {
        var motivation = new MotivationEntity(null, motivationsRefs.get(0));
        motivation.end();

        var motivations = List.of(motivation);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isFalse();
        assertThat(need.getMotivationHistory()).hasSize(1);
    }

    @Test
    public void shouldGetAllMotivations() {
        var motivation1 = new MotivationEntity(null, motivationsRefs.get(0));
        motivation1.end();
        var motivation2 = new MotivationEntity(null, motivationsRefs.get(1));

        var motivations = List.of(motivation1,motivation2);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation2.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);
    }

    // New one.
    @Test
    public void shouldUpdateMotivationsNew() {

        // list.of is immutable;
        List<MotivationEntity> motivations = new ArrayList<>();

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isFalse();
        assertThat(need.getMotivationHistory()).hasSize(0);

        var motivation1 = new MotivationEntity(null, motivationsRefs.get(0));
        NeedEntity.updateMotivation(need, motivation1.getMotivationRef().getUuid(), motivationsRefs);


        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation1.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(0);

    }

    // Existing one.
    @Test
    public void shouldNotUpdateMotivationsExisting() {
        var motivation1 = new MotivationEntity(null,motivationsRefs.get(0));
        motivation1.end();
        var motivation2 = new MotivationEntity(null, motivationsRefs.get(1));

        // list.of is immutable;
        List<MotivationEntity> motivations = new ArrayList<>();
        motivations.add(motivation1);
        motivations.add(motivation2);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation2.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

        NeedEntity.updateMotivation(need, motivation2.getMotivationRef().getUuid(), motivationsRefs);

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation2.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

    }

    // Replace one.
    @Test
    public void shouldUpdateMotivationsReplace() {
        var motivation1 = new MotivationEntity(null, motivationsRefs.get(0));
        motivation1.end();
        var motivation2 = new MotivationEntity(null,motivationsRefs.get(1));

        // list.of is immutable;
        List<MotivationEntity> motivations = new ArrayList<>();
        motivations.add(motivation1);
        motivations.add(motivation2);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation2.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

        var motivation3 = new MotivationEntity(null, motivationsRefs.get(2));
        NeedEntity.updateMotivation(need, motivation3.getMotivationRef().getUuid(), motivationsRefs);

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation3.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(2);

    }

    @Test
    public void shouldNotAddMotivationsEmpty() {

        var need = new NeedEntity();

        var motivation3 = new MotivationEntity(null, motivationsRefs.get(0));
        NeedEntity.updateMotivation(need, motivation3.getMotivationRef().getUuid(), motivationsRefs);

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation3.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(0);

    }


    @Test
    public void shouldNotAddMotivationsSame() {
        var motivation1 = new MotivationEntity(null, motivationsRefs.get(0));
        motivation1.end();
        var motivation2 = new MotivationEntity(null,motivationsRefs.get(1));

        // list.of is immutable;
        List<MotivationEntity> motivations = new ArrayList<>();
        motivations.add(motivation1);
        motivations.add(motivation2);

        var need = NeedEntity.builder().motivations(motivations).build();

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation2.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

        NeedEntity.updateMotivation(need, motivation2.getMotivationRef().getUuid(), motivationsRefs);

        assertThat(need.getCurrentMotivation().isPresent()).isTrue();
        assertThat(need.getCurrentMotivation().get().getMotivationRef().getUuid()).isEqualTo(motivation2.getMotivationRef().getUuid());
        assertThat(need.getMotivationHistory()).hasSize(1);

    }
}