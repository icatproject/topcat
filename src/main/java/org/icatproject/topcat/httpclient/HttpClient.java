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
import org.icatproject.topcat.Utils;

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

	public Response get(String offset, Map<String, String> headers, int readTimeout) throws Exception {
		return send("GET", offset, headers, null, readTimeout);
	}

	public Response get(String offset, Map<String, String> headers) throws Exception {
		return get(offset, headers, -1);
	}

	public Response post(String offset, Map<String, String> headers, String data, int readTimeout) throws Exception {
		return send("POST", offset, headers, data, readTimeout);
	}

	public Response post(String offset, Map<String, String> headers, String data) throws Exception {
		return post(offset, headers, data, -1);
	}

	public Response delete(String offset, Map<String, String> headers, int readTimeout) throws Exception {
		return send("DELETE", offset, headers, null, readTimeout);
	}

	public Response delete(String offset, Map<String, String> headers) throws Exception {
		return delete(offset, headers, -1);
	}

	public Response put(String offset, Map<String, String> headers, String data, int readTimeout) throws Exception {
		return send("PUT", offset, headers, data, readTimeout);
	}

	public Response put(String offset, Map<String, String> headers, String data) throws Exception {
		return put(offset, headers, data, -1);
	}

	public Response head(String offset, Map<String, String> headers, int readTimeout) throws Exception {
		return send("HEAD", offset, headers, null, readTimeout);
	}

	public Response head(String offset, Map<String, String> headers) throws Exception {
		return head(offset, headers, -1);
	}

	private Response send(String method, String offset, Map<String, String> headers, String body, int readTimeout) throws Exception {
		StringBuilder url = new StringBuilder(this.url + "/" + offset);

		HttpURLConnection connection = null;

		try {
		    //Create connection
		    connection = (HttpURLConnection) (new URL(url.toString())).openConnection();
		    connection.setRequestMethod(method);
    		connection.setUseCaches(false);
    		connection.setDoInput(true);
    		if(readTimeout > -1){
    			connection.setReadTimeout(readTimeout);
    		}

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
	    		responseBody = Utils.inputStreamToString(connection.getInputStream());
	    	} catch(Exception e1){
	    		try {
	    			responseBody = Utils.inputStreamToString(connection.getErrorStream());
	    		} catch(Exception e2){
	    			logger.info("send: error reading input/error for: " + method + " " + url.toString() + " -> (" + responseCode + "): " + e2.getMessage());
	    		}
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

}