package org.icatproject.topcat;

import java.util.*;
import java.lang.reflect.*;

import static org.junit.Assert.*;
import org.junit.*;

import javax.json.*;
import javax.ejb.EJB;

import org.icatproject.topcat.httpclient.HttpClient;
import org.icatproject.topcat.domain.*;
import java.net.URLEncoder;

import org.icatproject.topcat.repository.CacheRepository;

import java.sql.*;

public class IcatClientTest {

	@EJB
	private CacheRepository cacheRepository;

	private static String sessionId;

	private Connection connection;

	@BeforeClass
	public void beforeAll() {
		TestHelpers.installTrustManager();
	}

	@Before
	public void setup() throws Exception {

		HttpClient httpClient = new HttpClient("https://localhost:8181/icat");
		String data = "json=" + URLEncoder.encode(
				"{\"plugin\":\"simple\", \"credentials\":[{\"username\":\"root\"}, {\"password\":\"root\"}]}", "UTF8");
		String response = httpClient.post("session", new HashMap<String, String>(), data).toString();
		sessionId = Utils.parseJsonObject(response).getString("sessionId");

		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/icat", "root", "secret");
	}

	@Test
	public void testGetUserName() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181", sessionId);
		assertEquals("simple/root", icatClient.getUserName());
	}

	@Test
	public void testIsAdmin() throws Exception {
		IcatClient icatClient = new IcatClientUserIsAdmin("https://localhost:8181", sessionId);
		assertTrue(icatClient.isAdmin());
		icatClient = new IcatClientUserIsAdmin("https://localhost:8181", "bogus-session-id");
		assertFalse(icatClient.isAdmin());

		icatClient = new IcatClientUserNotAdmin("https://localhost:8181", sessionId);
		assertFalse(icatClient.isAdmin());
		icatClient = new IcatClientUserNotAdmin("https://localhost:8181", "bogus-session-id");
		assertFalse(icatClient.isAdmin());
	}

	@Test
	public void testGetEntities() throws Exception {
		IcatClient icatClient = new IcatClientUserIsAdmin("https://localhost:8181", sessionId);

		List<Long> ids = new ArrayList<Long>();

		List<JsonObject> results = icatClient.getEntities("investigation", ids);
		assertEquals(0, results.size());
		results = icatClient.getEntities("dataset", ids);
		assertEquals(0, results.size());
		results = icatClient.getEntities("datafile", ids);
		assertEquals(0, results.size());

		List<Long> investigationIds = new ArrayList<Long>();
		ResultSet investigations = connection.createStatement().executeQuery("SELECT * from INVESTIGATION limit 0, 1");
		investigations.next();
		investigationIds.add(investigations.getLong("ID"));

		results = icatClient.getEntities("investigation", investigationIds);
		assertEquals(1, results.size());

		List<Long> datasetIds = new ArrayList<Long>();
		ResultSet datasets = connection.createStatement().executeQuery("SELECT * from DATASET limit 0, 1");
		datasets.next();
		datasetIds.add(datasets.getLong("ID"));

		results = icatClient.getEntities("dataset", datasetIds);
		assertEquals(1, results.size());
		assertNotNull(results.get(0).getJsonObject("investigation"));

		List<Long> datafileIds = new ArrayList<Long>();
		ResultSet datafiles = connection.createStatement().executeQuery("SELECT * from DATAFILE");
		datafiles.next();
		datafileIds.add(datafiles.getLong("ID"));

		results = icatClient.getEntities("datafile", datafileIds);
		assertEquals(1, results.size());
		assertNotNull(results.get(0).getJsonObject("dataset"));
		assertNotNull(results.get(0).getJsonObject("dataset").getJsonObject("investigation"));

		for (long i = 2; i <= 10001; i++) {
			datafiles.next();
			datafileIds.add(datafiles.getLong("ID"));
		}

		results = icatClient.getEntities("datafile", datafileIds);
		assertEquals(10001, results.size());

	}

	@Test
	public void testGetFullName() throws Exception {
		IcatClient icatClient = new IcatClient("https://localhost:8181", sessionId);
		String fullName = icatClient.getFullName();

		assertNotNull(fullName);
		// assertTrue(fullName.length() > 0);
	}

	/*
	 * @Test public void testGetSize() throws Exception { IcatClient icatClient =
	 * new IcatClient("https://localhost:8181", sessionId);
	 * 
	 * List<Long> emptyIds = new ArrayList<Long>();
	 * 
	 * assertEquals((long) 0, (long) icatClient.getSize(cacheRepository, emptyIds,
	 * emptyIds, emptyIds));
	 * 
	 * List<Long> ids = new ArrayList<Long>(); ids.add((long) 1); ids.add((long) 2);
	 * ids.add((long) 3);
	 * 
	 * assertTrue(icatClient.getSize(cacheRepository, ids, emptyIds, emptyIds) >
	 * (long) 0); assertTrue(icatClient.getSize(cacheRepository, emptyIds, ids,
	 * emptyIds) > (long) 0); assertTrue(icatClient.getSize(cacheRepository,
	 * emptyIds, emptyIds, ids) > (long) 0);
	 * assertTrue(icatClient.getSize(cacheRepository, ids, ids, ids) > (long) 0); }
	 */

	private Map<String, List<Long>> createEmptyEntityTypeEntityIds() {
		Map<String, List<Long>> out = new HashMap<String, List<Long>>();
		out.put("investigation", new ArrayList<Long>());
		out.put("dataset", new ArrayList<Long>());
		out.put("datafile", new ArrayList<Long>());
		return out;
	}

	class IcatClientUserIsAdmin extends IcatClient {

		public IcatClientUserIsAdmin(String url, String sessionId) {
			super(url, sessionId);
		}

		protected String[] getAdminUserNames() throws Exception {
			return new String[] { "simple/root" };
		}
	}

	class IcatClientUserNotAdmin extends IcatClient {

		public IcatClientUserNotAdmin(String url, String sessionId) {
			super(url, sessionId);
		}

		protected String[] getAdminUserNames() throws Exception {
			return new String[] { "db/test" };
		}
	}

}