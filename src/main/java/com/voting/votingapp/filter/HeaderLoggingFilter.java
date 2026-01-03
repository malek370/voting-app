package com.voting.votingapp.filter;

import jakarta.servlet.*; // Use javax.servlet.* for Spring Boot 2.x
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;

@Component
@Order(1)
public class HeaderLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HeaderLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Log request details
        System.out.println("=== REQUEST HEADERS ==="); // This will definitely show in console
        logger.info("=== REQUEST HEADERS ===");
        logger.info("Method: {} | URI: {}", httpRequest.getMethod(), httpRequest.getRequestURI());

        Collections.list(httpRequest.getHeaderNames())
                .forEach(headerName -> {
                    String value = httpRequest.getHeader(headerName);
                    System.out.println(headerName + ": " + value);
                    logger.info("{}: {}", headerName, value);
                });

        chain.doFilter(request, response);

        // Log response headers
        System.out.println("=== RESPONSE HEADERS ===");
        logger.info("=== RESPONSE HEADERS ===");
        httpResponse.getHeaderNames()
                .forEach(headerName -> {
                    String value = httpResponse.getHeader(headerName);
                    System.out.println(headerName + ": " + value);
                    logger.info("{}: {}", headerName, value);
                });
    }
}
