package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.service.SentencePlanService;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;

@Api(tags = {"Sentence Planning API"})

@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class SentencePlanResource {

    private SentencePlanService sentencePlanService;

    public SentencePlanResource(SentencePlanService sentencePlanService) {
        this.sentencePlanService = sentencePlanService;
    }


    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}", produces = "application/json")
    @ApiOperation(value = "Gets a Sentence Plan from it's ID",
            response = SentencePlan.class,
            notes = "Request sentence plan")
    ResponseEntity<SentencePlan> getSentencePlan(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanFromUuid(sentencePlanUUID));
    }

    @GetMapping(value = "/offender/{offenderId}/sentenceplans", produces = "application/json")
    @ApiOperation(value = "Gets a list of Sentence Plans for an Offender",
            response = SentencePlan.class,
            notes = "Request sentence plans for offender. Includes both new and OASYs sentence plans")
    ResponseEntity<List<SentencePlanSummary>> getSentencePlansForOffender(@ApiParam(value = "Offender ID") @PathVariable("offenderId") Long oasysOffenderId) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlansForOffender(oasysOffenderId));
    }

    @PostMapping(value = "/sentenceplan", produces = "application/json")
    @ApiOperation(value = "Create new sentence plan",
            notes = "Creates a new sentence plan")
    ResponseEntity<SentencePlan> createSentencePlan(@ApiParam(value = "Offender details", required = true) @RequestBody @Valid CreateSentencePlanRequest createSentencePlanRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sentencePlanService.createSentencePlan(
                createSentencePlanRequest.getOffenderId(),
                createSentencePlanRequest.getOffenderReferenceType()));
    }

    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/steps", produces = "application/json")
    @ApiOperation(value = "Add a step to a sentence plan",
            notes = "Creates a draft new sentence plan")
    ResponseEntity<List<Step>> addStep(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Step details", required = true) @RequestBody @Valid AddSentencePlanStep step) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                sentencePlanService.addStep(sentencePlanUUID,
                step.getOwner(),
                step.getOwnerOther(),
                step.getStrength(),
                step.getDescription(),
                step.getIntervention(),
                step.getNeeds()));
    }

    @PutMapping(value = "/sentenceplan/{sentencePlanUUID}/steps/{stepId}", produces = "application/json")
    @ApiOperation(value = "Update Sentence Plan step from ID",
            notes = "Update a single sentence plan step")
    ResponseEntity updateSentencePlanStep(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Step ID") @PathVariable UUID stepId, @ApiParam(value = "Step details", required = true) @RequestBody @Valid UpdateSentencePlanStepRequest step) {
        sentencePlanService.updateStep(sentencePlanUUID, stepId,
                step.getOwner(),
                step.getOwnerOther(),
                step.getStrength(),
                step.getDescription(),
                step.getIntervention(),
                step.getNeeds(),
                step.getStatus()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}/steps", produces = "application/json")
    @ApiOperation(value = "Get Sentence Plan steps from ID",
            response = Step.class,
            responseContainer = "List",
            notes = "Request sentence plan steps")
        ResponseEntity<List<Step>> getSentencePlanSteps(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanSteps(sentencePlanUUID));
    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}/steps/{stepId}", produces = "application/json")
    @ApiOperation(value = "Get Sentence Plan step from ID",
            response = Step.class,
            notes = "Request a single sentence plan step")
    ResponseEntity<Step> getSentencePlanStep(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Step ID") @PathVariable UUID stepId) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanStep(sentencePlanUUID, stepId));
    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}/needs", produces = "application/json")
    @ApiOperation(value = "Get Sentence Plan needs from ID",
            response = Step.class,
            responseContainer = "List",
            notes = "Request sentence plan needs")
    ResponseEntity<List<Need>> getSentencePlanNeeds(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanNeeds(sentencePlanUUID));
    }

    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/motivations", produces = "application/json")
    @ApiOperation(value = "Update the Motivations against Needs on a Sentence Plan",
            notes = "Update Needs")
    ResponseEntity updateMotivations(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @RequestBody List<AssociateMotivationNeedRequest> request) {
        Map<UUID,UUID> needs = request.stream().collect(Collectors.toMap(AssociateMotivationNeedRequest::getNeedUUID, AssociateMotivationNeedRequest::getMotivationUUID));
        sentencePlanService.updateMotivations(sentencePlanUUID,needs);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/steps/priority", produces = "application/json")
    @ApiOperation(value = "Set the priorities of steps on a Sentence Plan",
            notes = "Set Priority")
    ResponseEntity<List<UpdateStepPriorityRequest>> updateStepPriority(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @RequestBody List<UpdateStepPriorityRequest> request) {
        Map<UUID,Integer> steps = request.stream().collect(Collectors.toMap(UpdateStepPriorityRequest::getStepUUID, UpdateStepPriorityRequest::getPriority));
        sentencePlanService.updateStepPriorities(sentencePlanUUID, steps);
        return ResponseEntity.ok(request);
    }

    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/steps/{stepId}/progress", produces = "application/json")
    @ApiOperation(value = "Progress a step",
            notes = "Progress a Step")
    ResponseEntity<ProgressStepRequest> progressStep(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Step ID") @PathVariable UUID stepId, @RequestBody ProgressStepRequest request) {
        sentencePlanService.progressStep(sentencePlanUUID, stepId, request.getStatus(), request.getPractitionerComments());
        return ResponseEntity.ok(request);
    }

    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/serviceUserComments", produces = "application/json")
    @ApiOperation(value = "Set the value of the service user comments",
            notes = "Set service user comments")
    ResponseEntity setServiceUserComments(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @RequestBody String serviceUserComment) {
        sentencePlanService.setServiceUserComments(sentencePlanUUID, serviceUserComment);
        return ResponseEntity.ok().build();
    }


}
