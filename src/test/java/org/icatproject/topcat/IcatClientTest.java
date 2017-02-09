package org.icatproject.topcat;

import java.util.*;
import java.lang.reflect.*;

import static org.junit.Assert.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.json.*;

import org.icatproject.topcat.httpclient.HttpClient;
import org.icatproject.topcat.domain.*;
import java.net.URLEncoder;

public class IcatClientTest {

	private static String sessionId;

	@Before
	public void setup() throws Exception {
		TestHelpers.installTrustManager();

		HttpClient httpClient = new HttpClient("https://localhost:8181/icat");
		String data = "json=" + URLEncoder.encode("{\"plugin\":\"simple\", \"credentials\":[{\"username\":\"root\"}, {\"password\":\"root\"}]}", "UTF8");
		String response = httpClient.post("session", new HashMap<String, String>(), data).toString();
		sessionId = parseJsonObject(response).getString("sessionId");
	}

	@Test
	public void testGetUserName() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181");
		assertEquals("simple/root", icatClient.getUserName(sessionId));
	}

	@Test
	public void testIsAdmin() throws Exception {
		IcatClient icatClient = new IcatClientUserIsAdmin("https://localhost:8181");
		assertTrue(icatClient.isAdmin(sessionId));
		assertFalse(icatClient.isAdmin("bogus-session-id"));

		icatClient = new IcatClientUserNotAdmin("https://localhost:8181");
		assertFalse(icatClient.isAdmin(sessionId));
		assertFalse(icatClient.isAdmin("bogus-session-id"));
	}

	@Test
	public void testGetEntities() throws Exception {
		IcatClient icatClient = new IcatClientUserIsAdmin("https://localhost:8181");

		List<Long> ids = new ArrayList<Long>();

		List<JsonObject> results = icatClient.getEntities(sessionId, "investigation", ids);
		assertEquals(0, results.size());
		results = icatClient.getEntities(sessionId, "dataset", ids);
		assertEquals(0, results.size());
		results = icatClient.getEntities(sessionId, "datafile", ids);
		assertEquals(0, results.size());

		ids.add((long) 1);

		results = icatClient.getEntities(sessionId, "investigation", ids);
		assertEquals(1, results.size());

		results = icatClient.getEntities(sessionId, "dataset", ids);
		assertEquals(1, results.size());
		assertNotNull(results.get(0).getJsonObject("investigation"));

		results = icatClient.getEntities(sessionId, "datafile", ids);
		assertEquals(1, results.size());
		assertNotNull(results.get(0).getJsonObject("dataset"));
		assertNotNull(results.get(0).getJsonObject("dataset").getJsonObject("investigation"));

		for(long i = 2; i <= 10001; i++){
			ids.add((long) i);
		}

		results = icatClient.getEntities(sessionId, "datafile", ids);
		assertEquals(10001, results.size());

	}


	@Test
	public void testGetFullName() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181");
		String fullName = icatClient.getFullName(sessionId);

		assertNotNull(fullName);
		assertTrue(fullName.length() > 0);
	}

	private JsonObject parseJsonObject(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonObject out = jsonReader.readObject();
        jsonReader.close();
        return out;
    }

    private Map<String, List<Long>> createEmptyEntityTypeEntityIds(){
    	Map<String, List<Long>> out = new HashMap<String, List<Long>>();
		out.put("investigation", new ArrayList<Long>());
		out.put("dataset", new ArrayList<Long>());
		out.put("datafile", new ArrayList<Long>());
		return out;
    }


    class IcatClientUserIsAdmin extends IcatClient {

		public IcatClientUserIsAdmin(String url){
			super(url);
		}

		protected String[] getAdminUserNames() throws Exception {
			return new String[] {"simple/root"};
		}
    }

    class IcatClientUserNotAdmin extends IcatClient {

		public IcatClientUserNotAdmin(String url){
			super(url);
		}

		protected String[] getAdminUserNames() throws Exception {
			return new String[] {"db/test"};
		}
    }


}