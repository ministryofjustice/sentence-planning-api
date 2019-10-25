package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.api.EventType;
import uk.gov.digital.justice.hmpps.sentenceplan.api.PlanStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class SentencePlanEntityTest {

    @Test
public void shouldCreateDraftSentencePlan() {
    var offender = mock(OffenderEntity.class);
    var sentencePlan = new SentencePlanEntity(offender);

    assertThat(sentencePlan.getUuid()).isNotNull();
    assertThat(sentencePlan.getCreatedOn()).isEqualToIgnoringSeconds(LocalDateTime.now());
    assertThat(sentencePlan.getStartDate()).isEqualToIgnoringSeconds(LocalDateTime.now());
    assertThat(sentencePlan.getEventType()).isEqualTo(EventType.CREATED);
    assertThat(sentencePlan.getStatus()).isEqualTo(PlanStatus.DRAFT);
    assertThat(sentencePlan.getData().getActions()).isEmpty();
    assertThat(sentencePlan.getData().getComments()).isEmpty();
    assertThat(sentencePlan.getData().getChildSafeguardingIndicated()).isNull();
    assertThat(sentencePlan.getData().getComplyWithChildProtectionPlanIndicated()).isNull();
    assertThat(sentencePlan.getNeeds()).isEmpty();
}
}