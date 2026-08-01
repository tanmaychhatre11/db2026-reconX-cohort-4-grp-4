package com.dbtraining.reconx.observability;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class MdcFilter implements Filter {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String TRADE_HEADER = "X-Trade-Ref";
    private static final Logger log = LoggerFactory.getLogger(MdcFilter.class);

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String correlationId = getHeader(
                httpRequest,
                CORRELATION_HEADER,
                UUID.randomUUID().toString()
        );

        String tradeRef = getHeader(
                httpRequest,
                TRADE_HEADER,
                null
        );

        try {
            MDC.put("correlationId", correlationId);

            if (tradeRef != null) {
                MDC.put("tradeRef", tradeRef);
            }
            log.info("Filter invoked");
            
            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }

    private String getHeader(
            HttpServletRequest request,
            String header,
            String fallback
    ) {
        String value = request.getHeader(header);

        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }
}