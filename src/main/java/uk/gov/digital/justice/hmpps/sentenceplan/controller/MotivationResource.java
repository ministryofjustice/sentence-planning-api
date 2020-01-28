package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.MotivationRefDto;
import uk.gov.digital.justice.hmpps.sentenceplan.service.MotivationRefService;

import java.util.List;

@Api(tags = {"Motivation Data API"})
@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
public class MotivationResource {

    private final MotivationRefService motivationRefService;

    public MotivationResource(MotivationRefService motivationRefService) {
        this.motivationRefService = motivationRefService;
    }

    @GetMapping(value = "/motivation", produces = "application/json")
    @ApiOperation(value = "Gets all active Motivations as ref data for use in dropdowns or lists",
            response = MotivationRefDto.class, responseContainer="List",
            notes = "Get all active Motivations")
    ResponseEntity<List<MotivationRefDto>> getActiveMotivations() {
        return ResponseEntity.ok(MotivationRefDto.from(motivationRefService.getActiveMotivations()));
    }

}
