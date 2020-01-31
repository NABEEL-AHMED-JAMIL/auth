package com.barco.auth.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {

    public Logger logger = LogManager.getLogger(CORSFilter.class);

    private final String ACCESS_ORIGIN_KEY = "Access-Control-Allow-Origin";
    private final String ACCESS_ORIGIN_VALUE = "*";
    private final String ACCESS_METHODS_KEY = "Access-Control-Allow-Methods";
    private final String ACCESS_METHODS_VALUE = "GET, OPTIONS, HEAD, PUT, POST";
    private final String OPTIONS = "OPTIONS";

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        ((HttpServletResponse) res).addHeader(ACCESS_ORIGIN_KEY, ACCESS_ORIGIN_VALUE);
        ((HttpServletResponse) res).addHeader(ACCESS_METHODS_KEY, ACCESS_METHODS_VALUE);
        HttpServletResponse resp = (HttpServletResponse) res;
        if (request.getMethod().equals(OPTIONS)) {
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            return;
        }
        chain.doFilter(request, res);
    }

    public void init(FilterConfig filterConfig) { }

    public void destroy() { }

}