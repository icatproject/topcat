package org.icatproject.topcat.exceptions;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Parent class for all response code exceptions
 *
 */
public class TopcatException extends Exception {
    private static final long serialVersionUID = 1L;
    private final static Logger logger = LoggerFactory.getLogger(TopcatException.class);

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
