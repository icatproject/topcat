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
	public void testGetCartItems1() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181");

		Map<String, List<Long>> entityTypeEntityIds = createEmptyEntityTypeEntityIds();

		List<CartItem> cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(0, cartItems.size());

		entityTypeEntityIds.get("investigation").add((long) 1);
		cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(1, cartItems.size());


		entityTypeEntityIds.get("dataset").add((long) 1);
		cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(2, cartItems.size());
		
		entityTypeEntityIds.get("datafile").add((long) 1);
		cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(3, cartItems.size());

		entityTypeEntityIds.get("investigation").add((long) 2);
		entityTypeEntityIds.get("dataset").add((long) 3);
		entityTypeEntityIds.get("datafile").add((long) 4);
		entityTypeEntityIds.get("datafile").add((long) 5);
		entityTypeEntityIds.get("datafile").add((long) 6);
		entityTypeEntityIds.get("datafile").add((long) 7);
		cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(9, cartItems.size());

		
	}

	@Test
	public void testGetCartItems2() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181");

		Map<String, List<Long>> entityTypeEntityIds = createEmptyEntityTypeEntityIds();
		for(long i = 1; i <= 10000; i++){
			entityTypeEntityIds.get("datafile").add(i);
		}
		List<CartItem> cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(10000, cartItems.size());
	}

	@Test
	public void testGetCartItems3() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181");

		Map<String, List<Long>> entityTypeEntityIds = createEmptyEntityTypeEntityIds();
		entityTypeEntityIds.get("investigation").add((long) 1);
		List<CartItem> cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(0, cartItems.get(0).getParentEntities().size());
	}

	@Test
	public void testGetCartItems4() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181");

		Map<String, List<Long>> entityTypeEntityIds = createEmptyEntityTypeEntityIds();
		entityTypeEntityIds.get("dataset").add((long) 1);
		List<CartItem> cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(1, cartItems.get(0).getParentEntities().size());
	}

	@Test
	public void testGetCartItems5() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181");

		Map<String, List<Long>> entityTypeEntityIds = createEmptyEntityTypeEntityIds();
		entityTypeEntityIds.get("datafile").add((long) 1);
		List<CartItem> cartItems = icatClient.getCartItems(sessionId, entityTypeEntityIds);
		assertEquals(2, cartItems.get(0).getParentEntities().size());
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