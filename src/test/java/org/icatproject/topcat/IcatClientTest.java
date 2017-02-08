package org.icatproject.topcat;

import java.util.*;
import java.lang.reflect.*;

import static org.junit.Assert.*;
import org.junit.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import javax.json.*;

import org.icatproject.topcat.httpclient.HttpClient;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IcatClientTest {

	private Logger logger = LoggerFactory.getLogger(IcatClientTest.class);
	private static IcatClient icatClient;
	private static String sessionId;

	@Before
	public void setup() throws Exception {
		TestHelpers.installTrustManager();

		icatClient = new IcatClient("https://localhost:8181");

		HttpClient httpClient = new HttpClient("https://localhost:8181/icat");
		String data = "json=" + URLEncoder.encode("{\"plugin\":\"simple\", \"credentials\":[{\"username\":\"root\"}, {\"password\":\"root\"}]}", "UTF8");
		String response = httpClient.post("session", new HashMap<String, String>(), data).toString();
		logger.info("response: " + response);
		sessionId = parseJsonObject(response).getString("sessionId");
	}

	@Test
	public void testGetUserName() throws Exception {
		assertEquals("simple/root", icatClient.getUserName(sessionId));
	}

	private JsonObject parseJsonObject(String json) throws Exception {
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        JsonReader jsonReader = Json.createReader(jsonInputStream);
        JsonObject out = jsonReader.readObject();
        jsonReader.close();
        return out;
    }

}