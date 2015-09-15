package uk.ac.stfc.topcat.gwt.client.model;

import java.util.Map;

public interface TopcatCookie {
    String getLastAuthenticationFacility();
    String getlastAuthenticationType();
    Map<String,String> getServers();
    
    void setLastAuthenticationFacility(String facility);
    void setlastAuthenticationType(String type);
    void setServers(Map<String, String> servers);    
}
