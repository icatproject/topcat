package org.icatproject.topcat;

import java.util.*;
import java.io.File;
import java.lang.reflect.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import javax.inject.Inject;

import javax.json.*;
import javax.ws.rs.core.Response;
import javax.ejb.EJB;

import org.icatproject.topcat.httpclient.HttpClient;
import org.icatproject.topcat.domain.*;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.ForbiddenException;

import java.net.URLEncoder;

import org.icatproject.topcat.repository.CacheRepository;
import org.icatproject.topcat.repository.ConfVarRepository;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.repository.DownloadTypeRepository;
import org.icatproject.topcat.web.rest.AdminResource;

import java.sql.*;

@RunWith(Arquillian.class)
public class AdminResourceTest {

	/*
	 * CURRENT STATUS: I can only get this to work if I put a cobbled copy of
	 * topcat.properties in the root folder. All attempts to use addAsResource()
	 * have failed, and I'm fed up trying to guess (from several million online
	 * examples) how it should be used correctly!
	 *
	 * Of course, these are not unit tests, but use an embedded container and a
	 * local ICAT/IDS which we assume to be populated appropriately.
	 */

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create(JavaArchive.class)
				.addClasses(AdminResource.class, CacheRepository.class, DownloadRepository.class,
						DownloadTypeRepository.class, ConfVarRepository.class)
				.addPackages(true, "org.icatproject.topcat.domain", "org.icatproject.topcat.exceptions")
				.addAsResource("META-INF/persistence.xml")
				// .addAsResource("topcat.properties")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
	}

	@EJB
	private DownloadRepository downloadRepository;

	@EJB
	private DownloadTypeRepository downloadTypeRepository;

	@EJB
	private ConfVarRepository confVarRepository;

	@EJB
	private CacheRepository cacheRepository;

	@Inject
	private AdminResource adminResource;

	private static String adminSessionId;
	private static String nonAdminSessionId;

	@BeforeClass
	public static void beforeAll() {
		TestHelpers.installTrustManager();
	}

	@Before
	public void setup() throws Exception {
		HttpClient httpClient = new HttpClient("https://localhost:8181/icat");

		// Log in as an admin user

		String data = "json=" + URLEncoder.encode(
				"{\"plugin\":\"simple\", \"credentials\":[{\"username\":\"root\"}, {\"password\":\"root\"}]}", "UTF8");
		String response = httpClient.post("session", new HashMap<String, String>(), data).toString();
		adminSessionId = Utils.parseJsonObject(response).getString("sessionId");

		// Also log in as a non-admin user

		data = "json=" + URLEncoder.encode(
				"{\"plugin\":\"simple\", \"credentials\":[{\"username\":\"nonroot\"}, {\"password\":\"nonroot\"}]}",
				"UTF8");
		response = httpClient.post("session", new HashMap<String, String>(), data).toString();
		nonAdminSessionId = Utils.parseJsonObject(response).getString("sessionId");
	}

	@Test
	public void testIsValidSession() throws Exception {
		String facilityName = "LILS";
		Response response;

		// Should be true for an admin user

		response = adminResource.isValidSession(facilityName, adminSessionId);
		assertEquals(200, response.getStatus());
		assertEquals("true", (String) response.getEntity());

		// Should be false for a non-admin (but valid) user

		response = adminResource.isValidSession(facilityName, nonAdminSessionId);
		assertEquals(200, response.getStatus());
		assertEquals("false", (String) response.getEntity());

		// Request should fail for a nonsense sessionId
		response = adminResource.isValidSession(facilityName, "nonsense id");
		// Expected this to raise a BadRequestException, but surprisingly it doesn't
		assertEquals(200, response.getStatus());
		assertEquals("false", (String) response.getEntity());
	}

	@Test
	public void testDownloadAPI() throws Exception {
		String facilityName = "LILS";
		Response response;
		JsonObject json;
		List<Download> downloads;

		// Create a new download for the test
		Download testDownload = new Download();
		testDownload.setFacilityName(facilityName);
		testDownload.setSessionId(adminSessionId);
		testDownload.setStatus(DownloadStatus.PREPARING);
		testDownload.setIsDeleted(false);

		// Other fields that can't be null, but which we (hopefully) don't really need:
		testDownload.setUserName("simple/root");
		testDownload.setFileName("testFile.txt");
		testDownload.setTransport("http");

		downloadRepository.save(testDownload);

		// Get the current downloads - may not be empty
		// It appears queryOffset cannot be empty!
		String queryOffset = "1 = 1";
		response = adminResource.getDownloads(facilityName, adminSessionId, queryOffset);
		assertEquals(200, response.getStatus());

		downloads = (List<Download>) response.getEntity();

		// Check that the result tallies with the DownloadRepository contents

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("queryOffset", queryOffset);
		List<Download> repoDownloads = new ArrayList<Download>();
		repoDownloads = downloadRepository.getDownloads(params);

		assertEquals(repoDownloads.size(), downloads.size());
		for (Download download : repoDownloads) {
			assertNotNull(findDownload(downloads, download.getId()));
		}

		// Next, change the download status. Must be different from the current status!
		String downloadStatus = "EXPIRED";
		if (testDownload.getStatus().equals(DownloadStatus.valueOf(downloadStatus))) {
			downloadStatus = "PAUSED";
		}

		response = adminResource.setDownloadStatus(testDownload.getId(), facilityName, adminSessionId, downloadStatus);
		assertEquals(200, response.getStatus());

		// and test that the new status has been set

		response = adminResource.getDownloads(facilityName, adminSessionId, queryOffset);
		assertEquals(200, response.getStatus());
		downloads = (List<Download>) response.getEntity();

		testDownload = findDownload(downloads, testDownload.getId());

		// To be thorough, we ought to check that ONLY the status field has changed. Not
		// going to!
		assertEquals(DownloadStatus.valueOf(downloadStatus), testDownload.getStatus());

		// Now toggle the deleted status - may have been deleted already!
		Boolean currentDeleted = testDownload.getIsDeleted();

		response = adminResource.deleteDownload(testDownload.getId(), facilityName, adminSessionId, !currentDeleted);
		assertEquals(200, response.getStatus());

		// and check that it has worked (again, not bothering to check that nothing else
		// has changed)

		response = adminResource.getDownloads(facilityName, adminSessionId, queryOffset);
		assertEquals(200, response.getStatus());
		downloads = (List<Download>) response.getEntity();

		testDownload = findDownload(downloads, testDownload.getId());
		assertTrue(testDownload.getIsDeleted() != currentDeleted);

		// Test that getDownloadStatus() etc. produce an error response for a non-admin
		// user

		try {
			response = adminResource.getDownloads(facilityName, nonAdminSessionId, queryOffset);
			// We should not see the following
			System.out.println("DEBUG: AdminRT.getDownloads response: " + response.getStatus() + ", "
					+ (String) response.getEntity());
			fail("AdminResource.getDownloads did not raise exception for non-admin user");
		} catch (ForbiddenException fe) {
			assertTrue(true);
		}

		try {
			response = adminResource.setDownloadStatus(testDownload.getId(), facilityName, nonAdminSessionId,
					downloadStatus);
			// We should not see the following
			System.out.println("DEBUG: AdminRT.setDownloadStatus response: " + response.getStatus() + ", "
					+ (String) response.getEntity());
			fail("AdminResource.setDownloadStatus did not raise exception for non-admin user");
		} catch (ForbiddenException fe) {
			assertTrue(true);
		}

		try {
			response = adminResource.deleteDownload(testDownload.getId(), facilityName, nonAdminSessionId,
					!currentDeleted);
			// We should not see the following
			System.out.println("DEBUG: AdminRT.deleteDownload response: " + response.getStatus() + ", "
					+ (String) response.getEntity());
			fail("AdminResource.deleteDownload did not raise exception for non-admin user");
		} catch (ForbiddenException fe) {
			assertTrue(true);
		}

		// Remove the test download from the repository
		downloadRepository.removeDownload(testDownload.getId());
	}

	@Test
	public void testSetDownloadTypeStatus() throws Exception {

		String facilityName = "LILS";
		String downloadType = "http";
		Response response;
		JsonObject json;

		// Determine the current download type status; remember, it may not be set at
		// all

		Boolean disabled = false;
		String message = "";
		DownloadType dt = downloadTypeRepository.getDownloadType(facilityName, downloadType);

		if (dt != null) {
			disabled = dt.getDisabled();
			message = dt.getMessage();
			System.out.println("DEBUG: AdminRT: initial download type status is {" + disabled + "," + message + "}");
		} else {
			System.out.println("DEBUG: AdminRT: initial download type status not found");
		}

		if (message.length() == 0) {
			message = "Disabled for testing";
		}

		// Toggle the disabled status

		response = adminResource.setDownloadTypeStatus(downloadType, facilityName, adminSessionId, !disabled, message);
		assertEquals(200, response.getStatus());

		// Now test that it has had the desired result

		dt = downloadTypeRepository.getDownloadType(facilityName, downloadType);

		assertNotNull(dt);
		if (dt != null) {
			System.out.println(
					"DEBUG: AdminRT final download type status is {" + dt.getDisabled() + "," + dt.getMessage() + "}");
			assertTrue(disabled != dt.getDisabled());
			assertEquals(message, dt.getMessage());
		}

		// Test that setDownloadTypeStatus produces an error for non-admin users.

		try {
			response = adminResource.setDownloadTypeStatus(downloadType, facilityName, nonAdminSessionId, !disabled,
					message);
			// We should not see the following
			System.out.println("DEBUG: AdminRT.setDownloadTypeStatus response: " + response.getStatus() + ", "
					+ (String) response.getEntity());
			fail("AdminResource.setDownloadTypeStatus did not raise exception for non-admin user");
		} catch (ForbiddenException fe) {
			assertTrue(true);
		}

		// Finally, ought to reset the disabled status to the original value!
		// (Though we could have used a dummy download type for the test...)
		// However, this won't reset an originally-empty message.

		if (dt != null) {
			response = adminResource.setDownloadTypeStatus(downloadType, facilityName, adminSessionId, disabled,
					message);
			assertEquals(200, response.getStatus());
		} else {
			// There was no entry for this download type previously. As the status is false
			// by default, make sure that's what we set it to now!
			response = adminResource.setDownloadTypeStatus(downloadType, facilityName, adminSessionId, false, message);
			assertEquals(200, response.getStatus());
		}
	}

	@Test
	public void testClearCachedSize() throws Exception {

		String facilityName = "LILS";
		String entityType = "dataset";
		Long id = 3L;
		Long size = 150L;
		String key = "getSize:" + entityType + ":" + id;
		Response response;

		// Set a dummy size (this will overwrite any existing cached value!)

		cacheRepository.put(key, size);

		// Now use the API to clear it
		response = adminResource.clearCachedSize(entityType, id, facilityName, adminSessionId);
		assertEquals(200, response.getStatus());

		// and now it should be gone from the repository
		assertNull(cacheRepository.get(key));

		// Test behaviour for non-admin user
		try {
			response = adminResource.clearCachedSize(entityType, id, facilityName, nonAdminSessionId);
			// We should not see the following
			System.out.println("DEBUG: AdminRT.clearCachedSize response: " + response.getStatus() + ", "
					+ (String) response.getEntity());
			fail("AdminResource.clearCachedSize did not raise exception for non-admin user");
		} catch (ForbiddenException fe) {
			assertTrue(true);
		}
	}

	@Test
	public void testSetConfVar() throws Exception {

		String facilityName = "LILS";
		String name = "testVar";
		String value = "test value";
		Response response;

		// First, check whether the confVar is set to this value already - if so, choose
		// a different value
		if (value.equals(confVarRepository.getConfVar(name))) {
			value = "different test value";
		}

		response = adminResource.setConfVar(name, facilityName, adminSessionId, value);
		assertEquals(200, response.getStatus());

		assertEquals(value, confVarRepository.getConfVar(name).getValue());

		// Test behaviour for non-admin user
		try {
			response = adminResource.setConfVar(name, facilityName, nonAdminSessionId, value);
			// We should not see the following
			System.out.println("DEBUG: AdminRT.setConfVar response: " + response.getStatus() + ", "
					+ (String) response.getEntity());
			fail("AdminResource.setConfVar did not raise exception for non-admin user");
		} catch (ForbiddenException fe) {
			assertTrue(true);
		}
	}

	private Download findDownload(List<Download> downloads, Long downloadId) {

		for (Download download : downloads) {
			if (download.getId() == downloadId)
				return download;
		}
		return null;
	}

}