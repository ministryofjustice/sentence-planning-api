package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.api.*;
import uk.gov.digital.justice.hmpps.sentenceplan.security.AccessLevel;
import uk.gov.digital.justice.hmpps.sentenceplan.security.Authorised;
import uk.gov.digital.justice.hmpps.sentenceplan.service.SentenceBoardReviewService;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Api(tags = {"Sentence Board Review API"})

@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
public class SentenceBoardReviewResource {

    private final SentenceBoardReviewService sentenceBoardReviewService;

    public SentenceBoardReviewResource(SentenceBoardReviewService sentenceBoardReviewService) {
        this.sentenceBoardReviewService = sentenceBoardReviewService;
    }

    @PostMapping(value = "/sentenceplans/{sentencePlanUUID}/reviews", produces = "application/json")
    @ApiOperation(value = "Add a Board Review to a Sentence Plan")
    @Authorised(accessLevel = AccessLevel.WRITE_SENTENCE_PLAN)
    ResponseEntity addReview(@ApiParam(value = "Sentence Plan ID", required = true) @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Review details", required = true, example = "11111111-1111-1111-1111-111111111111") @RequestBody @Valid AddSentenceBoardReviewRequest review) {
        sentenceBoardReviewService.addSentenceBoardReview(
                sentencePlanUUID,
                review.getComments(),
                review.getAttendees(),
                review.getDateOfBoard());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/sentenceplans/{sentencePlanUUID}/reviews", produces = "application/json")
    @ApiOperation(value = "Gets all Sentence Plan Board Reviews")
    @Authorised(accessLevel = AccessLevel.READ_SENTENCE_PLAN)
    ResponseEntity<List<SentenceBoardReviewSummary>> getAllReviews(@ApiParam(value = "Sentence Plan ID", required = true, example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID sentencePlanUUID) {
        return ResponseEntity.ok(SentenceBoardReviewSummary.from(sentenceBoardReviewService.getSentenceBoardReviewsBySentencePlanUUID(
                sentencePlanUUID)));
    }

    @GetMapping(value = "/sentenceplans/{sentencePlanUUID}/reviews/{sentenceBoardReviewUUID}", produces = "application/json")
    @ApiOperation(value = "Get a Sentence Board Review for a Sentence Plan")
    @Authorised(accessLevel = AccessLevel.READ_SENTENCE_PLAN)
    ResponseEntity<SentenceBoardReview> getReview(@ApiParam(value = "Sentence Plan ID", required = true, example = "11111111-1111-1111-1111-111111111111") @PathVariable UUID sentencePlanUUID, @ApiParam(value = "Sentence Board Review ID", required = true) @PathVariable UUID sentenceBoardReviewUUID) {
        return ResponseEntity.ok(SentenceBoardReview.from(sentenceBoardReviewService.getSentenceBoardReviewBySentencePlanUUID(sentencePlanUUID, sentenceBoardReviewUUID)));
    }
}
