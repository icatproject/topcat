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

import org.icatproject.topcat.repository.CacheRepository;

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
    		return Utils.parseJsonObject(httpClient.get("session/" + sessionId, new HashMap<String, String>()).toString()).getString("userName");
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
		} catch(Exception e){}
		return false;
	}

	public String getFullName() throws TopcatException {
		try {
			String query = "select user.fullName from User user where user.name = :user";
			String url = "entityManager?sessionId=" + URLEncoder.encode(sessionId, "UTF8") + "&query=" + URLEncoder.encode(query, "UTF8");
    		String response = httpClient.get(url, new HashMap<String, String>()).toString();
    		try {
    			return Utils.parseJsonArray(response).getString(0);
    		} catch(Exception e){
    			return "";
    		}
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
				for(JsonValue entityValue : Utils.parseJsonArray(httpClient.get(passedUrl, new HashMap<String, String>()).toString())){
					JsonObject entity = (JsonObject) entityValue;
					out.add(entity.getJsonObject(entityType.substring(0, 1).toUpperCase() + entityType.substring(1)));
				}
			}

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}

		return out;
	}


	public Long getSize(CacheRepository cacheRepository, String entityType, Long entityId) throws TopcatException {
		try {
			String key = "getSize:" + entityType + ":" + entityId;
			Long size = (Long) cacheRepository.get(key);

			if(size != null){
				return size;
			}

			String query = null;

			if(entityType.equals("investigation")){
				query = "select sum(datafile.fileSize) from  Datafile datafile, datafile.dataset as dataset, dataset.investigation as investigation where investigation.id = " + entityId;
			} else if(entityType.equals("dataset")){
				query = "select sum(datafile.fileSize) from  Datafile datafile, datafile.dataset as dataset where dataset.id = " + entityId;
			} else if(entityType.equals("datafile")){
				query = "select datafile.fileSize from  Datafile datafile where datafile.id = " + entityId;
			} else {
				throw new BadRequestException("Unknown or supported entity \"" + entityType + "\" for getSize");
			}

			String url = "entityManager?sessionId=" + URLEncoder.encode(sessionId, "UTF8") + "&query=" + URLEncoder.encode(query, "UTF8");
			String response = httpClient.get(url, new HashMap<String, String>()).toString();
			size = ((JsonNumber) Utils.parseJsonArray(response).get(0)).longValue();
			cacheRepository.put(key, size);

			return size;
		} catch(TopcatException e){
			throw e;
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	public Long getSize(CacheRepository cacheRepository, List<Long> investigationIds, List<Long> datasetIds, List<Long> datafileIds) throws TopcatException {
		Long out = 0L;

		for(Long investigationId : investigationIds){
			out += getSize(cacheRepository, "investigation", investigationId);
		}

		for(Long datasetId : datasetIds){
			out += getSize(cacheRepository, "dataset", datasetId);
		}

		for(Long datafileId : datafileIds){
			out += getSize(cacheRepository, "datafile", datafileId);
		}
		
		return out;
	}

	protected String[] getAdminUserNames() throws Exception {
		return Properties.getInstance().getProperty("adminUserNames", "").split("([ ]*,[ ]*|[ ]*)");
	}

}

