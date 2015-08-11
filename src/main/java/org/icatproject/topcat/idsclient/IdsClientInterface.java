package org.icatproject.topcat.idsclient;

import java.net.URL;

import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient.Flag;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.topcat.exceptions.TopcatException;


public interface IdsClientInterface {
    public String prepareData(String sessionId, DataSelection dataSelection, Flag flags) throws TopcatException;
    public boolean isPrepared(String preparedId) throws TopcatException;
    public URL getDataUrl(String preparedId, String outname) throws TopcatException;
    public Status getStatus(String sessionId, DataSelection dataSelection) throws TopcatException;
    public long getSize(String sessionId, DataSelection dataSelection) throws TopcatException;
    public void ping() throws TopcatException;
    public boolean isTwoLevel() throws TopcatException;
    public String getApiVersion() throws TopcatException;
}
