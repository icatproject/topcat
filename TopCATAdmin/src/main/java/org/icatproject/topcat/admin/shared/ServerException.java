package org.icatproject.topcat.admin.shared;

@SuppressWarnings("serial")
public class ServerException extends Exception {

	// Needed by GWT serialization
	@SuppressWarnings("unused")
	private ServerException() {
		super();
	}
	
	public ServerException(String msg) {
		super(msg);
	}

}
