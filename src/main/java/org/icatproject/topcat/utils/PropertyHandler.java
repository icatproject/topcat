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
    private int maxPerGetStatus;
    private int pollDelay;
    private int pollIntervalWait;
    private int pollIsPreparedWait;
    private String[] adminUserNames;


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
            maxPerGetStatus = props.getPositiveInt("ids.getStatus.max");
            pollDelay = props.getPositiveInt("poll.delay");
            pollIntervalWait = props.getPositiveInt("poll.interval.wait");
            pollIsPreparedWait = props.getPositiveInt("poll.isprepared.wait");
            adminUserNames = props.getProperty("adminUserNames").split("[ ]*,[ ]*");
        } catch (CheckedPropertyException e) {
            logger.info("Property file topcat.properties not loaded");
            e.printStackTrace();
        }

        logger.info("Property file topcat.properties loaded");
    }

    public int getPollDelay() {
        return pollDelay;
    }

    public void setPollDelay(int pollDelay) {
        this.pollDelay = pollDelay;
    }

    public int getPollIntervalWait() {
        return pollIntervalWait;
    }

    public void setPollIntervalWait(int pollIntervalWait) {
        this.pollIntervalWait = pollIntervalWait;
    }

    public int getPollIsPreparedWait() {
        return pollIsPreparedWait;
    }

    public void setPollIsPreparedWait(int pollIsPreparedWait) {
        this.pollIsPreparedWait = pollIsPreparedWait;
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

    public int getMaxPerGetStatus() {
        return maxPerGetStatus;
    }

    public void setMaxPerGetStatus(int maxPerGetStatus) {
        this.maxPerGetStatus = maxPerGetStatus;
    }

    public String[] getAdminUserNames(){
        return adminUserNames;
    }
}
