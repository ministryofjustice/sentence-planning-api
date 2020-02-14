package uk.gov.digital.justice.hmpps.sentenceplan.security;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;

public class ToggleAuthorisation implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return Arrays.stream(context.getEnvironment().getActiveProfiles()).noneMatch(prof -> prof.equals("disableauthorisation"));
        }
}
