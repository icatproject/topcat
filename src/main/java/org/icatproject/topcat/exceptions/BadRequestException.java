package org.icatproject.topcat.exceptions;

import java.net.HttpURLConnection;

public class BadRequestException extends TopcatException{
    private static final long serialVersionUID = 1L;

    public BadRequestException(String message) {
        super(HttpURLConnection.HTTP_BAD_REQUEST, message);
    }

}
