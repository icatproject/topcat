package org.icatproject.skeletonplugin.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter("/*")
public class CacheControlFilter implements Filter  {

    private static final Logger logger = LoggerFactory.getLogger(CacheControlFilter.class);

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        boolean isIe = false;
        String userAgent = request.getHeader("user-agent");
        if(userAgent != null){
            isIe = userAgent.indexOf("MSIE") > 0;
        }

        String uriPath = request.getRequestURI();
        String uiGridFontPathPattern = ".*ui-grid\\.(eot|svg|woff|ttf)\\z";
        boolean isUiGridFont = Pattern.matches(uiGridFontPathPattern, uriPath);

        if(!(isIe && isUiGridFont)){
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }

        String woffFontPattern = ".*\\.woff\\z";
        boolean isWoffFont = Pattern.matches(woffFontPattern, uriPath);
        if(isWoffFont){
            response.setHeader("Content-Type", "application/x-font-woff");
        }

        chain.doFilter(req, res);
    }

    public void init(FilterConfig config) throws ServletException {

    }

    public void destroy() {

    }

}