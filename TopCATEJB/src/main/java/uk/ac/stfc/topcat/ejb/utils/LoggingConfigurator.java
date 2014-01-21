package uk.ac.stfc.topcat.ejb.utils;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.icatproject.utils.CheckedProperties;

@Singleton
@Startup
public class LoggingConfigurator {

	@PostConstruct
	private void init() {
		CheckedProperties props = new CheckedProperties();
		try {
			props.loadFromFile("topcat.properties");
		} catch (Exception e) {
			String msg = "Problem with topcat.properties " + e.getMessage();
			throw new IllegalStateException(msg);
		}

		String path = props.getProperty("log4j.properties");
		if (path != null) {
			File f = new File(path);
			if (!f.exists()) {
				String msg = "log4j.properties file " + f.getAbsolutePath()
						+ " specified in topcat.properties not found";
				throw new IllegalStateException(msg);
			}
			PropertyConfigurator.configure(path);

		} else {
			/*
			 * This seems to be necessary even though the default initialisation is to load from the
			 * Classpath
			 */
			PropertyConfigurator.configure(LoggingConfigurator.class.getClassLoader().getResource(
					"log4j.properties"));
		}

		Logger logger = Logger.getLogger(LoggingConfigurator.class);
		if (path != null) {
			logger.info("Logging configuration read from " + path);
		} else {
			logger.info("Using log4j default configuration");
		}

	}
}
