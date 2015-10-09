package org.icatproject.topcat.exceptions;

import java.net.HttpURLConnection;

public class AuthenticationException extends TopcatException{
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(HttpURLConnection.HTTP_UNAUTHORIZED, message);
    }

}
