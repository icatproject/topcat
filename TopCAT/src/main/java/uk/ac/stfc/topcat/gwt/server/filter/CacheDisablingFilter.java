package uk.ac.stfc.topcat.gwt.server.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter to apply "never cache" HTTP headers.
 * <p>
 * See: <a href="http://seewah.blogspot.com/2009/02/gwt-tips-2-nocachejs-getting-cached-in.html">GWT Tips 2 - nocache.js getting cached in browser</a> * 
 * <p>
 */
public class CacheDisablingFilter implements Filter {

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            Date now = new Date();
            httpResponse.setDateHeader("Date", now.getTime());
            httpResponse.setHeader("Expires", "0");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
        }
        
        chain.doFilter(request, response);
    }


    @Override
    public void init(FilterConfig arg0) throws ServletException {
        
    }

}

