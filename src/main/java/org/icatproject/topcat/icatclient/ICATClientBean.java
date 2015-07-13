package org.icatproject.topcat.icatclient;

import java.net.MalformedURLException;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import org.icatproject.topcat.Constants;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.repository.DownloadRepository;

@Stateless
public class ICATClientBean {
    @EJB
    private DownloadRepository downloadRepository;

    /**
     * login to an icat server
     *
     * @param icatUrl the server url
     * @param authenticationType the authentication type
     * @param parameters
     * @return the icat session id
     * @throws MalformedURLException
     * @throws AuthenticationException
     * @throws InternalException
     */
    public String login(String icatUrl, String authenticationType, Map<String, String> parameters) throws MalformedURLException, AuthenticationException, InternalException {
        ICATClientInterface service = getIcatService(icatUrl);

        return service.login(authenticationType, parameters);
    }

    /**
     * Check if icatSessionId is valid on a server
     *
     * @param icatUrl the server url
     * @param icatSessionId the icat session id
     * @return whether the session id valid or not
     * @throws MalformedURLException
     * @throws TopcatException
     */
    public boolean isSessionValid(String icatUrl, String icatSessionId) throws MalformedURLException, TopcatException {
        ICATClientInterface service = getIcatService(icatUrl);

        return service.isSessionValid(icatSessionId);
    }


    /**
     * Returns the remaining minutes of a icat session
     *
     * @param icatUrl the server url
     * @param icatSessionId the icat session id
     * @return the remaining minutes or -1 if session is no valid
     * @throws MalformedURLException
     * @throws TopcatException
     */
    public Long getRemainingMinutes(String icatUrl, String icatSessionId) throws MalformedURLException, TopcatException {
        ICATClientInterface service = getIcatService(icatUrl);

        return service.getRemainingMinutes(icatSessionId);
    }


    /**
     * Returns an icat service for a given server name
     *
     * @param serverName
     * @return
     * @throws InternalException
     * @throws MalformedURLException
     */
    private ICATClientInterface getIcatService(String icatUrl) throws InternalException, MalformedURLException {
        String url = "facilities02.esc.rl.ac.uk";

        ICATClientInterface service = ICATClientFactory.getInstance().createICATClient(Constants.ICAT_VERSION, icatUrl);

        if (service == null) {
            throw new InternalException("unable to retrieve service with url " + url);
        }

        return service;
    }



}
