package uk.gov.digital.justice.hmpps.sentenceplan.application;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
public class RequestData implements HandlerInterceptor {


    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USERNAME_HEADER = "X-Auth-Username";
    private static final String ANONYMOUS = "anonymous";
    public static final String SESSION_ID_HEADER = "X-Session-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.clear();
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USERNAME_HEADER, initialiseUserName(request));
        MDC.put(SESSION_ID_HEADER, initialiseSessionId(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(USERNAME_HEADER, getUsername());
        response.setHeader(CORRELATION_ID_HEADER, getCorrelationId());
        response.setHeader(SESSION_ID_HEADER, getSessionId());
        MDC.clear();
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        var correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return !StringUtils.isEmpty(correlationId) ? correlationId : UUID.randomUUID().toString();
    }


    private String initialiseUserName(HttpServletRequest request) {
        var username = request.getHeader(USERNAME_HEADER);
        return !StringUtils.isEmpty(username) ? username : ANONYMOUS;
    }

    private String initialiseSessionId(HttpServletRequest request) {
        return request.getHeader(SESSION_ID_HEADER);
    }

    public String getCorrelationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String getUsername() { return MDC.get(USERNAME_HEADER); }

    public String getSessionId() { return MDC.get(SESSION_ID_HEADER); }
}