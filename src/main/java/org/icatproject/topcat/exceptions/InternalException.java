package org.icatproject.topcat.exceptions;

import java.net.HttpURLConnection;

public class InternalException extends TopcatException{
    private static final long serialVersionUID = 1L;

    public InternalException(String message) {
        super(HttpURLConnection.HTTP_INTERNAL_ERROR, message);
    }

}
