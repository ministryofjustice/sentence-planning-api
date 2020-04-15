package uk.gov.digital.justice.hmpps.sentenceplan.client;

import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import uk.gov.digital.justice.hmpps.sentenceplan.application.RequestData;

@Component
public class RetryingOauth2RestTemplate {

    @Getter
    private final OAuth2RestTemplate restTemplate;
    private final RequestData requestData;

    public RetryingOauth2RestTemplate(OAuth2RestTemplate restTemplate, RequestData requestData){
        this.restTemplate = restTemplate;
        this.requestData = requestData;
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <R> ResponseEntity<R> get(String url, Class<R> responseType){
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, createHeaders()), responseType);
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    public <R> ResponseEntity<R> get(String url, ParameterizedTypeReference<R> responseType){
        return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(null, createHeaders()), responseType);
    }


    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(RequestData.CORRELATION_ID_HEADER, requestData.getCorrelationId());
        headers.add(RequestData.USERNAME_HEADER, requestData.getUsername());
        return headers;
    }
}
