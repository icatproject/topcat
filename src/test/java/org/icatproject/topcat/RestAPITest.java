package org.icatproject.topcat;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJB;

import org.apache.commons.io.IOUtils;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.icatclient.ICATClientFactory;
import org.icatproject.topcat.icatclient.ICATClientInterface;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.jayway.restassured.RestAssured;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import static com.jayway.restassured.config.SSLConfig.*;


@UsingDataSet("download.json")
@RunWith(Arquillian.class)
public class RestAPITest {
    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackages(true, "org.icatproject")
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            .setWebXML(new File("src/main/webapp/WEB-INF", "/web.xml"));

    }

    @EJB
    private ICATClientBean icatClientService;

    @ArquillianResource
    protected URL appUrl;

    private static Properties prop;
    private String restAPIUrl;
    private static String icatUrl;

    @BeforeClass
    public static void setup() throws MalformedURLException {
        prop = new Properties();
        InputStream is = RestAPITest.class.getClassLoader().getResourceAsStream("icatserver.properties");
        try {
            prop.load(is);
        } catch (Exception e) {
            System.out.println("Problem loading icatserver.properties: " + e.getClass() + " " + e.getMessage());
        }

        icatUrl = prop.getProperty("icat.server.url");

    }

    @Before
    public void prepareRestAPITest() throws Exception {
        //RestAssured.basePath = "/webapi/v1";
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.config = RestAssured.config().sslConfig(sslConfig().allowAllHostnames());
        //RestAssured.config = RestAssuredConfig.config().redirect(new RedirectConfig().followRedirects(true).allowCircularRedirects(true));

        restAPIUrl = appUrl.toString() + "api/v1";

        System.out.println("restAPIUrl: " + restAPIUrl);
    }


    @Test
    public void ping() {
        get(restAPIUrl + "/ping").then().statusCode(200);

        String ping = get(restAPIUrl + "/ping").asString();
        assertEquals("{\"value\":\"ok\"}", ping);
    }


    @Test
    public void return404() {
        get(restAPIUrl + "/404").then().statusCode(404);
    }


    @Test
    public void getEmptyCart() {
        get(restAPIUrl + "/admin/downloads/facility/sig?userName=vcf21513").then().log().all().statusCode(200).body("$", hasSize(0));
    }


    @Test
    public void getUserDownloadWithNoSessionIdOrIcatUrl() {
        get(restAPIUrl + "/downloads/facility/sig?userName=vcf21513").then().statusCode(400).body("message", equalTo("sessionId query parameter is required"));
    }

    @Test
    public void getUserDownloadWithInvalidSessionId() {
        get(restAPIUrl + "/downloads/facility/sig?sessionId=123456&userName=vcf21513").then().statusCode(400).body("message", equalTo("icatUrl query parameter is required"));
    }

    @Test
    public void getUserDownloadWithInvalidSessionIdAndIcatUrl() throws UnsupportedEncodingException {
        get(restAPIUrl + "/downloads/facility/sig?sessionId=123456&icatUrl=" + icatUrl + "&userName=vcf21513").then().statusCode(403).body("message", equalTo("sessionId not valid"));
    }

    @Test
    public void getUserDownloadWithValidSessionIdAndIcatUrlWithoutData() throws UnsupportedEncodingException, MalformedURLException {
        String sessionId = getIcatSession();

        System.out.println("sessionId:" + sessionId + " icatUrl: " + icatUrl);


        get(restAPIUrl + "/downloads/facility/sig?sessionId="+ sessionId + "&icatUrl=" + icatUrl + "&userName=vcf21513").then().statusCode(200).body("$", hasSize(0));
    }


    //@Test
    public void getUserDownloadWithValidSessionIdAndIcatUrlWithData() throws IOException {
        //check database has data
        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", "dls");
        params.put("userName", "vcf21513");

        //get(restAPIUrl + "/generate-fixture/download").then().statusCode(200).body("value", equalTo(20));

        //this.loadSingleCart();


        //List<Download> downloads = downloadRepository.getDownloadsByFacilityName(params);
        //assertTrue("has data", downloads.size() > 0);


        String sessionId = getIcatSession();

        System.out.println("sessionId:" + sessionId + " icatUrl: " + icatUrl);

        //get(restAPIUrl + "/downloads/facility/sig/user/wayne?sessionId="+ sessionId + "&icatUrl=" + icatUrl).then().statusCode(200).body("$", hasSize(0));
    }

    //@Test
    public void loadSingleCart() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();

        String json = IOUtils.toString(classLoader.getResourceAsStream("datasets/single_cart.json"));

        System.out.println(json);

        given().contentType("application/json").body(json).when().post("/cart").then().statusCode(200);

    }




    private String getIcatSession() throws MalformedURLException {
        String authenticationType = prop.getProperty("icat.login.authentication.type");
        String username = prop.getProperty("icat.login.username");
        String password = prop.getProperty("icat.login.password");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);

        String icatSessionId = null;
        ICATClientInterface service = ICATClientFactory.getInstance().createICATClient(prop.getProperty("icat.server.version"), prop.getProperty("icat.server.url"));

        try {
            icatSessionId = service.login(authenticationType, parameters);
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        } catch (InternalException e) {
            fail(e.getMessage());
        }

        System.out.println("Test global icatsessionId: " + icatSessionId);

        return icatSessionId;
    }


}
