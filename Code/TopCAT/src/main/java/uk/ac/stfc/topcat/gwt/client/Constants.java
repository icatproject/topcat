package uk.ac.stfc.topcat.gwt.client;

public class Constants {

    /**
     * The MAX_DOWNLOAD_FRAMES relates to the number of __download frames in
     * TOPCATWeb.jsp
     */
    public static final int MAX_DOWNLOAD_FRAMES = 10;

    /** Name of property from properties file. */
    public static final String ACCESSIBILITY = "ACCESSIBILITY";

    /** Name of property from properties file. */
    public static final String PRIVACY_POLICY = "PRIVACY_POLICY";

    /** Name of property from properties file. */
    public static final String DATA_POLICY = "DATA_POLICY";

    /** Name of property from properties file. */
    public static final String TERMS_OF_USE = "TERMS_OF_USE";

    /** Name of property from properties file. */
    public static final String COMPLAINTS_PROCEDURE = "COMPLAINTS_PROCEDURE";

    /** Name of property from properties file. */
    public static final String FEEDBACK = "FEEDBACK";

    /** Name of property from properties file. */
    public static final String LOGO_URL = "LOGO_URL";

    /** Name of property from properties file. */
    public static final String MESSAGE = "MESSAGE";

    /** Download status */
    public static final String STATUS_IN_PROGRESS = "in progress";

    /** Download status */
    public static final String STATUS_AVAILABLE = "available";

    /** Download status */
    public static final String STATUS_EXPIRED = "expired";

    /** Download status */
    public static final String STATUS_ERROR = "ERROR";

    /** Investigation **/
    public static final String INVESTIGATION = "INVESTIGATION";

    /** Data set **/
    public static final String DATA_SET = "DATA_SET";

    /** Data file **/
    public static final String DATA_FILE = "DATA_FILE";

    /** ICAT Data Service */
    public static final String IDS = "IDS";

    /** Old download service */
    @Deprecated
    public static final String RESTFUL_DOWNLOAD = "restfulDownload";
    
    /** Url path of the IDS service (include ending /)*/
    public static final String IDS_URL_PATH = "ids/";
    
    
    /** Polling refresh interval for messages **/
    public static final int MESSAGE_REFRESH_INTERVAL = 30000;
    
}
