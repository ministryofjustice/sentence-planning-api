package uk.gov.digital.justice.hmpps.sentenceplan.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfig  extends WebSecurityConfigurerAdapter {

    //TODO: Implement authentication for api, for now disable to allow client auth to assessment api
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests().anyRequest().permitAll();
    }
}