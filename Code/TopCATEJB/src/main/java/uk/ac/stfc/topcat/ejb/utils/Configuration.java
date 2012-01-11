/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.ejb.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is configuration file reader.
 * 
 * @author Mr. Srikanth Nagella
 */
public enum Configuration {
    INSTANCE;
    private static final String ACCESSIBILITY = "ACCESSIBILITY";
    private static final String COMPLAINTS_PROCEDURE = "COMPLAINTS_PROCEDURE";
    private static final String DATA_POLICY = "DATA_POLICY";
    private static final String FEEDBACK = "FEEDBACK";
    private static final String PRIVACY_POLICY = "PRIVACY_POLICY";
    private static final String TERMS_OF_USE = "TERMS_OF_USE";
    private boolean keywordsCached;
    private String logoURL;
    private Map<String, String> links;

    private Configuration() {
        try {
            Properties prop = new Properties();
            prop.load(Configuration.class.getClassLoader().getResourceAsStream("topcat.properties"));
            loadProperties(prop);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadProperties(Properties prop) {
        keywordsCached = Boolean.parseBoolean(prop.getProperty("KEYWORDS_CACHED"));
        // defaults to logo.jpg
        logoURL = prop.getProperty("LOGO_URL", "images/logo.jpg");
        links = new HashMap<String, String>(6);
        links.put(ACCESSIBILITY, prop.getProperty(ACCESSIBILITY));
        links.put(COMPLAINTS_PROCEDURE, prop.getProperty(COMPLAINTS_PROCEDURE));
        links.put(DATA_POLICY, prop.getProperty(DATA_POLICY));
        links.put(FEEDBACK, prop.getProperty(FEEDBACK));
        links.put(PRIVACY_POLICY, prop.getProperty(PRIVACY_POLICY));
        links.put(TERMS_OF_USE, prop.getProperty(TERMS_OF_USE));
    }

    /**
     * This method returns whether the keywords should be read from local cache
     * in database or use webservice.
     * import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;

     * @return true to use local db cache / false for webservice
     */
    public boolean isKeywordsCached() {
        return keywordsCached;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public Map<String, String> getLinks() {
        return links;
    }

}
