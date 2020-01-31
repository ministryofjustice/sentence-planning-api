package uk.gov.digital.justice.hmpps.sentenceplan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import uk.gov.digital.justice.hmpps.sentenceplan.api.CommentDto;
import uk.gov.digital.justice.hmpps.sentenceplan.api.ObjectiveDto;
import uk.gov.digital.justice.hmpps.sentenceplan.api.TimelineDto;
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
    public void createTimelineEntry(UUID sentencePlanUUID, LogEvent type, ObjectiveEntity objectiveEntity) {
        String objective;
        try {
            objective = objectMapper.writeValueAsString(ObjectiveDto.from(objectiveEntity));
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Cant parse Objective to String");
        }
        var timelineEntity = new TimelineEntity(sentencePlanUUID, type, objectiveEntity.getId().toString(), requestData.getUsername(), null, objective);
        timelineRepository.save(timelineEntity);
    }

    @Transactional
    public void createTimelineEntry(UUID sentencePlanUUID, LogEvent type, CommentEntity commentEntity) {
        String comment;
        try {
            comment = objectMapper.writeValueAsString(CommentDto.from(commentEntity));
        } catch (JsonProcessingException e) {
            throw new EntityCreationException("Cant parse Comment to String");
        }
        var timelineEntity = new TimelineEntity(sentencePlanUUID, type, commentEntity.getCommentType().toString(), requestData.getUsername(), comment, null);
        timelineRepository.save(timelineEntity);
    }

    // For when we want SP created and started events
    //@Transactional
    //public void createTimelineEntry(UUID sentencePlanUUID, LogEvent type) {
    //    var timelineEntity = new TimelineEntity(sentencePlanUUID, type, sentencePlanUUID.toString(), requestData.getUsername(), null,null);
    //    timelineRepository.save(timelineEntity);
    //}

    @Transactional
    public List<TimelineDto> getTimelineEntries(UUID sentencePlanUUID, String entityKey){
        var entries = timelineRepository.findBySentencePlanUUIDAndEntityKey(sentencePlanUUID, entityKey);
        return TimelineDto.from(entries, objectMapper);
    }

    @Transactional
    public List<TimelineDto> getTimelineEntries(UUID sentencePlanUUID){
        var entries = timelineRepository.findBySentencePlanUUID(sentencePlanUUID);
        return TimelineDto.from(entries, objectMapper);
    }


}