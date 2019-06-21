package uk.gov.digital.justice.hmpps.sentenceplan.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OASYSAssessmentAPIClient {

    private RestTemplate restTemplate;

    public OASYSAssessmentAPIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
