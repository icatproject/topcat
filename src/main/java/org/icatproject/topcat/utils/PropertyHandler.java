package org.icatproject.topcat.utils;

import org.icatproject.utils.CheckedProperties;
import org.icatproject.utils.CheckedProperties.CheckedPropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyHandler {

    private static PropertyHandler instance = null;
    private static final Logger logger = LoggerFactory.getLogger(PropertyHandler.class);

    public synchronized static PropertyHandler getInstance() {
        logger.debug("PropertyHandler getInstance called");
        if (instance == null) {
            instance = new PropertyHandler();
        }
        return instance;
    }

    public static Logger getLogger() {
        return logger;
    }

    private String path;
    private boolean mailEnable;
    private String mailSubject;
    private String mailBodyHttps;
    private String mailBodyGlobus;
    private String mailBodySmartClient;

    private PropertyHandler() {
        CheckedProperties props = new CheckedProperties();

        try {
            props.loadFromFile("topcat.properties");

            path = props.getProperty("file.directory");
            mailEnable = props.getBoolean("mail.enable");
            mailSubject = props.getProperty("mail.subject");
            mailBodyHttps = props.getProperty("mail.body.https");
            mailBodyGlobus = props.getProperty("mail.body.globus");
            mailBodySmartClient = props.getProperty("mail.body.smartclient");
        } catch (CheckedPropertyException e) {
            logger.info("Property file topcat.properties not loaded");
            e.printStackTrace();
        }

        logger.info("Property file topcat.properties loaded");

    }

    public String getPath() {
        return path;
    }

    public boolean isMailEnable() {
        return mailEnable;
    }

    public String getMailSubject() {
        return mailSubject;
    }

    public String getMailBodyHttps() {
        return mailBodyHttps;
    }


    public String getMailBodyGlobus() {
        return mailBodyGlobus;
    }

    public String getMailBodySmartClient() {
        return mailBodySmartClient;
    }
}
