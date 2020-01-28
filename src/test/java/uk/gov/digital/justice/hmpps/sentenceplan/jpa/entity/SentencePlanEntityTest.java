package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class SentencePlanEntityTest {

    @Test
    public void shouldCreateDraftSentencePlan() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        assertThat(sentencePlan.getUuid()).isNotNull();
        assertThat(sentencePlan.getCreatedOn()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(sentencePlan.getData().getObjectives()).isEmpty();
        assertThat(sentencePlan.getData().getComments()).isEmpty();
        assertThat(sentencePlan.getData().getChildSafeguardingIndicated()).isNull();
        assertThat(sentencePlan.getNeeds()).isEmpty();
        assertThat(sentencePlan.getStartedDate()).isNull();
    }

    @Test
    public void shouldStartThePlan() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        assertThat(sentencePlan.getStartedDate()).isNull();
        sentencePlan.start();
        assertThat(sentencePlan.getStartedDate()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    public void shouldBeDraftWhenStartedDateNull() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        assertThat(sentencePlan.getStartedDate()).isNull();
        assertThat(sentencePlan.isDraft()).isTrue();

        sentencePlan.start();

        assertThat(sentencePlan.isDraft()).isFalse();
    }

    @Test
    public void shouldEndThePlan() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        assertThat(sentencePlan.getCompletedDate()).isNull();
        sentencePlan.end();
        assertThat(sentencePlan.getCompletedDate()).isCloseTo(LocalDateTime.now(), within(5, ChronoUnit.SECONDS));
    }

    @Test
    public void shouldUpdateNeedsWhenNoCurrentNeeds() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);
        var needUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newNeeds = List.of(NeedEntity.builder().description("new Need 1")
        .uuid(needUUID).build());

        assertThat(sentencePlan.getNeeds()).isEmpty();
        sentencePlan.updateNeeds(newNeeds);
        assertThat(sentencePlan.getNeeds()).hasSize(1);
        assertThat(sentencePlan.getNeeds().get(0).getDescription()).isEqualTo("new Need 1");
        assertThat(sentencePlan.getNeeds().get(0).getUuid()).isEqualTo(needUUID);
    }

    @Test
    public void shouldAddNewNeedsToExistingNeeds() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        var existingNeedUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newNeedUUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

        var existingNeed = NeedEntity.builder().description("Need 1")
                .uuid(existingNeedUUID).build();
        var existingNeeds = new ArrayList<NeedEntity>();
        existingNeeds.add(existingNeed);

        sentencePlan.setNeeds(existingNeeds);

        //add existing need and new need
        var newNeeds = List.of(existingNeed, NeedEntity.builder().description("Need 2")
                .uuid(newNeedUUID).build());

        assertThat(sentencePlan.getNeeds()).hasSize(1);
        sentencePlan.updateNeeds(newNeeds);
        assertThat(sentencePlan.getNeeds()).hasSize(2);
        assertThat(sentencePlan.getNeeds()).extracting("description").containsOnly("Need 1", "Need 2");
    }


    @Test
    public void shouldMakeMissingNeedsInActive() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        var existingNeedUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newNeedUUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

        var existingNeed = NeedEntity.builder().description("Need 1")
                .uuid(existingNeedUUID).build();
        var existingNeeds = new ArrayList<NeedEntity>();
        existingNeeds.add(existingNeed);

        sentencePlan.setNeeds(existingNeeds);

        //add new need but not existing neeed
        var newNeeds = List.of(NeedEntity.builder().description("Need 2")
                .uuid(newNeedUUID).build());

        sentencePlan.updateNeeds(newNeeds);
        var existingUpdatedNeed = sentencePlan.getNeeds().stream().filter(n -> n.getUuid().equals(existingNeedUUID)).findAny();
        assertThat(existingUpdatedNeed.get().getActive()).isFalse();
    }

    @Test
    public void shouldMakeNewNeedsActive() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        var existingNeedUUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var newNeedUUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

        var existingNeed = NeedEntity.builder().description("Need 1")
                .uuid(existingNeedUUID).build();
        var existingNeeds = new ArrayList<NeedEntity>();
        existingNeeds.add(existingNeed);

        sentencePlan.setNeeds(existingNeeds);

        var newNeeds = List.of(NeedEntity.builder().description("Need 2")
                .uuid(newNeedUUID).build());

        sentencePlan.updateNeeds(newNeeds);
        var existingUpdatedNeed = sentencePlan.getNeeds().stream().filter(n -> n.getUuid().equals(newNeedUUID)).findAny();
        assertThat(existingUpdatedNeed.get().getActive()).isTrue();
    }

    @Test
    public void shouldAddObjectiveWithLowestPriority() {
        var offender = mock(OffenderEntity.class);
        var sentencePlan = new SentencePlanEntity(offender);

        var objective1 = new ObjectiveEntity("Objective 1", Collections.emptyList(), true);
        var objective2 = new ObjectiveEntity("Objective 2", Collections.emptyList(), true);

        sentencePlan.addObjective(objective1);
        sentencePlan.addObjective(objective2);

        var firstResult = sentencePlan.getObjectives().values().stream().filter(o ->o.getDescription().equals("Objective 1")).findAny();
        var secondResult = sentencePlan.getObjectives().values().stream().filter(o ->o.getDescription().equals("Objective 2")).findAny();

        assertThat(firstResult.get().getPriority()).isEqualTo(0);
        assertThat(secondResult.get().getPriority()).isEqualTo(1);
    }
}