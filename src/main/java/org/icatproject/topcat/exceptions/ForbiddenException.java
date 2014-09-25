package org.icatproject.topcat.exceptions;

import java.net.HttpURLConnection;

public class ForbiddenException extends TopcatException{
    private static final long serialVersionUID = 1L;

    public ForbiddenException(String message) {
        super(HttpURLConnection.HTTP_FORBIDDEN, message);
    }

}
