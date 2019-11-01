package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysSentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.service.SentencePlanService;

import javax.validation.Valid;
import java.util.Map;
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

    @PostMapping(value = "/sentenceplan", produces = "application/json")
    @ApiOperation(value = "Create new sentence plan",
            notes = "Creates a new sentence plan")
    ResponseEntity<UUID> createSentencePlan(@ApiParam(value = "Offender details", required = true) @RequestBody @Valid CreateSentencePlanRequest createSentencePlanRequest) {
        UUID sentencePlanUUID = sentencePlanService.createSentencePlan(
                createSentencePlanRequest.getOffenderId(),
                createSentencePlanRequest.getOffenderReferenceType());
        return ResponseEntity.ok(sentencePlanUUID);
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
    ResponseEntity<List<SentencePlanSummary>> getSentencePlansForOffender(@ApiParam(value = "OASys Offender ID") @PathVariable("offenderId") Long oasysOffenderId) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlansForOffender(oasysOffenderId));
    }

    @GetMapping(value = "/offender/{offenderId}/sentenceplan/{sentencePlanId}", produces = "application/json")
    @ApiOperation(value = "Gets an Oasys Sentence Plan from its ID",
            response = SentencePlan.class,
            notes = "Request legacy sentence plan")
    ResponseEntity<OasysSentencePlan> getOASysSentencePlan(@ApiParam(value = "Oasys Offender ID") @PathVariable("offenderId") Long oasysOffenderId, @ApiParam(value = "Oasys Sentence Plan ID") @PathVariable("sentencePlanId") String sentencePlanId) {
        return ResponseEntity.ok(sentencePlanService.getLegacySentencePlan(oasysOffenderId, sentencePlanId));
    }

    @PutMapping(value = "/sentenceplan/{sentencePlanUUID}/comments", produces = "application/json")
    @ApiOperation(value = "Add comments",
            notes = "Add comments of various type to the Sentence Plan")
    ResponseEntity addComments(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @RequestBody List<AddCommentRequest> comments) {
        sentencePlanService.addSentencePlanComments(sentencePlanUUID, comments);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}/comments", produces = "application/json")
    @ApiOperation(value = "Get comments",
            notes = "Service user and practitioner comments")
    ResponseEntity<Map<CommentType, Comment>> getComments(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanComments(sentencePlanUUID));
    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}/needs", produces = "application/json")
    @ApiOperation(value = "Get Sentence Plan needs from ID",
            response = Action.class,
            responseContainer = "List",
            notes = "Request sentence plan needs")
    ResponseEntity<List<Need>> getSentencePlanNeeds(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanNeeds(sentencePlanUUID));
    }

    //@PostMapping(value = "/sentenceplan/{sentencePlanUUID}/objective", produces = "application/json")

    //@GetMapping(value = "/sentenceplan/{sentencePlanUUID}/objective", produces = "application/json")


    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/actions", produces = "application/json")
    @ApiOperation(value = "Add an Action to a sentence plan",
            notes = "Creates a draft new sentence plan")
    ResponseEntity addAction(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Action details", required = true) @RequestBody @Valid AddSentencePlanAction action) {
         sentencePlanService.addAction(sentencePlanUUID,
                action.getInterventionUUID(),
                action.getDescription(),
                action.getTargetDate(),
                action.getMotivationUUID(),
                action.getOwner(),
                action.getOwnerOther(),
                action.getStatus()
         );
        return ResponseEntity.ok().build();

    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}/actions", produces = "application/json")
    @ApiOperation(value = "Get Sentence Plan actions from ID",
            response = Action.class,
            responseContainer = "List",
            notes = "Request sentence plan actions")
        ResponseEntity<List<Action>> getSentencePlanActions(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanActions(sentencePlanUUID));
    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}/actions/{actionId}", produces = "application/json")
    @ApiOperation(value = "Get Sentence Plan action from ID",
            response = Action.class,
            notes = "Request a single sentence plan action")
    ResponseEntity<Action> getSentencePlanAction(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Action ID") @PathVariable UUID actionId) {
        return ResponseEntity.ok(sentencePlanService.getSentencePlanAction(sentencePlanUUID, actionId));
    }

    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/actions/priority", produces = "application/json")
    @ApiOperation(value = "Set the priorities of actions on a Sentence Plan",
            notes = "Set Priority")
    ResponseEntity<List<UpdateActionPriorityRequest>> updateActionPriority(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @RequestBody List<UpdateActionPriorityRequest> request) {
        var actions = request.stream().collect(Collectors.toMap(UpdateActionPriorityRequest::getActionUUID, UpdateActionPriorityRequest::getPriority));
        sentencePlanService.updateActionPriorities(sentencePlanUUID, actions);
        return ResponseEntity.ok(request);
    }

    @PostMapping(value = "/sentenceplan/{sentencePlanUUID}/actions/{actionId}/progress", produces = "application/json")
    @ApiOperation(value = "Progress a action",
            notes = "Progress an Action")
    ResponseEntity progressAction(@ApiParam(value = "Sentence Plan ID") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Action ID") @PathVariable UUID actionId, @RequestBody ProgressActionRequest request) {
       // sentencePlanService.progressAction(sentencePlanUUID, actionId, request.getStatus(), request.g);
        return ResponseEntity.ok().build();
    }

}
