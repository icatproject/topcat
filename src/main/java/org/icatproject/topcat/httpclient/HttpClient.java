package org.icatproject.topcat.httpclient;

import java.util.Map;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonValue;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;

import java.io.*;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpClient {

	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

	private String url;

	public HttpClient(String url){
		this.url = url;
	}

	public Integer urlLength(String offset){
		return (this.url + "/" + offset).length();
	}

	public Response get(String offset, Map<String, String> headers) throws Exception {
		return send("GET", offset, headers);
	}

	public Response post(String offset, Map<String, String> headers, String data) throws Exception {
		return send("POST", offset, headers, data);
	}

	public Response delete(String offset, Map<String, String> headers) throws Exception {
		return send("DELETE", offset, headers);
	}

	public Response put(String offset, Map<String, String> headers, String data) throws Exception {
		return send("PUT", offset, headers, data);
	}

	public Response head(String offset, Map<String, String> headers) throws Exception {
		return send("HEAD", offset, headers);
	}

	private Response send(String method, String offset, Map<String, String> headers, String body) throws Exception {
		StringBuilder url = new StringBuilder(this.url + "/" + offset);

		HttpURLConnection connection = null;

		try {
		    //Create connection
		    connection = (HttpURLConnection) (new URL(url.toString())).openConnection();
		    connection.setRequestMethod(method);
    		connection.setUseCaches(false);
    		connection.setDoInput(true);

    		for(Map.Entry<String, String> entry : headers.entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}

    		if(body != null && (method.equals("POST") || method.equals("PUT"))){
    			connection.setDoOutput(true);
    			connection.setRequestProperty("Content-Length", Integer.toString(body.toString().getBytes().length));

	    		DataOutputStream request = new DataOutputStream(connection.getOutputStream());
	    		request.writeBytes(body.toString());
	    		request.close();
	    	}

	    	Integer responseCode = connection.getResponseCode();

	    	Map<String, String> responseHeaders = new HashMap();
	    	for(String key : connection.getHeaderFields().keySet()){
	    		responseHeaders.put(key, connection.getHeaderField(key));
	    	}

	    	String responseBody = "";
	    	try {
	    		responseBody = inputStreamToString(connection.getInputStream());
	    	} catch(Exception e1){
	    		try {
	    			responseBody = inputStreamToString(connection.getErrorStream());
	    		} catch(Exception e2){}
	    	}

	    	if(responseCode >= 400){
	    		logger.info("send error: " + method + " " + url.toString() + " -> (" + responseCode + ") " + responseBody);
	    	}

		    return new Response(responseCode, responseHeaders, responseBody);
    	} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private Response send(String method, String offset, Map<String, String> headers) throws Exception {
		return send(method, offset, headers, null);
	}

	private String inputStreamToString(InputStream inputStream) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	    StringBuilder out = new StringBuilder();
	    int currentChar;
	    while ((currentChar = bufferedReader.read()) > -1) {
	    	out.append(Character.toChars(currentChar));
	    }
	    bufferedReader.close();
	    return out.toString();
	}


}