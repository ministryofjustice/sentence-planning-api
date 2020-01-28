package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.InterventionRefDto;
import uk.gov.digital.justice.hmpps.sentenceplan.service.InterventionRefService;
import java.util.List;

@Api(tags = {"Intervention Data API"})
@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
public class InterventionRefResource {

    private final InterventionRefService interventionRefService;

    public InterventionRefResource(InterventionRefService interventionRefService) {
        this.interventionRefService = interventionRefService;
    }

    @GetMapping(value = "/interventions", produces = "application/json")
    @ApiOperation(value = "Gets all active Interventions as ref data for use in dropdowns or lists",
            notes = "Get all active Interventions")
    ResponseEntity<List<InterventionRefDto>> getActiveInterventions() {
        return ResponseEntity.ok(
                interventionRefService.getActiveInterventions());
    }

    @GetMapping(value = "/interventions/all", produces = "application/json")
    @ApiOperation(value = "Gets all (including inactive) Interventions as ref data for use in dropdowns or lists",
            notes = "Get all Interventions")
    ResponseEntity<List<InterventionRefDto>> getAllInterventions() {
        return ResponseEntity.ok(
                interventionRefService.getAllInterventions());
    }


    @PostMapping(value = "/interventions", produces = "application/json")
    @ApiOperation(value = "Refresh Intervention Data")
    ResponseEntity refreshInterventions() {
        interventionRefService.refreshInterventions();
        return ResponseEntity.ok().build();
    }
}
