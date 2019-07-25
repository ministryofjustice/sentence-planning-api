package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.Motivation;
import uk.gov.digital.justice.hmpps.sentenceplan.service.MotivationService;

import java.util.List;

@Api(tags = {"Sentence Planning API"})

@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class MotivationResource {

    private MotivationService motivationService;

    public MotivationResource(MotivationService motivationService) {
        this.motivationService = motivationService;
    }

    @GetMapping(value = "/motivation", produces = "application/json")
    @ApiOperation(value = "Gets all active Motivations as ref data for use in dropdowns or lists",
            response = Motivation.class, responseContainer="List",
            notes = "Get all active Motivations")
    ResponseEntity<List<Motivation>> getActiveMotivations() {
        return ResponseEntity.ok(motivationService.getActiveMotivations());
    }

}
