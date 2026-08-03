package com.fellowlodge.api.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Prevents browsers, proxies and the React guest portal from serving stale
 * API responses. Because every REST call re-reads PostgreSQL, disabling HTTP
 * caching is what makes Administrator changes appear "in real time" on the
 * guest portal.
 */
@Component
@Order(1)
public class CacheControlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = request instanceof HttpServletRequest httpRequest ? httpRequest.getRequestURI() : "";
        if (uri.startsWith("/api/")) {
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Expires", "0");
        }
        chain.doFilter(request, response);
    }
}
