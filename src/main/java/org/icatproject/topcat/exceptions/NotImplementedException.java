package org.icatproject.topcat.exceptions;

import java.net.HttpURLConnection;

public class NotImplementedException extends TopcatException{
    private static final long serialVersionUID = 1L;

    public NotImplementedException(String message) {
        super(HttpURLConnection.HTTP_NOT_IMPLEMENTED, message);
    }

}
