package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.net.URLEncoder;

import org.icatproject.topcat.httpclient.*;
import org.icatproject.topcat.exceptions.*;
import org.icatproject.topcat.domain.*;

import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcatClient {

	private Logger logger = LoggerFactory.getLogger(IcatClient.class);

    private HttpClient httpClient;
    private String sessionId;
   
    public IcatClient(String url, String sessionId){
        this.httpClient = new HttpClient(url + "/icat");
        this.sessionId = sessionId;
    }

    public String getUserName() throws TopcatException {
    	try {
    		Response response = httpClient.get("session/" + sessionId, new HashMap<String, String>());
    		if(response.getCode() == 404){
                throw new NotFoundException("Could not run getUserName got a 404 response");
            } else if(response.getCode() >= 400){
                throw new BadRequestException(Utils.parseJsonObject(response.toString()).getString("message"));
            }
    		return Utils.parseJsonObject(response.toString()).getString("userName");
    	} catch (TopcatException e){
            throw e;
    	} catch (Exception e){
            throw new BadRequestException(e.getMessage());
    	}
    }

	public Boolean isAdmin() throws TopcatException {
		try {
			String[] adminUserNames = getAdminUserNames();
			String userName = getUserName();
			int i;

			for (i = 0; i < adminUserNames.length; i++) {
				if(userName.equals(adminUserNames[i])){
					return true;
				}
			}
		} catch(Exception e){
			logger.info("isAdmin: " + e.getMessage());
			// Ought to throw a BadRequestException here,
			// but existing usage expects a return value of false in this case.
			// throw new BadRequestException(e.getMessage());
		}
		return false;
	}

	public String getFullName() throws TopcatException {
		try {
			String query = "select user.fullName from User user where user.name = :user";
			String url = "entityManager?sessionId=" + URLEncoder.encode(sessionId, "UTF8") + "&query=" + URLEncoder.encode(query, "UTF8");
    		Response response = httpClient.get(url, new HashMap<String, String>());
    		
    		if(response.getCode() == 404){
    			logger.info("IcatClient.getFullName: got a 404 response");
                throw new NotFoundException("Could not run getFullName got a 404 response");
            } else if(response.getCode() >= 400){
            	String message = Utils.parseJsonObject(response.toString()).getString("message");
    			logger.info("IcatClient.getFullName: got a " + response.getCode() + " response: " + message);
                throw new BadRequestException(Utils.parseJsonObject(response.toString()).getString("message"));
            }

    		JsonArray responseArray = Utils.parseJsonArray(response.toString());
    		if( responseArray.size() == 0 ){
    			logger.info("IcatClient.getFullName: client returned empty array, so returning empty string");
    			return "";
    		} else {
    			return responseArray.getString(0);
    		}
    	} catch (TopcatException e){
            throw e;
    	} catch (Exception e){
            throw new BadRequestException(e.getMessage());
    	}
	}

	public List<JsonObject> getEntities(String entityType, List<Long> entityIds) throws TopcatException {
		List<JsonObject> out = new ArrayList<JsonObject>();
		try {
			entityIds = new ArrayList<Long>(entityIds);

			String queryPrefix;
			String querySuffix;

			if (entityType.equals("datafile")) {
				queryPrefix = "SELECT datafile from Datafile datafile where datafile.id in (";
				querySuffix = ") include datafile.dataset.investigation";
			} else if (entityType.equals("dataset")) {
				queryPrefix = "SELECT dataset from Dataset dataset where dataset.id in (";
				querySuffix = ") include dataset.investigation";
			} else {
				queryPrefix = "SELECT investigation from Investigation investigation where investigation.id in (";
				querySuffix = ")";
			}

			StringBuffer currentCandidateEntityIds = new StringBuffer();
			String currentPassedUrl = null;
			String currentCandidateUrl = null;

			List<String> passedUrls = new ArrayList<String>();

			while(entityIds.size() > 0){
				if (currentCandidateEntityIds.length() != 0) {
					currentCandidateEntityIds.append(",");
				}
				currentCandidateEntityIds.append(entityIds.get(0));
				currentCandidateUrl = "entityManager?sessionId="  + URLEncoder.encode(sessionId, "UTF8") + "&query=" + URLEncoder.encode(queryPrefix + currentCandidateEntityIds.toString() + querySuffix , "UTF8");
				if(httpClient.urlLength(currentCandidateUrl) > 2048){
					currentCandidateEntityIds = new StringBuffer();
					if(currentPassedUrl == null){
						break;
					}
					passedUrls.add(currentPassedUrl);
					currentPassedUrl = null;
				} else {
					currentPassedUrl = currentCandidateUrl;
					currentCandidateUrl = null;
					entityIds.remove(0);
				}
			}

			if(currentPassedUrl != null){
				passedUrls.add(currentPassedUrl);
			}

			for(String passedUrl : passedUrls){
				Response response = httpClient.get(passedUrl, new HashMap<String, String>());

				if(response.getCode() == 404){
	                throw new NotFoundException("Could not run getEntities got a 404 response");
	            } else if(response.getCode() >= 400){
	                throw new BadRequestException(Utils.parseJsonObject(response.toString()).getString("message"));
	            }

				for(JsonValue entityValue : Utils.parseJsonArray(response.toString())){
					JsonObject entity = (JsonObject) entityValue;
					out.add(entity.getJsonObject(entityType.substring(0, 1).toUpperCase() + entityType.substring(1)));
				}
			}
		} catch (TopcatException e){
            throw e;
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}

		return out;
	}


	protected String[] getAdminUserNames() throws Exception {
		return Properties.getInstance().getProperty("adminUserNames", "").split("([ ]*,[ ]*|[ ]+)");
	}

}

