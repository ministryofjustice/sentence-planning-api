package uk.gov.digital.justice.hmpps.sentenceplan.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(produces = MediaType.TEXT_PLAIN_VALUE)
public class PingEndpoint {

    @GetMapping(value = "/ping")
    public ResponseEntity<String> ping() {
        return  ResponseEntity.ok("pong");
    }
}
