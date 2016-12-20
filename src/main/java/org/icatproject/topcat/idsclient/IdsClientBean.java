package org.icatproject.topcat.idsclient;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ejb.Stateless;

import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient.Flag;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.TopcatException;

@Stateless
public class IdsClientBean {
    /**
     * Returns an icat service for a given server name
     *
     * @param serverName
     * @return
     * @throws InternalException
     * @throws MalformedURLException
     */
    private IdsClientInterface getIdsService(String idsUrl) throws MalformedURLException, InternalException {
        IdsClientInterface service = IdsClientFactory.getInstance().createIdsClient(idsUrl);

        if (service == null) {
            throw new InternalException("unable to retrieve service with url " + idsUrl);
        }

        return service;
    }


    public String prepareData(String idsUrl, String sessionId, DataSelection dataSelection, Flag flags) throws TopcatException, MalformedURLException {
        IdsClientInterface service = getIdsService(idsUrl);

        return service.prepareData(sessionId, dataSelection, flags);
    }

    public boolean isPrepared(String idsUrl, String preparedId) throws TopcatException, MalformedURLException {
        IdsClientInterface service = getIdsService(idsUrl);

        return service.isPrepared(preparedId);
    }

    public Status getStatus(String idsUrl, String sessionId, DataSelection dataSelection) throws TopcatException, MalformedURLException {
        IdsClientInterface service = getIdsService(idsUrl);

        return service.getStatus(sessionId, dataSelection);
    }

    public long getSize(String idsUrl, String sessionId, DataSelection dataSelection) throws TopcatException, MalformedURLException {
        IdsClientInterface service = getIdsService(idsUrl);

        return service.getSize(sessionId, dataSelection);
    }

    public boolean isTwoLevel(String idsUrl) throws TopcatException, MalformedURLException {
        IdsClientInterface service = getIdsService(idsUrl);

        return service.isTwoLevel();
    }

}
