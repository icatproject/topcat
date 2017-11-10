
package org.icatproject.topcat;

import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Properties extends java.util.Properties {
    
    private static Properties instance = null;

    private Logger logger = LoggerFactory.getLogger(Properties.class);
    
    public synchronized static Properties getInstance() {
       if(instance == null) {
          instance = new Properties();
       }
       return instance;
    }
    
    public Properties(){
        super();
        try {
            load(new FileInputStream("topcat.properties"));
        } catch(IOException e){
            logger.info("error loading topcat.properties: " + e.getMessage() + "; continuing, but expect further problems.");
        }
    }
    
}
