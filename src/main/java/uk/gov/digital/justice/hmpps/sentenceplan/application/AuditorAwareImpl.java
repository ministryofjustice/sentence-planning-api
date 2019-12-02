package uk.gov.digital.justice.hmpps.sentenceplan.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@Service(value = "auditorAware")
public class AuditorAwareImpl implements AuditorAware<String> {

    private RequestData requestData;

    public AuditorAwareImpl(RequestData requestData) {
        this.requestData = requestData;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(requestData.getUsername());
    }
} 