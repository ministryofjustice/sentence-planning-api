package uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MotivationRefEntityTest {

    @Test
    public void shouldCreateMotivationRef() {
        String motivationText = "Motivation_Text";
        String friendlyText = "Friendly_Text";

        var motivation = new MotivationRefEntity(motivationText, friendlyText);

        assertThat(motivation.getUuid()).isNotNull();
        assertThat(motivation.getMotivationText()).isEqualTo(motivationText);
        assertThat(motivation.getFriendlyText()).isEqualTo(friendlyText);
        assertThat(motivation.getCreated()).isEqualToIgnoringSeconds(LocalDateTime.now());
        assertThat(motivation.getDeleted()).isNull();

    }
}