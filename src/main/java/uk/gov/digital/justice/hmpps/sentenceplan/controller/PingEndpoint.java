package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PingEndpoint {

    @GetMapping(value = "/ping", produces = "application/json")
    public ResponseEntity<String> ping() {
        return  ResponseEntity.ok("pong");
    }
}
