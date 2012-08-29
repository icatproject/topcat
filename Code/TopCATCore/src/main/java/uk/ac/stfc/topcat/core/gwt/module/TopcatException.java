package uk.ac.stfc.topcat.core.gwt.module;

@SuppressWarnings("serial")
public class TopcatException extends Exception {

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private TopcatException faultInfo;
    private TopcatExceptionType type;

    public TopcatException() {
        super();
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

    public TopcatException(String message, TopcatExceptionType type) {
        super(message);
        this.type = type;
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

    /**
     * 
     * @return returns type
     */
    public TopcatExceptionType getType() {
        return type;
    }

}
