package org.icatproject.topcat.icatclient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.ForbiddenException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientInterface;
import org.icatproject_4_5_0.ICAT;
import org.icatproject_4_5_0.ICATService;
import org.icatproject_4_5_0.IcatException_Exception;
import org.icatproject_4_5_0.Login.Credentials;
import org.icatproject_4_5_0.Login.Credentials.Entry;

import javax.xml.namespace.QName;

/**
 * ICAT client for ICAT 4.3
 */
public class ICATClient45 implements ICATClientInterface {
    private ICAT service;

    private final static Logger logger = Logger.getLogger(ICATClient45.class);

    public ICATClient45(String serverURL) throws MalformedURLException {
        logger.info("ICATInterfacev43: serverURL (" + serverURL + ")");

        if (!serverURL.matches(".*/ICATService/ICAT\\?wsdl$")) {
            if (serverURL.matches(".*/$")) {
                serverURL = serverURL + "ICATService/ICAT?wsdl";
            } else {
                serverURL = serverURL + "/ICATService/ICAT?wsdl";
            }
        }
        URL url = new URL(serverURL);

        service = new ICATService(url, new QName("http://icatproject.org", "ICATService")).getICATPort();
    }

    /**
     * Authenticate against an ICAT service and return an icat session id
     *
     * @param authenticationType the authentication type name
     * @param parameters the map of parameters (username and password)
     * @return String the icat session id
     *
     * @throws AuthenticationException  the authentication exception
     * @throws InternalException the internal exception
     *
     */
    @Override
    public String login(String authenticationType, Map<String, String> parameters) throws AuthenticationException, InternalException {
        logger.info("login: authenticationType (" + authenticationType + "), number of parameters " + parameters.size());

        String result = new String();

        try {
            Credentials credentials = new Credentials();
            List<Entry> entries = credentials.getEntry();
            for (String key : parameters.keySet()) {
                Entry entry = new Entry();
                entry.setKey(key);
                entry.setValue(parameters.get(key));
                entries.add(entry);
            }
            result = service.login(authenticationType, credentials);
        } catch (IcatException_Exception ex) {
            logger.debug("IcatException_Exception:" + ex.getMessage());
            throw new AuthenticationException(ex.getMessage());
        } catch (javax.xml.ws.WebServiceException ex) {
            logger.debug("login: WebServiceException:" + ex.getMessage());
            throw new InternalException("ICAT Server not available");
        }

        logger.info("login: result(" + result + ")");

        return result;
    }

    /**
     * Returns the user name as used by icat
     *
     * @param icatSessionId the icat session id
     * @return the icat user name
     * @throws TopcatException
     */
    @Override
    public String getUserName(String icatSessionId) throws TopcatException {
        String result = null;

        try {
            result = service.getUserName(icatSessionId);
        } catch (IcatException_Exception e) {
            throwNewICATException(e);
        }

        return result;

    }


    /**
     * Returns whether a session is valid or not
     *
     * @param icatSessionId the icat session id
     * @return whether the session is valid or not
     */
    @Override
    public Boolean isSessionValid(String icatSessionId) throws IcatException {
        Double result = null;

        try {
            result = service.getRemainingMinutes(icatSessionId);
        } catch (IcatException_Exception e) {
            logger.info("isSessionValid: " + e.getMessage());

            return false;
            //throw new IcatException(e.getMessage());
        }

        if (result > 0) {
            return true;
        }

        return false;
    }


    /**
     * Returns the number of minutes remaining for a session
     *
     * @param icatSessionId the icat session id
     * @return the remaining minutes of the session. -1 if session is not valid
     *          or if other icat server error is thrown
     */
    @Override
    public Long getRemainingMinutes(String icatSessionId) throws IcatException {
        Double result = null;

        try {
            result = service.getRemainingMinutes(icatSessionId);
        } catch (IcatException_Exception e) {
            return -1L;
        }

        return result.longValue();
    }

    /**
     * Resets the time-to-live of the icat session
     *
     * @param icatSessionId the icat session id
     * @throws TopcatException
     *
     */
    @Override
    public void refresh(String icatSessionId) throws TopcatException {
        try {
            service.refresh(icatSessionId);
        } catch (IcatException_Exception e) {
            logger.info("logout: " + e.getMessage());
            throwNewICATException(e);
        }

    }

    /**
     * This invalidates the sessionId.
     *
     * @param icatSessionId the icat session id
     * @throws TopcatException
     *
     */
    @Override
    public void logout(String icatSessionId) throws TopcatException {
        try {
            service.logout(icatSessionId);
        } catch (IcatException_Exception e) {
            logger.info("logout: " + e.getMessage());
            throwNewICATException(e);
        }

    }


    //TODO
    /**
     * Determine if an IcatException_Exception is an INSUFFICIENT_PRIVILEGES or SESSION type
     * exception
     *
     * @param e
     * @throws AuthenticationException
     * @throws IcatException
     * @throws ForbiddenException
     * @throws BadRequestException
     * @throws NotFoundException
     */
    private void throwNewICATException(IcatException_Exception e) throws TopcatException {
        logger.debug("icatExceptionType: " + e.getFaultInfo().getType());

        switch(e.getFaultInfo().getType()) {
        case INSUFFICIENT_PRIVILEGES:
            throw new ForbiddenException(e.getMessage());
        case BAD_PARAMETER:
            throw new BadRequestException(e.getMessage());
        case INTERNAL:
            throw new IcatException(e.getMessage());
        case SESSION:
            throw new AuthenticationException(e.getMessage());
        case NO_SUCH_OBJECT_FOUND:
            throw new NotFoundException(e.getMessage());
        case VALIDATION:
            throw new IcatException(e.getMessage());
        default:
            throw new IcatException(e.getMessage());
        }

        /*
        if (e.getFaultInfo().getType() == IcatExceptionType.INSUFFICIENT_PRIVILEGES || e.getFaultInfo().getType() == IcatExceptionType.SESSION) {
            throw new AuthenticationException(e.getMessage());
        } else {
            throw new IcatException(e.getMessage());
        }
        */


    }


}
