package org.icatproject.topcat.exceptions;


/**
 * Parent class for all response code exceptions
 *
 */
public class TopcatException extends Exception {
    private static final long serialVersionUID = 1L;

    private int httpStatusCode;
    private String message;

    public TopcatException(int httpStatusCode, String message) {
        this.httpStatusCode = httpStatusCode;

        this.message = message;
    }

    public String getShortMessage() {
        return message;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getMessage() {
        return "(" + httpStatusCode + ") : " + message;
    }

}
