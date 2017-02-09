package org.icatproject.topcat;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.net.URLEncoder;

import org.icatproject.topcat.httpclient.*;
import org.icatproject.topcat.exceptions.*;
import org.icatproject.topcat.domain.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.json.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.icatproject.topcat.utils.PropertyHandler;

public class IcatClient {

	private Logger logger = LoggerFactory.getLogger(IcatClient.class);

    private HttpClient httpClient;
   
    public IcatClient(String url){
        this.httpClient = new HttpClient(url + "/icat");
    }

    public String getUserName(String sessionId) throws TopcatException {
    	try {
    		return parseJsonObject(httpClient.get("session/" + sessionId, new HashMap<String, String>()).toString()).getString("userName");
    	} catch (Exception e){
            throw new BadRequestException(e.getMessage());
    	}
    }

	public Boolean isAdmin(String icatSessionId) throws TopcatException {
		try {
			String[] adminUserNames = getAdminUserNames();
			String userName = getUserName(icatSessionId);
			int i;

			for (i = 0; i < adminUserNames.length; i++) {
				if(userName.equals(adminUserNames[i])){
					return true;
				}
			}
		} catch(Exception e){}
		return false;
	}

	//todo: merge into UserResource addCartItems
	public List<CartItem> getCartItems(String sessionId, Map<String, List<Long>> entityTypeEntityIds) throws TopcatException {
		List<CartItem> out = new ArrayList<CartItem>();

		for (String entityType : entityTypeEntityIds.keySet()) {
			List<Long> entityIds = entityTypeEntityIds.get(entityType);
			for (JsonObject entity : getEntities(sessionId, entityType, entityIds)) {
				String name = entity.getString("name");
				Long entityId = Long.valueOf(entity.getInt("id"));

				CartItem cartItem = new CartItem();
				cartItem.setEntityType(EntityType.valueOf(entityType));
				cartItem.setEntityId(entityId);
				cartItem.setName(name);


				if (entityType.equals("datafile")) {
					ParentEntity parentEntity = new ParentEntity();
					parentEntity.setEntityType(EntityType.valueOf("dataset"));
					parentEntity.setEntityId(Long.valueOf(entity.getJsonObject("dataset").getInt("id")));
					cartItem.getParentEntities().add(parentEntity);

					parentEntity = new ParentEntity();
					parentEntity.setEntityType(EntityType.valueOf("investigation"));
					parentEntity.setEntityId(Long.valueOf(entity.getJsonObject("dataset").getJsonObject("investigation").getInt("id")));
					cartItem.getParentEntities().add(parentEntity);

				} else if (entityType.equals("dataset")) {
					ParentEntity parentEntity = new ParentEntity();
					parentEntity.setEntityType(EntityType.valueOf("investigation"));
					parentEntity.setEntityId(Long.valueOf(entity.getJsonObject("investigation").getInt("id")));
					cartItem.getParentEntities().add(parentEntity);
				}

				out.add(cartItem);

			}
		}

		return out;
	}

	public String getFullName(String sessionId) throws TopcatException {
		try {
			String query = "select user.fullName from User user where user.name = :user";
			String url = "entityManager?sessionId=" + URLEncoder.encode(sessionId, "UTF8") + "&query=" + URLEncoder.encode(query, "UTF8");
    		return parseJsonArray(httpClient.get(url, new HashMap<String, String>()).toString()).getString(0);
    	} catch (Exception e){
            throw new BadRequestException(e.getMessage());
    	}
	}

	// public Long getSize(String sessionId, String entityType, Long entityId){

	// }

	protected String[] getAdminUserNames() throws Exception {
		return PropertyHandler.getInstance().getAdminUserNames();
	}

	private List<JsonObject> getEntities(String sessionId, String entityType, List<Long> entityIds) throws TopcatException {
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
				for(JsonValue entityValue : parseJsonArray(httpClient.get(passedUrl, new HashMap<String, String>()).toString())){
					JsonObject entity = (JsonObject) entityValue;
					out.add(entity.getJsonObject(entityType.substring(0, 1).toUpperCase() + entityType.substring(1)));
				}
			}

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}

		return out;
	}

	//todo: merge into Util methods in 2.3.0
	private JsonObject parseJsonObject(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonObject out = jsonReader.readObject();
        jsonReader.close();
        return out;
    }

    private JsonArray parseJsonArray(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonArray out = jsonReader.readArray();
        jsonReader.close();
        return out;
    }
}

