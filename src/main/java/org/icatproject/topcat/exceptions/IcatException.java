package org.icatproject.topcat.exceptions;

import java.net.HttpURLConnection;

public class IcatException extends TopcatException{
    private static final long serialVersionUID = 1L;

    public IcatException(String message) {
        super(HttpURLConnection.HTTP_INTERNAL_ERROR, message);
    }

}
