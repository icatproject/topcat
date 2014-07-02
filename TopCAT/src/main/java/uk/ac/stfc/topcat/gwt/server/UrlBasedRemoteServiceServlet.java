package uk.ac.stfc.topcat.gwt.server;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * Policy file fix as per https://code.google.com/p/google-web-toolkit/issues/detail?id=4817#c4
 * 
 * Example apache mod_proxy configuration with no contextroot:
 * 
 * ProxyPass /topcat/ https://localhost:8181/
 * ProxyPassReverse /topcat/ https://localhost:8181/
 * ProxyPassReverseCookiePath / /topcat/
 * 
 * ProxyPass /topcatadmin/ https://localhost:8181/topcatadmin/
 * ProxyPassReverse /topcatadmin/ https://localhost:8181/topcatadmin/
 * 
 * <Location /topcat/>
 *   RequestHeader edit X-GWT-Module-Base ^(.*)/topcat/(.*)$ $1/$2
 * </Location>
 * 
 * Example apache mod_proxy configuration if contextroot is used:
 * 
 * ProxyPass /topcat/ https://localhost:8181/CONTEXTROOT/
 * ProxyPassReverse /topcat/ https://localhost:8181/CONTEXTROOT/
 * ProxyPassReverseCookiePath /CONTEXTROOT /topcat/
 * <Location /topcat/>
 *   RequestHeader edit X-GWT-Module-Base ^(.*)/topcat/(.*)$ $1/CONTEXTROOT/$2
 * </Location>
 * 
 */
public class UrlBasedRemoteServiceServlet extends RemoteServiceServlet {
    private static final long serialVersionUID = 1652976359046240164L;
    
    @Override
    protected SerializationPolicy doGetSerializationPolicy(
            HttpServletRequest request, String moduleBaseURL, String strongName) {
        //get the base url from the header instead of the body this way 
        //apache reverse proxy with rewrite on the header can work
        String moduleBaseURLHdr = request.getHeader("X-GWT-Module-Base");
        
        if(moduleBaseURLHdr != null){
            moduleBaseURL = moduleBaseURLHdr;
        }
        
        return super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
    }
}
