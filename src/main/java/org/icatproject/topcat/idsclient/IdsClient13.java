package org.icatproject.topcat.idsclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.net.MalformedURLException;
import java.net.URL;

import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient;
import org.icatproject.ids.client.IdsException;
import org.icatproject.ids.client.IdsClient.Flag;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.ids.client.InsufficientPrivilegesException;
import org.icatproject.ids.client.NotImplementedException;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.BadRequestException;
import org.icatproject.topcat.exceptions.ForbiddenException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.NotFoundException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ids client
 */
public class IdsClient13 implements IdsClientInterface {
    private IdsClient service;

    private static final Logger logger = LoggerFactory.getLogger(IdsClient.class);

    public IdsClient13(String serverURL) throws MalformedURLException {
        logger.info("IdsInterfacev: serverURL (" + serverURL + ")");
        URL url = new URL(serverURL);

        service = new IdsClient(url);
    }

    @Override
    public String prepareData(String sessionId, DataSelection dataSelection, Flag flags) throws TopcatException {
        String result = null;

        try {
            result =  service.prepareData(sessionId, dataSelection, flags);
        } catch (org.icatproject.ids.client.NotImplementedException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        } catch (org.icatproject.ids.client.BadRequestException e) {
            throw new org.icatproject.topcat.exceptions.BadRequestException(e.getMessage());
        } catch (InsufficientPrivilegesException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        } catch (org.icatproject.ids.client.NotFoundException e) {
            throw new org.icatproject.topcat.exceptions.NotFoundException(e.getMessage());
        } catch (org.icatproject.ids.client.InternalException e) {
            throw new org.icatproject.topcat.exceptions.InternalException(e.getMessage());
        }

        return result;
    }

    @Override
    public boolean isPrepared(String preparedId) throws TopcatException {
        boolean result = false;


        try {
            result =  service.isPrepared(preparedId);
        } catch (org.icatproject.ids.client.BadRequestException e) {
            throw new org.icatproject.topcat.exceptions.BadRequestException(e.getMessage());
        } catch (org.icatproject.ids.client.NotFoundException e) {
            throw new org.icatproject.topcat.exceptions.NotFoundException(e.getMessage());
        } catch (org.icatproject.ids.client.InternalException e) {
            throw new org.icatproject.topcat.exceptions.InternalException(e.getMessage());
        } catch (NotImplementedException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        }

        return result;
    }

    @Override
    public URL getDataUrl(String preparedId, String outname) throws TopcatException {
        URL url = null;
        url = service.getDataUrl(preparedId, outname);
        return url;
    }

    @Override
    public Status getStatus(String sessionId, DataSelection dataSelection) throws TopcatException {
        Status status = null;
        try {
            for(DataSelection currentDataSelection : chunkDataSelection(dataSelection)){
                Status currentStatus = service.getStatus(sessionId, currentDataSelection);
                if(currentStatus == Status.ARCHIVED){
                    status = Status.ARCHIVED;
                    break;
                } else if(currentStatus == Status.RESTORING){
                    status = Status.RESTORING;
                } else if(status != Status.RESTORING){
                    status = Status.ONLINE;
                }
            }
        } catch (org.icatproject.ids.client.BadRequestException e) {
            throw new org.icatproject.topcat.exceptions.BadRequestException(e.getMessage());
        } catch (org.icatproject.ids.client.NotFoundException e) {
            throw new org.icatproject.topcat.exceptions.NotFoundException(e.getMessage());
        } catch (InsufficientPrivilegesException e) {
            throw new org.icatproject.topcat.exceptions.ForbiddenException(e.getMessage());
        } catch (org.icatproject.ids.client.InternalException e) {
            throw new org.icatproject.topcat.exceptions.InternalException(e.getMessage());
        } catch (NotImplementedException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        }

        return status;
    }

    @Override
    public long getSize(String sessionId, DataSelection dataSelection) throws TopcatException {
        long size = -1L;

        try {
            service.getSize(sessionId, dataSelection);
        } catch (org.icatproject.ids.client.BadRequestException e) {
            throw new org.icatproject.topcat.exceptions.BadRequestException(e.getMessage());
        } catch (org.icatproject.ids.client.NotFoundException e) {
            throw new org.icatproject.topcat.exceptions.NotFoundException(e.getMessage());
        } catch (InsufficientPrivilegesException e) {
            throw new org.icatproject.topcat.exceptions.ForbiddenException(e.getMessage());
        } catch (org.icatproject.ids.client.InternalException e) {
            throw new org.icatproject.topcat.exceptions.InternalException(e.getMessage());
        } catch (NotImplementedException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        }


        return size;
    }

    @Override
    public void ping() throws TopcatException {

        try {
            service.ping();
        } catch (org.icatproject.ids.client.InternalException e) {
            throw new org.icatproject.topcat.exceptions.InternalException(e.getMessage());
        } catch (NotImplementedException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        }
    }


    @Override
    public boolean isTwoLevel() throws TopcatException {
        try {
            return service.isTwoLevel();
        } catch (org.icatproject.ids.client.InternalException e) {
            throw new org.icatproject.topcat.exceptions.InternalException(e.getMessage());
        } catch (NotImplementedException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        }
    }


    @Override
    public String getApiVersion() throws TopcatException {
        String apiVersion = null;

        try {
            apiVersion = service.getApiVersion();
        } catch (org.icatproject.ids.client.InternalException e) {
            throw new org.icatproject.topcat.exceptions.InternalException(e.getMessage());
        } catch (NotImplementedException e) {
            throw new org.icatproject.topcat.exceptions.NotImplementedException(e.getMessage());
        }

        return apiVersion;

    }



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
    @SuppressWarnings("unused")
    private void throwNewICATException(IdsException e) throws TopcatException {
        logger.debug("IdsException class: " + e.getClass());
    }

    private List<DataSelection> chunkDataSelection(DataSelection dataSelection){
        List<DataSelection> out = new ArrayList<DataSelection>();
        Map<String, String> parameters = dataSelection.getParameters();
        List<Long> investigationIds = new ArrayList<Long>();
        List<Long> datasetIds = new ArrayList<Long>();
        List<Long> datafileIds = new ArrayList<Long>();

        if(parameters.get("investigationIds") != null){
            for(String id : parameters.get("investigationIds").split(",")){
                investigationIds.add(Long.valueOf(id));
            }
        }
        if(parameters.get("datasetIds") != null){
            for(String id : parameters.get("datasetIds").split(",")){
                datasetIds.add(Long.valueOf(id));
            }
        }
        if(parameters.get("datafileIds") != null){
            for(String id : parameters.get("datafileIds").split(",")){
                datafileIds.add(Long.valueOf(id));
            }
        }

        while(!investigationIds.isEmpty() || !datasetIds.isEmpty() || !datafileIds.isEmpty()){
            DataSelection currentDataSelection = new DataSelection();

            while(!investigationIds.isEmpty()){
                if(dataSelectionSize(currentDataSelection) >= 900){
                    break;
                }
                currentDataSelection.addInvestigation(investigationIds.remove(0));
            }

            while(!datasetIds.isEmpty()){
                if(dataSelectionSize(currentDataSelection) >= 900){
                    break;
                }
                currentDataSelection.addDataset(datasetIds.remove(0));
            }

            while(!datafileIds.isEmpty()){
                if(dataSelectionSize(currentDataSelection) >= 900){
                    break;
                }
                currentDataSelection.addDatafile(datafileIds.remove(0));
            }

            out.add(currentDataSelection);
        }

        return out;
    }

    private int dataSelectionSize(DataSelection dataSelection){
        Map<String, String> parameters = dataSelection.getParameters();
        int out = 0;
        if(parameters.get("investigationIds") != null){
            out += parameters.get("investigationIds").length();
        }
        if(parameters.get("datasetIds") != null){
            out += parameters.get("datasetIds").length();
        }
        if(parameters.get("datafileIds") != null){
            out += parameters.get("datafileIds").length();
        }
        return out; 
    }

}
