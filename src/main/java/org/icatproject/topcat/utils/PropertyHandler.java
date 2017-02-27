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

	private boolean mailEnable;
	private String mailSubject;
	private String mailBodyHttps;
	private String mailBodyGlobus;
	private String mailBodySmartClient;
	private String mailBodyScarf;
	private int maxPerGetStatus;
	private int pollDelay;
	private int pollIntervalWait;
	private int pollIsPreparedWait;
	private String[] adminUserNames;
	private int maxCacheSize;

	private PropertyHandler() {
		CheckedProperties props = new CheckedProperties();

		try {
			props.loadFromFile("topcat.properties");

			mailEnable = props.getBoolean("mail.enable");
			if (mailEnable) {
				mailSubject = props.getString("mail.subject");
				mailBodyHttps = props.getString("mail.body.https");
				mailBodyGlobus = props.getString("mail.body.globus");
				mailBodySmartClient = props.getString("mail.body.smartclient");
				mailBodyScarf = props.getString("mail.body.scarf");
			}
			maxPerGetStatus = props.getPositiveInt("ids.getStatus.max");
			pollDelay = props.getPositiveInt("poll.delay");
			pollIntervalWait = props.getPositiveInt("poll.interval.wait");
			adminUserNames = props.getString("adminUserNames").split("[ ]*,[ ]*");
			maxCacheSize = props.getPositiveInt("maxCacheSize");
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

	public String getMailBodyScarf() {
		return mailBodyScarf;
	}

	public int getMaxPerGetStatus() {
		return maxPerGetStatus;
	}

	public String[] getAdminUserNames() {
		return adminUserNames;
	}

	public int getMaxCacheSize() {
		return maxCacheSize;
	}
}
