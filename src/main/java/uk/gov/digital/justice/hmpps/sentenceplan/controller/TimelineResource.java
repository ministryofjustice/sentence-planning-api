package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.justice.hmpps.sentenceplan.api.TimelineDto;
import uk.gov.digital.justice.hmpps.sentenceplan.service.TimelineService;

import java.util.Collection;
import java.util.UUID;

@Api(tags = {"Timeline Data API"})
@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
public class TimelineResource {

    private final TimelineService timelineService;

    public TimelineResource(TimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping(value = "/timeline/sentenceplans/{sentencePlanUUID}/entity/{entityKey}", produces = "application/json")
    @ApiOperation(value = "Gets all timeline data for part of a sentence plan.")
    ResponseEntity<Collection<TimelineDto>> getActiveMotivations(@ApiParam(value = "Sentence Plan ID", required = true, example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID sentencePlanUUID,
                                                                 @ApiParam(value = "Entity Id", required = true, example = "COMMENT_TYPE") @PathVariable String entityKey) {
        return ResponseEntity.ok(timelineService.getTimelineEntries(sentencePlanUUID, entityKey));
    }

}
