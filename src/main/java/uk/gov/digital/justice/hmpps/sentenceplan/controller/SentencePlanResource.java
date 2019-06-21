package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.justice.hmpps.sentenceplan.api.SentencePlan;
import uk.gov.digital.justice.hmpps.sentenceplan.service.SentencePlanService;

import java.time.LocalDateTime;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.EVENT;
import static uk.gov.digital.justice.hmpps.sentenceplan.application.LogEvent.SENTENCE_PLAN_RETRIEVED;

@RestController
@Slf4j
public class SentencePlanResource {

    private SentencePlanService sentencePlanService;

    public SentencePlanResource(SentencePlanService sentencePlanService) {
        this.sentencePlanService = sentencePlanService;
    }

    @GetMapping(value = "/sentenceplan/{sentencePlanUUID}")
    ResponseEntity<SentencePlan> getCase(@PathVariable UUID sentencePlanUUID) {
        log.info("Retrieving Sentence Plan {}", sentencePlanUUID, value(EVENT,SENTENCE_PLAN_RETRIEVED));
        return ResponseEntity.ok(new SentencePlan(LocalDateTime.now()));

    }

}
