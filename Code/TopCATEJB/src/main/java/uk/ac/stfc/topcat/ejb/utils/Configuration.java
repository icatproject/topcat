/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.ejb.utils;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is configuration file reader.
 * @author Mr. Srikanth Nagella
 */
public enum Configuration {
    INSTANCE;
    boolean keywordsCached;
    private Configuration(){
        try {
            Properties prop = new Properties();
            prop.load(Configuration.class.getClassLoader().getResourceAsStream("topcat.properties"));
           loadProperties(prop);
        } catch (IOException ex) {
           Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadProperties(Properties prop){
        keywordsCached = Boolean.parseBoolean(prop.getProperty("KEYWORDS_CACHED"));
    }
    /**
     * This method returns whether the keywords should be read from local cache in database
     * or use webservice.
     *
     * @return true to use local db cache / false for webservice
     */
    public boolean isKeywordsCached(){
        return keywordsCached;
    }
}
