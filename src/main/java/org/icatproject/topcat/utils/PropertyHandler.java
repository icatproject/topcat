package org.icatproject.topcat.utils;

import java.util.HashMap;
import java.util.Map;

import org.icatproject.topcat.domain.IdsReader;
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
    private Map<String, IdsReader> idsReaders;
    private boolean mailEnable;
    private String mailSubject;
    private String mailBodyHttps;
    private String mailBodyGlobus;

    private PropertyHandler() {

        logger.debug("PropertyHandler constructor called");

        CheckedProperties props = new CheckedProperties();

        idsReaders = new HashMap<String, IdsReader>();


        try {
            props.loadFromFile("topcat.properties");

            path = props.getProperty("file.directory");
            mailEnable = props.getBoolean("mail.enable");
            mailSubject = props.getProperty("mail.subject");
            mailBodyHttps = props.getProperty("mail.body.https");
            mailBodyGlobus = props.getProperty("mail.body.globus");

            logger.debug("Property path: " + path);

            String readerString = props.getProperty("ids.readers");

            logger.debug("Property ids.readers: " + readerString);

            String[] readers = null;

            if (readerString.indexOf(",") != -1) {
                logger.debug("contains ,");
                readers = readerString.split(",");
            } else {
                logger.debug("no ,");
                readers = new String[]{readerString};
            }

            logger.debug("Property readers: " + readers);

            for (String reader : readers) {
                String[] idsProps = reader.split(" ");

                logger.debug("idsProps " + idsProps[0]);
                logger.debug("idsProps " + idsProps[1]);
                logger.debug("idsProps " + idsProps[2]);
                logger.debug("idsProps " + idsProps[3]);
                logger.debug("idsProps " + idsProps[4]);
                logger.debug("idsProps " + idsProps[5]);

                IdsReader idsReader = new IdsReader();
                idsReader.setFacilityName(idsProps[0]);
                idsReader.setAuthenticatorType(idsProps[1]);
                idsReader.setUserNameKey(idsProps[2]);
                idsReader.setUserName(idsProps[3]);
                idsReader.setPasswordKey(idsProps[4]);
                idsReader.setPassword(idsProps[5]);

                idsReaders.put(idsProps[0], idsReader);

                logger.debug("set property end");
            }

            logger.debug("Property loadFromFile");

        } catch (CheckedPropertyException e) {
            logger.info("Property file topcat.properties not loaded");
            e.printStackTrace();
        }


        logger.info("Property file topcat.properties loaded");

    }

    public String getPath() {
        return path;
    }

    public Map<String, IdsReader> getIdsReaders() {
        return idsReaders;
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


}
