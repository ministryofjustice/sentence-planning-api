package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.justice.hmpps.sentenceplan.client.dto.OasysOffender;
import uk.gov.digital.justice.hmpps.sentenceplan.security.AccessLevel;
import uk.gov.digital.justice.hmpps.sentenceplan.security.Authorised;
import uk.gov.digital.justice.hmpps.sentenceplan.service.OffenderService;

@Api(tags = {"Offender API"})
@RestController
@RequestMapping(
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class OffenderResource {

    private final OffenderService offenderService;

    public OffenderResource(OffenderService offenderService) {
        this.offenderService = offenderService;
    }

    @GetMapping(value = "/offenders/oasysOffenderId/{offenderId}", produces = "application/json")
    @ApiOperation(value = "Get an OASys Offender by Id",
            notes = "Temporary endpoint to allow authentication of offender calls")
    @Authorised(accessLevel = AccessLevel.READ_SENTENCE_PLAN)
    ResponseEntity<OasysOffender> getOASysSentencePlan(@ApiParam(value = "OASys Offender ID", required = true, example = "123456") @PathVariable("offenderId") Long oasysOffenderId) {
        return ResponseEntity.ok(offenderService.getOasysOffenderFromOasys(oasysOffenderId));
    }
}
