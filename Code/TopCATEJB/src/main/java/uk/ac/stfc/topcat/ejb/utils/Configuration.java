/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.ejb.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * This is configuration file reader.
 * 
 * @author Mr. Srikanth Nagella
 */
public enum Configuration {
    INSTANCE;
    private static final String LOGO_URL = "LOGO_URL";
    private static final String MESSAGE = "MESSAGE";
    private static final String ACCESSIBILITY = "ACCESSIBILITY";
    private static final String COMPLAINTS_PROCEDURE = "COMPLAINTS_PROCEDURE";
    private static final String DATA_POLICY = "DATA_POLICY";
    private static final String FEEDBACK = "FEEDBACK";
    private static final String PRIVACY_POLICY = "PRIVACY_POLICY";
    private static final String TERMS_OF_USE = "TERMS_OF_USE";
    private boolean keywordsCached;
    private Map<String, String> properties;

    private Configuration() {
        try {
            Properties prop = new Properties();
            prop.load(Configuration.class.getClassLoader().getResourceAsStream("topcat.properties"));
            loadProperties(prop);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).fatal("Problem with topcat.properties " + ex.getMessage());
        }
    }

    private void loadProperties(Properties prop) {
        keywordsCached = Boolean.parseBoolean(prop.getProperty("KEYWORDS_CACHED"));
        properties = new HashMap<String, String>(8);
        // defaults to logo.jpg
        properties.put(LOGO_URL, prop.getProperty("LOGO_URL", "images/logo.jpg"));
        properties.put(MESSAGE, prop.getProperty("MESSAGE", ""));
        // links
        properties.put(ACCESSIBILITY, prop.getProperty(ACCESSIBILITY));
        properties.put(COMPLAINTS_PROCEDURE, prop.getProperty(COMPLAINTS_PROCEDURE));
        properties.put(DATA_POLICY, prop.getProperty(DATA_POLICY));
        properties.put(FEEDBACK, prop.getProperty(FEEDBACK));
        properties.put(PRIVACY_POLICY, prop.getProperty(PRIVACY_POLICY));
        properties.put(TERMS_OF_USE, prop.getProperty(TERMS_OF_USE));
    }

    /**
     * This method returns whether the keywords should be read from local cache
     * in database or use webservice. import
     * uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
     * 
     * @return true to use local db cache / false for webservice
     */
    public boolean isKeywordsCached() {
        return keywordsCached;
    }

    /**
     * This method returns the information from the topcat.properties file.
     * 
     * @return a map of properties
     */
    public Map<String, String> getTopcatProperties() {
        return properties;
    }

}
