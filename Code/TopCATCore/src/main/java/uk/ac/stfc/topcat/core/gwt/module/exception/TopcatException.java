package uk.ac.stfc.topcat.core.gwt.module.exception;

@SuppressWarnings("serial")
public class TopcatException extends Exception {

    /**
     * Fault detail element.
     * 
     */
    private TopcatException faultInfo;

    public TopcatException() {
        super();
    }

    public TopcatException(String msg) {
        super(msg);
    }

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public TopcatException(String message, TopcatException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public TopcatException(String message, TopcatException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return returns fault bean:
     *         uk.ac.stfc.topcat.icatclient.v410.TopcatException
     */
    public TopcatException getFaultInfo() {
        return faultInfo;
    }

}
