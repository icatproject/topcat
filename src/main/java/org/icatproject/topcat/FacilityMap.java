package org.icatproject.topcat;

import java.util.HashMap;
import java.util.Map;

import org.icatproject.topcat.exceptions.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacilityMap {
	
    private static FacilityMap instance = null;

    public synchronized static FacilityMap getInstance() throws InternalException {
       if(instance == null) {
          instance = new FacilityMap();
       }
       return instance;
    }
    
	private Logger logger = LoggerFactory.getLogger(FacilityMap.class);
	
	private Properties properties;
	private Map<String,String> facilityIcatUrl;
	private Map<String,String> facilityIdsUrl;

	public FacilityMap() throws InternalException{
		
		facilityIcatUrl = new HashMap<String,String>();
		facilityIdsUrl = new HashMap<String,String>();
		
		properties = Properties.getInstance();
		
		String[] facilities = properties.getProperty("facility.list","").split("([ ]*,[ ]*|[ ]+)");
		
		// Complain/log if property is not set
		if( facilities.length == 0 || (facilities.length == 1 && facilities[0].length() == 0)){
			logger.error( "FacilityMap: property facility.list is not defined.");
			throw new InternalException("Property facility.list is not defined.");
		}
		
		for( String facility : facilities ){
			String icatUrl = properties.getProperty("facility." + facility + ".icatUrl","");
			// Complain/log if property is not set
			if( icatUrl.length() == 0 ){
				String error = "FacilityMap: property facility." + facility + ".icatUrl is not defined.";
				logger.error( error );
				throw new InternalException( error );
			}
			facilityIcatUrl.put( facility,  icatUrl );
			String idsUrl = properties.getProperty("facility." + facility + ".idsUrl","");
			// Complain/log if property is not set
			if( idsUrl.length() == 0 ){
				String error = "FacilityMap: property facility." + facility + ".idsUrl is not defined.";
				logger.error( error );
				throw new InternalException( error );
			}
			facilityIdsUrl.put( facility,  idsUrl );
		}
	}
	
	public String getIcatUrl( String facility ) throws InternalException{
		String url = facilityIcatUrl.get( facility );
		if( url == null ){
			String error = "FacilityMap.getIcatUrl: unknown facility: " + facility;
			logger.error( error );
			throw new InternalException( error );
		}
		return url;
	}

	public String getIdsUrl( String facility ) throws InternalException{
		String url = facilityIdsUrl.get( facility );
		if( url == null ){
			String error = "FacilityMap.getIdsUrl: unknown facility: " + facility;
			logger.error( error );
			throw new InternalException( error );
		}
		return url;
	}
	
	public String getDownloadUrl( String facility, String downloadType ) throws InternalException{
		String url = "";
		// First, look for the property directly
		url = properties.getProperty( "facility." + facility + ".downloadType." + downloadType, "" );
		if( url.length() == 0 ){
			// No such property, so fall back to the facility idsUrl
			logger.info("FacilityMap.getDownloadUrl: no specific property for facility '" 
					+ facility + "' and download type '" + downloadType + "'; returning idsUrl instead" );
			url = this.getIdsUrl(facility);
		}
		return url;
	}
}
