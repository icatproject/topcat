package org.icatproject.topcat.httpclient;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Response {

	private static final Logger logger = LoggerFactory.getLogger(Response.class);

	private Integer code;
	private Map<String, String> headers;
	private String body;

	public Response(Integer code, Map<String, String> headers, String body){
		this.code = code;
		this.headers = headers;
		this.body = body;
	}

	public Integer getCode(){
		return code;
	}

	public Map<String, String> getHeaders(){
		return headers;
	}

	public String getHeader(String name){
		return headers.get(name);
	}

	public String toString(){
		return body;
	}

}