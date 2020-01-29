package uk.gov.digital.justice.hmpps.sentenceplan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.repository.TimelineRepository;
import uk.gov.digital.justice.hmpps.sentenceplan.jpa.entity.*;
import uk.gov.digital.justice.hmpps.sentenceplan.service.exceptions.EntityCreationException;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
public class TimelineService {
    private final TimelineRepository timelineRepository;
    private final RequestData requestData;
    private final ObjectMapper objectMapper;


    public TimelineService(TimelineRepository timelineRepository, RequestData requestData, ObjectMapper objectMapper) {
        this.timelineRepository = timelineRepository;
        this.requestData = requestData;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createTimelineEntry(UUID sentencePlanUUID, LogEvent type, ObjectiveEntity to) {
        String objective;
        try {
            objective = objectMapper.writeValueAsString(to);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Cant parse Objective to String");
        }
        var timelineEntity = new TimelineEntity(sentencePlanUUID, type, requestData.getUsername(), objective);
        timelineRepository.save(timelineEntity);
    }

    @Transactional
    public void createTimelineEntry(UUID sentencePlanUUID, LogEvent type, CommentEntity to) {
        String objective;
        try {
            objective = objectMapper.writeValueAsString(to);
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Cant parse Comment to String");
        }
        var timelineEntity = new TimelineEntity(sentencePlanUUID, type, requestData.getUsername(), objective);
        timelineRepository.save(timelineEntity);
    }

    @Transactional
    public void createTimelineEntry(UUID sentencePlanUUID, LogEvent type) {
        var timelineEntity = new TimelineEntity(sentencePlanUUID, type, requestData.getUsername(), null);
        timelineRepository.save(timelineEntity);
    }


}