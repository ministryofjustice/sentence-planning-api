package uk.gov.digital.justice.hmpps.sentenceplan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.TimelineRepository;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimelineServiceTest {

    @Mock
    TimelineRepository timelineRepository;

    @Mock
    private RequestData requestData;

    @Mock
    private ObjectMapper objectMapper;

    TimelineService timelineService;


    @Before
    public void setup() {

        timelineService = new TimelineService(timelineRepository, requestData, objectMapper);
       // when(requestData.getUsername()).thenReturn("a user");
    }

    @Test
    public void shouldCreateTimelineEntry() {

    }

}