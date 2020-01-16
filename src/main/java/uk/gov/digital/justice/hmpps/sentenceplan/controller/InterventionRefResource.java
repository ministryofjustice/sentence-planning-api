package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.InterventionRef;
import uk.gov.digital.justice.hmpps.sentenceplan.service.InterventionRefService;
import java.util.List;
import java.util.stream.Collectors;

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
            response = InterventionRef.class, responseContainer="List",
            notes = "Get all active Interventions")
    ResponseEntity<List<InterventionRef>> getActiveInterventions() {
        return ResponseEntity.ok(
                interventionRefService.getActiveInterventions().stream()
                        .map(InterventionRef::from).collect(Collectors.toList()));
    }

    @GetMapping(value = "/interventions/all", produces = "application/json")
    @ApiOperation(value = "Gets all (including inactive) Interventions as ref data for use in dropdowns or lists",
            response = InterventionRef.class, responseContainer="List",
            notes = "Get all Interventions")
    ResponseEntity<List<InterventionRef>> getAllInterventions() {
        return ResponseEntity.ok(
                interventionRefService.getAllInterventions().stream()
                        .map(InterventionRef::from).collect(Collectors.toList()));
    }


    @PostMapping(value = "/interventions", produces = "application/json")
    @ApiOperation(value = "Refresh Intervention Data")
    ResponseEntity refreshInterventions() {
        interventionRefService.refreshInterventions();
        return ResponseEntity.ok().build();
    }
}
