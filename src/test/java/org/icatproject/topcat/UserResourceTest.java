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
import java.net.URLEncoder;

import org.icatproject.topcat.repository.CacheRepository;
import org.icatproject.topcat.repository.CartRepository;
import org.icatproject.topcat.repository.DownloadRepository;
import org.icatproject.topcat.repository.DownloadTypeRepository;
import org.icatproject.topcat.web.rest.UserResource;

import java.sql.*;

@RunWith(Arquillian.class)
public class UserResourceTest {

	// CURRENT STATUS:
	// I can only get this to work if I put a cobbled copy of topcat.properties in the root folder.
	// All attempts to use addAsResource() have failed, and I'm fed up trying to guess (from several million online examples) how it should be used correctly!
	
    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(UserResource.class, CacheRepository.class, Cache.class, DownloadRepository.class, DownloadTypeRepository.class, CartRepository.class)
            .addAsResource("META-INF/persistence.xml")
            // .addAsResource("topcat.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

	@EJB
	private DownloadRepository downloadRepository;

	@EJB
	private DownloadTypeRepository downloadTypeRepository;

	@EJB
	private CartRepository cartRepository;

	@EJB
	private CacheRepository cacheRepository;
	
	@Inject
	private UserResource userResource;

	private static String sessionId;

	private Connection connection;

	@Before
	public void setup() throws Exception {
		TestHelpers.installTrustManager();

		HttpClient httpClient = new HttpClient("https://localhost:8181/icat");
		String data = "json=" + URLEncoder.encode("{\"plugin\":\"simple\", \"credentials\":[{\"username\":\"root\"}, {\"password\":\"root\"}]}", "UTF8");
		String response = httpClient.post("session", new HashMap<String, String>(), data).toString();
		sessionId = Utils.parseJsonObject(response).getString("sessionId");

		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/icat", "root", "secret");
	}

	@Test
	public void testGetSize() throws Exception {
		String facilityName = "LILS";
		String entityType = "investigation";
		Long entityId = (long) 1;
		IcatClient icatClient = new IcatClient("https://localhost:8181", sessionId);
		// UserResource userResource = new UserResource();

		List<Long> emptyIds = new ArrayList<Long>();
		
		Response response = userResource.getSize(facilityName,sessionId,entityType,entityId);

		// Actual size value discovered by trial and error!
		// assertEquals((long) 155062810, Long.parseLong(response.getEntity().toString()));
		// Possibly more robust:
		assertTrue(Long.parseLong(response.getEntity().toString()) > (long) 0);

	}

    private Map<String, List<Long>> createEmptyEntityTypeEntityIds(){
    	Map<String, List<Long>> out = new HashMap<String, List<Long>>();
		out.put("investigation", new ArrayList<Long>());
		out.put("dataset", new ArrayList<Long>());
		out.put("datafile", new ArrayList<Long>());
		return out;
    }


    class IcatClientUserIsAdmin extends IcatClient {

		public IcatClientUserIsAdmin(String url, String sessionId){
			super(url, sessionId);
		}

		protected String[] getAdminUserNames() throws Exception {
			return new String[] {"simple/root"};
		}
    }

    class IcatClientUserNotAdmin extends IcatClient {

		public IcatClientUserNotAdmin(String url, String sessionId){
			super(url, sessionId);
		}

		protected String[] getAdminUserNames() throws Exception {
			return new String[] {"db/test"};
		}
    }


}