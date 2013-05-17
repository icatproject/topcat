/**
 * 
 * Copyright (c) 2009-2013
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.gwt.server;

/**
 * Imports
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.icatproject.idsclient.Client;
import org.icatproject.idsclient.Status;
import org.icatproject.idsclient.exceptions.BadRequestException;
import org.icatproject.idsclient.exceptions.ForbiddenException;
import org.icatproject.idsclient.exceptions.IDSException;
import org.icatproject.idsclient.exceptions.NotFoundException;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileFormat;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.NotSupportedException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserDownload;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UtilityLocal;
import uk.ac.stfc.topcat.ejb.utils.Configuration;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.model.AuthenticationModel;
import uk.ac.stfc.topcat.gwt.client.model.DatafileFormatModel;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ICATNodeType;
import uk.ac.stfc.topcat.gwt.client.model.ParameterModel;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This is servlet implementation of Utility methods such as getting information
 * about instruments etc.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@SuppressWarnings("serial")
public class UtilityServiceImpl extends RemoteServiceServlet implements UtilityService {

    private UtilityLocal utilityManager = null;
    private UserManagementBeanLocal userManager = null;
    private Map<String, String> downloadPlugins = new HashMap<String, String>();
    private static String RESTFUL_DOWNLOAD_SERVICE = "restfulDownload";

    /**
     * Servlet Init method.
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);

        try {
            // create initial context
            Context ctx = new InitialContext();
            utilityManager = (UtilityLocal) ctx
                    .lookup("java:global/TopCAT/UtilityBean!uk.ac.stfc.topcat.ejb.session.UtilityLocal");
            userManager = (UserManagementBeanLocal) ctx
                    .lookup("java:global/TopCAT/UserManagementBean!uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal");
        } catch (NamingException ex) {
            ex.printStackTrace();
        }

        ArrayList<TFacility> facilities = utilityManager.getFacilities();
        for (TFacility facility : facilities) {
            downloadPlugins.put(facility.getName(), facility.getDownloadPluginName());
        }
    }

    /**
     * This method returns all facility(iCAT instances) objects.
     */
    @Override
    public ArrayList<TFacility> getFacilities() {
        return utilityManager.getFacilities();
    }

    /**
     * This method returns list of all instrument names in a given facility.
     * 
     * @param facilityName
     *            iCAT instance name
     * @throws TopcatException
     */
    @Override
    public ArrayList<String> getInstrumentNames(String facilityName) throws TopcatException {
        return utilityManager.getInstrumentNames(getSessionId(), facilityName);
    }

    /**
     * This method returns list of investigation types in a given facility.
     * 
     * @param facilityName
     *            iCAT instance name
     * @throws TopcatException
     */
    @Override
    public ArrayList<String> getInvestigationTypes(String facilityName) throws TopcatException {
        return utilityManager.getInvestigationTypes(getSessionId(), facilityName);
    }

    /**
     * This method constructs a ICATNode child for the input node.
     * 
     * @param node
     * @param isMyData
     * @return list of ICATNode's children
     * @throws SessionException
     * @throws TopcatException
     */
    private ArrayList<ICATNode> getICATNodeChildren(ICATNode node, boolean isMyData) throws SessionException,
            TopcatException {
        // Check the node type
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        // if the node is null then its root, load the facilities
        if (node == null || node.getNodeType() == ICATNodeType.UNKNOWN) {
            ArrayList<String> facilityNames = utilityManager.getFacilityNames();
            for (String facility : facilityNames) {
                ICATNode tnode = new ICATNode();
                tnode.setNode(ICATNodeType.FACILITY, null, facility);
                result.add(tnode);
            }
        } else if (node.getNodeType() == ICATNodeType.FACILITY) {
            result.addAll(createInstrumentsNodesInFacility(node, isMyData));
        } else if (node.getNodeType() == ICATNodeType.INSTRUMENT) {
            result.addAll(createCyclesInInstrument(node, isMyData));
        } else if (node.getNodeType() == ICATNodeType.CYCLE) {
            result.addAll(createInvestigationNodesInCycle(node, isMyData));
        } else if (node.getNodeType() == ICATNodeType.INVESTIGATION) {
            ArrayList<ICATNode> tresult = createDatasetNodesInInvestigation(node);
            // If only one dataset then directly show datafiles in tree
            if (tresult.size() == 1) {
                ICATNode tnode = new ICATNode();
                tnode.setNode(ICATNodeType.DATASET, tresult.get(0).getDatasetId(), tresult.get(0).getDatasetName());
                tnode.setFacility(node.getFacility());
                return getICATNodeChildren(tnode, isMyData);
            } else {
                result.addAll(tresult);
            }
        } else if (node.getNodeType() == ICATNodeType.DATASET) {
            result.addAll(createDatafileNodesInDataset(node));
        }
        if (node != null && node.getNodeType() != ICATNodeType.INSTRUMENT)
            Collections.sort(result);
        return result;
    }

    /**
     * This method returns all the instruments in facility.
     * 
     * @param node
     * @return
     * @throws TopcatException
     */
    private ArrayList<ICATNode> createInstrumentsNodesInFacility(ICATNode node, boolean isMyData)
            throws TopcatException {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        ArrayList<String> instrumentList = utilityManager.getInstrumentNames(getSessionId(), node.getFacility());
        if (instrumentList.size() > 0) {
            for (String instrument : instrumentList) {
                ICATNode tnode = new ICATNode();
                tnode.setNode(ICATNodeType.INSTRUMENT, instrument, instrument);
                tnode.setFacility(node.getFacility());
                result.add(tnode);
            }
        } else {
            result.addAll(createInvestigationNodesInFacility(node, isMyData));
        }
        return result;
    }

    /**
     * This method gets all the instruments in a given cycle.
     * 
     * @param node
     * @return
     * @throws TopcatException
     */
    private ArrayList<ICATNode> createCyclesInInstrument(ICATNode node, boolean isMyData) throws TopcatException {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        try {
            List<TFacilityCycle> facilityCycleList = utilityManager.getFacilityCyclesWithInstrument(getSessionId(),
                    node.getFacility(), node.getInstrumentName());
            if (facilityCycleList.size() > 0) {
                for (TFacilityCycle cycle : facilityCycleList) {
                    ICATNode tnode = new ICATNode();
                    tnode.setNode(ICATNodeType.CYCLE, "", cycle.getName());
                    tnode.setStartDate(cycle.getStartDate());
                    tnode.setEndDate(cycle.getFinishDate());
                    tnode.setFacility(node.getFacility());
                    tnode.setInstrumentName(node.getInstrumentName());
                    tnode.setDescription(cycle.getDescription());
                    result.add(tnode);
                }
            } else {
                // No cycles found try investigations directly from instruments
                result.addAll(createInvestigationNodesInInstrument(node, isMyData));
            }
        } catch (NotSupportedException ex) {
            // Cycle method is not available try investigations directly
            // from instruments
            result.addAll(createInvestigationNodesInInstrument(node, isMyData));
        }
        return result;
    }

    private ArrayList<ICATNode> createInvestigationNodesInInstrument(ICATNode node, boolean isMyData)
            throws TopcatException {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        ArrayList<TInvestigation> invList;
        if (isMyData)
            invList = utilityManager.getMyInvestigationsInServerAndInstrument(getSessionId(), node.getFacility(),
                    node.getInstrumentName());
        else
            invList = utilityManager.getAllInvestigationsInServerAndInstrument(getSessionId(), node.getFacility(),
                    node.getInstrumentName());
        for (TInvestigation inv : invList) {
            ICATNode tnode = new ICATNode();
            tnode.setNode(ICATNodeType.INVESTIGATION, inv.getInvestigationId(),
                    inv.getTitle() + "(Inv. Id:" + inv.getInvestigationName() + " & Visit Id:" + inv.getVisitId() + ")");
            tnode.setFacility(node.getFacility());
            tnode.setTitle(inv.getTitle());
            result.add(tnode);
        }
        return result;
    }

    private ArrayList<ICATNode> createInvestigationNodesInFacility(ICATNode node, boolean isMyData)
            throws TopcatException {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        List<TInvestigation> invList;
        if (isMyData)
            invList = utilityManager.getMyInvestigationsInServer(getSessionId(), node.getFacility());
        else
            invList = utilityManager.getAllInvestigationsInServer(getSessionId(), node.getFacility());
        for (TInvestigation inv : invList) {
            ICATNode tnode = new ICATNode();
            tnode.setNode(ICATNodeType.INVESTIGATION, inv.getInvestigationId(),
                    inv.getTitle() + "(Inv. Id:" + inv.getInvestigationName() + " & Visit Id:" + inv.getVisitId() + ")");
            tnode.setFacility(node.getFacility());
            tnode.setTitle(inv.getTitle());
            result.add(tnode);
        }
        return result;
    }

    /**
     * This method gets all the Investigations in the cycle and returns them.
     * 
     * @param node
     * @param isMyData
     * @return
     * @throws TopcatException
     */
    private ArrayList<ICATNode> createInvestigationNodesInCycle(ICATNode node, boolean isMyData) throws TopcatException {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        ArrayList<TInvestigation> invList;
        TFacilityCycle cycle = new TFacilityCycle();
        cycle.setDescription(node.getDescription());
        cycle.setStartDate(node.getStartDate());
        cycle.setFinishDate(node.getEndDate());
        if (isMyData)
            invList = utilityManager.getMyInvestigationsInServerInstrumentAndCycle(getSessionId(), node.getFacility(),
                    node.getInstrumentName(), cycle);
        else
            invList = utilityManager.getAllInvestigationsInServerInstrumentAndCycle(getSessionId(), node.getFacility(),
                    node.getInstrumentName(), cycle);
        for (TInvestigation inv : invList) {
            ICATNode tnode = new ICATNode();
            tnode.setNode(ICATNodeType.INVESTIGATION, inv.getInvestigationId(),
                    inv.getTitle() + "(Id:" + inv.getInvestigationName() + ")");
            tnode.setFacility(node.getFacility());
            tnode.setTitle(inv.getTitle());
            result.add(tnode);
        }
        return result;
    }

    /**
     * This method gets the datasets in given input investigation and returns
     * them as an array list.
     * 
     * @param node
     * @return
     * @throws TopcatException
     */
    private ArrayList<ICATNode> createDatasetNodesInInvestigation(ICATNode node) throws TopcatException {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        ArrayList<TDataset> invList;
        invList = utilityManager.getDatasetsInServer(getSessionId(), node.getFacility(), node.getInvestigationId());
        for (TDataset inv : invList) {
            ICATNode tnode = new ICATNode();
            tnode.setNode(ICATNodeType.DATASET, inv.getId(), inv.getName());
            tnode.setFacility(node.getFacility());
            result.add(tnode);
        }
        return result;
    }

    /**
     * This method collects all the datafiles in the given dataset and returns
     * them as an array list.
     * 
     * @param node
     * @return
     * @throws TopcatException
     */
    private ArrayList<ICATNode> createDatafileNodesInDataset(ICATNode node) throws TopcatException {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        ArrayList<TDatafile> invList = utilityManager.getDatafilesInServer(getSessionId(), node.getFacility(),
                node.getDatasetId());
        for (TDatafile inv : invList) {
            ICATNode tnode = new ICATNode();
            tnode.setNode(ICATNodeType.DATAFILE, inv.getId(), inv.getName());
            tnode.setFacility(node.getFacility());
            result.add(tnode);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getDatasetParameters(java
     * .lang.String, java.lang.String)
     */
    @Override
    public ArrayList<ParameterModel> getDatasetParameters(String facilityName, String datasetId) throws TopcatException {
        return getDatasetParameters(getSessionId(), facilityName, datasetId);
    }

    /**
     * This method returns creates a parameter model which has list of parameter
     * names and corresponding values for a given dataset id on a server.
     * 
     * @param sessionId
     *            user session id
     * @param facilityName
     *            facility name
     * @param datasetId
     *            input dataset id.
     * @return list of parameters corresponding to dataset
     * @throws TopcatException
     */
    private ArrayList<ParameterModel> getDatasetParameters(String sessionId, String facilityName, String datasetId)
            throws TopcatException {
        ArrayList<ParameterModel> result = new ArrayList<ParameterModel>();
        ArrayList<TDatasetParameter> ds = utilityManager.getDatasetInfoInServer(sessionId, facilityName, datasetId);
        if (ds == null)
            return result;
        for (TDatasetParameter dsParam : ds) {
            result.add(new ParameterModel(dsParam.getName(), dsParam.getUnits(), dsParam.getValue()));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getDatafileParameters(java
     * .lang.String, java.lang.String)
     */
    @Override
    public ArrayList<ParameterModel> getDatafileParameters(String facilityName, String datafileId)
            throws TopcatException {
        return getDatafileParameters(getSessionId(), facilityName, datafileId);
    }

    /**
     * This method returns creates a parameter model which has list of parameter
     * names and corresponding values for a given datafile id on a server.
     * 
     * @param sessionId
     *            user session id
     * @param facilityName
     *            facility name
     * @param datafileId
     *            input datafile id.
     * @return list of parameters corresponding to datafile
     * @throws TopcatException
     */
    private ArrayList<ParameterModel> getDatafileParameters(String sessionId, String facilityName, String datafileId)
            throws TopcatException {
        ArrayList<ParameterModel> result = new ArrayList<ParameterModel>();
        ArrayList<TDatafileParameter> df = utilityManager.getDatafileInfoInServer(sessionId, facilityName, datafileId);
        if (df == null)
            return result;
        for (TDatafileParameter dfParam : df) {
            result.add(new ParameterModel(dfParam.getName(), dfParam.getUnits(), dfParam.getValue()));
        }
        return result;
    }

    /**
     * This method returns the list of datasets in a given investigation.
     * 
     * @param facilityName
     *            iCAT instance name
     * @param investigationId
     *            input id of investigation
     * @throws TopcatException
     */
    @Override
    public ArrayList<DatasetModel> getDatasetsInInvestigations(String facilityName, String investigationId)
            throws TopcatException {
        ArrayList<DatasetModel> result = new ArrayList<DatasetModel>();
        ArrayList<TDataset> dsList;
        dsList = utilityManager.getDatasetsInServer(getSessionId(), facilityName, investigationId);
        if (dsList == null)
            return result;
        for (TDataset ds : dsList) {
            result.add(new DatasetModel(facilityName, ds.getId(), ds.getName(), ds.getStatus(), ds.getType(), ds
                    .getDescription()));
        }
        return result;
    }

    /**
     * This method returns the datafiles information corresponding to the input
     * set of datasets.
     * 
     * @param datasets
     *            input list of datasets
     * @throws TopcatException
     */
    @Override
    public ArrayList<DatafileModel> getDatafilesInDatasets(ArrayList<DatasetModel> datasets) throws TopcatException {
        ArrayList<DatafileModel> result = new ArrayList<DatafileModel>();
        String sessionId = getSessionId();
        String dsName;
        for (DatasetModel dataset : datasets) {
            if (dataset.getName() != null) {
                dsName = dataset.getName();
            } else {
                dsName = utilityManager.getDatasetName(sessionId, dataset.getFacilityName(), dataset.getId());
            }
            ArrayList<TDatafile> dfList = utilityManager.getDatafilesInServer(sessionId, dataset.getFacilityName(),
                    dataset.getId());
            if (dfList == null) {
                continue;
            }
            for (TDatafile df : dfList) {
                if (df.getCreateTime() != null)
                    result.add(new DatafileModel(dataset.getFacilityName(), dataset.getId(), dsName, df.getId(), df
                            .getName(), df.getDescription(), df.getSize().toString(), df.getDoi(), df.getLocation(), df
                            .getFormatId(), df.getFormat(), df.getFormatDescription(), df.getFormatVersion(), df
                            .getFormatType(), df.getCreateTime(), df.getModTime()));
                else
                    result.add(new DatafileModel(dataset.getFacilityName(), dataset.getId(), dsName, df.getId(), df
                            .getName(), df.getDescription(), df.getSize().toString(), df.getDoi(), df.getLocation(), df
                            .getFormatId(), df.getFormat(), df.getFormatDescription(), df.getFormatVersion(), df
                            .getFormatType(), null, null));

            }
        }
        return result;
    }

    /**
     * This method returns session id from the session information.
     * 
     * @return user session id
     * @throws SessionException
     */
    private String getSessionId() throws SessionException {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String sessionId = null;
        if (session.getAttribute("SESSION_ID") == null) { // First time login
            try {
                sessionId = userManager.login();
                session.setAttribute("SESSION_ID", sessionId);
            } catch (AuthenticationException e) {
                throw new SessionException("Invalid topcat session id");
            }
        } else {
            sessionId = (String) session.getAttribute("SESSION_ID");
        }
        return sessionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getDatafilesDownloadURL(java
     * .lang.String, java.util.ArrayList, java.lang.String)
     */
    @Override
    public DownloadModel getDatafilesDownloadURL(String facilityName, ArrayList<Long> datafileIds, String downloadName)
            throws TopcatException {
        String status;
        Date submitTime = new Date(System.currentTimeMillis());
        Date expiryTime = null;
        String url = utilityManager.getDatafilesDownloadURL(getSessionId(), facilityName, datafileIds);
        String downloadPlugin = downloadPlugins.get(facilityName);
        if (downloadPlugin != null && downloadPlugin.equalsIgnoreCase(RESTFUL_DOWNLOAD_SERVICE)) {
            status = Constants.STATUS_IN_PROGRESS;
            expiryTime = getExpiryTime(url);
            utilityManager.addMyDownload(getSessionId(), facilityName, submitTime, downloadName, status, expiryTime,
                    url, null);
        } else {
            status = Constants.STATUS_AVAILABLE;
        }
        return new DownloadModel(facilityName, submitTime, downloadName, status, expiryTime, url);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getDatasetDownloadURL(java
     * .lang.String, java.lang.Long, java.lang.String)
     */
    @Override
    public DownloadModel getDatasetDownloadURL(String facilityName, Long datasetId, String downloadName)
            throws TopcatException {
        String status;
        Date submitTime = new Date(System.currentTimeMillis());
        Date expiryTime = null;
        String url = utilityManager.getDatasetDownloadURL(getSessionId(), facilityName, datasetId);
        if (downloadPlugins.get(facilityName).equalsIgnoreCase(RESTFUL_DOWNLOAD_SERVICE)) {
            status = Constants.STATUS_IN_PROGRESS;
            expiryTime = getExpiryTime(url);
            utilityManager.addMyDownload(getSessionId(), facilityName, submitTime, downloadName, status, expiryTime,
                    url, null);
        } else {
            status = Constants.STATUS_AVAILABLE;
        }
        return new DownloadModel(facilityName, submitTime, downloadName, status, expiryTime, url);
    }

    private Date getExpiryTime(String url) {
        Date expiryTime = null;
        try {
            URL statusUrl = new URL(url + "/ExpiryTime");
            BufferedReader in = new BufferedReader(new InputStreamReader(statusUrl.openStream()));
            SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
            expiryTime = df.parse(in.readLine());
            in.close();
        } catch (IOException e) {
            // TODO
            System.out.println("ERROR in getExpiryTime " + e.getMessage());
        } catch (ParseException e) {
            // TODO
            System.out.println("ERROR in getExpiryTime " + e.getMessage());
        }
        return expiryTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getAllICATNodeDatafiles(uk
     * .ac.stfc.topcat.gwt.client.model.ICATNode)
     */
    @Override
    public HashMap<String, ArrayList<ICATNode>> getAllICATNodeDatafiles(ICATNode node) throws SessionException,
            TopcatException {
        ArrayList<ICATNode> resultNodes;
        try {
            resultNodes = getICATNodeChildren(node, false);
        } catch (SessionException e) {
            throw new SessionException(e.getMessage());
        }
        if (resultNodes.size() != 0 && resultNodes.get(0).getNodeType() != ICATNodeType.DATAFILE) {
            HashMap<String, ArrayList<ICATNode>> result = new HashMap<String, ArrayList<ICATNode>>();
            result.put("", resultNodes);
            return result;
        } else {
            return createDatafilesHierarchy(resultNodes);
        }
    }

    /**
     * This method creates a hierarchy for the datafiles ie RAW datafiles are
     * parents and log,.txt,s0,s1.. children of RAW files if they start with the
     * datafile name
     * 
     * @param datafileList
     * @return
     */
    private HashMap<String, ArrayList<ICATNode>> createDatafilesHierarchy(ArrayList<ICATNode> datafileList) {
        HashMap<String, ArrayList<ICATNode>> result = new HashMap<String, ArrayList<ICATNode>>();
        ArrayList<ICATNode> logFiles = new ArrayList<ICATNode>();
        ArrayList<ICATNode> rawFiles = new ArrayList<ICATNode>();
        ArrayList<ICATNode> otherFiles = new ArrayList<ICATNode>();
        // Separate all the files according to their type
        for (ICATNode datafile : datafileList) {
            String filefullname = datafile.getDatafileName();
            int dot = filefullname.lastIndexOf(".");
            String filetype = filefullname.substring(dot + 1);
            if (isRAWFileType(filetype)) {
                rawFiles.add(datafile);
            } else if (isLogFileType(filetype)) {
                logFiles.add(datafile);
            } else {
                otherFiles.add(datafile);
            }
        }
        // put the log files into raw files
        for (ICATNode logfile : logFiles) {
            String filefullname = logfile.getDatafileName();
            int dot = filefullname.lastIndexOf(".");
            String filename = filefullname.substring(0, dot);
            // check each raw file whether the log file name starts with raw
            // filename
            boolean rawFound = false;
            for (ICATNode rawfile : rawFiles) {
                String rawfilefullname = rawfile.getDatafileName();
                int rawdot = rawfilefullname.lastIndexOf(".");
                String rawfilename = rawfilefullname.substring(0, rawdot);
                if (filename.startsWith(rawfilename)) {// found the raw file
                    rawFound = true;
                    ArrayList<ICATNode> logChildList = result.get(rawfile.getFacility() + rawfile.getDatafileId());
                    if (logChildList == null) {
                        logChildList = new ArrayList<ICATNode>();
                        result.put(rawfile.getFacility() + rawfile.getDatafileId(), logChildList);
                    }
                    logChildList.add(logfile);
                    break;
                }
            }
            if (!rawFound)
                otherFiles.add(logfile);
        }
        // Add raw files and other files to null as first level list
        rawFiles.addAll(otherFiles);
        result.put("", rawFiles);
        return result;
    }

    private boolean isRAWFileType(String filetype) {
        if (filetype.compareToIgnoreCase("raw") == 0)
            return true;
        return false;
    }

    private boolean isLogFileType(String filetype) {
        if (filetype.compareToIgnoreCase("log") == 0 || filetype.compareToIgnoreCase("txt") == 0) {
            return true;
        }
        // check for s0,s1...
        if (filetype.matches("[sS]\\d+")) {
            return true;
        }
        return false;
    }

    /**
     * Get a list of investigations for the given facility that belong to the
     * user.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @return a list of <code>TInvestigation</code> containing investigations
     * @throws TopcatException
     */
    @Override
    public List<TInvestigation> getMyInvestigationsInServer(String facilityName) throws TopcatException {
        return utilityManager.getMyInvestigationsInServer(getSessionId(), facilityName);
    }

    @Override
    public TInvestigation getInvestigationDetails(String facilityName, String investigationId) throws TopcatException {
        return utilityManager.getInvestigationDetails(getSessionId(), facilityName, investigationId);
    }

    @Override
    public ArrayList<String> getParameterNames(String facilityName) throws TopcatException {
        return utilityManager.getParameterNames(getSessionId(), facilityName);
    }

    @Override
    public ArrayList<String> getParameterUnits(String facilityName, String name) throws TopcatException {
        return utilityManager.getParameterUnits(getSessionId(), facilityName, name);
    }

    @Override
    public ArrayList<String> getParameterTypes(String facilityName, String name, String units) throws TopcatException {
        return utilityManager.getParameterTypes(getSessionId(), facilityName, name, units);
    }

    @Override
    public Map<String, String> getTopcatProperties() {
        return Configuration.INSTANCE.getTopcatProperties();
    }

    @Override
    public ArrayList<DownloadModel> getMyDownloadList(Set<String> facilities) throws TopcatException {
        ArrayList<DownloadModel> result = new ArrayList<DownloadModel>();
        for (String facilityName : facilities) {
            List<TopcatUserDownload> dlList = utilityManager.getMyDownloadList(getSessionId(), facilityName);
            if (dlList == null)
                continue;
            for (TopcatUserDownload dl : dlList) {
                result.add(new DownloadModel(facilityName, dl.getUrl(), dl.getPreparedId(), dl.getName(), dl
                        .getSubmitTime(), dl.getStatus()));
            }
        }
        return result;
    }

    @Override
    public List<DownloadModel> getDownloadStatus(Set<DownloadModel> downloadModels) throws TopcatException {
        List<DownloadModel> finalStateReached = new ArrayList<DownloadModel>();
        for (DownloadModel downloadModel : downloadModels) {
            DownloadModel updatedDownloadModel = null;
            if (downloadModel.getPreparedId() == null || downloadModel.getPreparedId().isEmpty()) {
                updatedDownloadModel = updateDownloadStatusClassic(downloadModel);
            } else {
                updatedDownloadModel = updateDownloadStatus(downloadModel);
            }
            if (updatedDownloadModel != null) {
                finalStateReached.add(updatedDownloadModel);
            }
        }
        return finalStateReached;
    }

    @Deprecated
    private DownloadModel updateDownloadStatusClassic(DownloadModel downloadModel) throws SessionException {
        BufferedReader in = null;
        try {
            // get the status from the download service
            String requestUrl = downloadModel.getUrl();
            URL statusUrl = new URL(requestUrl + "/Status");
            URLConnection conn = statusUrl.openConnection();
            try {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } catch (IOException e) {
                if (((HttpURLConnection) statusUrl.openConnection()).getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    // assume this one is finished, possibly expired
                    downloadModel.setStatus(Constants.STATUS_ERROR);
                    downloadModel.setUrl(requestUrl + "/Download?Filename=" + downloadModel.getDownloadName());
                    utilityManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(), requestUrl,
                            requestUrl + "/Download?Filename=" + downloadModel.getDownloadName(),
                            Constants.STATUS_ERROR);
                    return downloadModel;
                } else {
                    throw new IOException(e);
                }
            }
            String inputLine = in.readLine();
            System.out.println("updateDownloadStatusClassic " + inputLine);
            if (inputLine.equalsIgnoreCase("COMPLETED")) {
                // this one is finished
                downloadModel.setStatus(Constants.STATUS_AVAILABLE);
                downloadModel.setUrl(requestUrl + "/Download?Filename=" + downloadModel.getDownloadName());
                utilityManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(), requestUrl,
                        requestUrl + "/Download?Filename=" + downloadModel.getDownloadName(),
                        Constants.STATUS_AVAILABLE);
                return downloadModel;
            } else if (inputLine.equalsIgnoreCase("ERROR")) {
                // this one is finished
                downloadModel.setStatus(Constants.STATUS_ERROR);
                downloadModel.setUrl(requestUrl + "/Download?Filename=" + downloadModel.getDownloadName());
                utilityManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(), requestUrl,
                        requestUrl + "/Download?Filename=" + downloadModel.getDownloadName(), Constants.STATUS_ERROR);
                return downloadModel;
            }
            in.close();
        } catch (IOException e) {
            // download service unavailable
            // TODO
            System.out.println("ERROR in updateDownloadStatusClassic " + e.getMessage());
            try {
                in.close();
            } catch (IOException e1) {
            } catch (NullPointerException e1) {
            }
        }
        return null;
    }

    /**
     * Get the status of the download from the download service.
     * 
     * @param downloadModel
     *            the download to check
     * @return an updated DataModel is returned if the status has changed
     *         overwise null is returned
     * @throws TopcatException
     */
    private DownloadModel updateDownloadStatus(DownloadModel downloadModel) throws TopcatException {
        Client ids = null;
        try {
            ids = new Client(downloadModel.getUrl());
        } catch (BadRequestException e) {
            // TODO
            System.out.println("ERROR in updateDownloadStatus " + e.getMessage());
            throw new InternalException("Error bad url for the download service. " + e.getMessage());
        }
        Status status = null;
        String downloadUrl = "";
        try {
            status = ids.getStatus(downloadModel.getPreparedId());
            if (status == Status.ONLINE || status == Status.INCOMPLETE) {
                downloadUrl = ids.getDataUrl(downloadModel.getPreparedId(), downloadModel.getDownloadName());
            }
        } catch (NotFoundException e) {
            // assume this one is finished, possibly expired
            System.out.println("updateDownloadStatus: NotFoundException: " + e.getMessage());
            downloadModel.setStatus(Constants.STATUS_EXPIRED);
            utilityManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(),
                    downloadModel.getUrl(), downloadModel.getUrl(), Constants.STATUS_EXPIRED);
            return downloadModel;
        } catch (IDSException e) {
            // something has gone wrong
            // TODO
            System.out.println("ERROR in updateDownloadStatus " + e.getMessage());
            throw new InternalException("Error returned from the data service. " + e.getMessage());
        } catch (IOException e) {
            // cannot contact IDS, network issues?
            // TODO
            System.out.println("ERROR in updateDownloadStatus " + e.getMessage());
            throw new InternalException("Unable to contact the data service, " + downloadModel.getUrl());
        }
        if (status == Status.ONLINE) {
            // this one is finished
            utilityManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(),
                    downloadModel.getUrl(), downloadUrl, Constants.STATUS_AVAILABLE);
            downloadModel.setUrl(downloadUrl);
            downloadModel.setStatus(Constants.STATUS_AVAILABLE);
            return downloadModel;
        } else if (status == Status.INCOMPLETE) {
            // this one is finished
            // TODO what to do, give the user an incomplete set?
            utilityManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(),
                    downloadModel.getUrl(), downloadUrl, Constants.STATUS_ERROR);
            downloadModel.setUrl(downloadUrl);
            downloadModel.setStatus(Constants.STATUS_ERROR);
            System.out.println("updateDownloadStatus: IMCOMPLETE");
            return downloadModel;
        }
        return null;
    }

    @Override
    public List<AuthenticationModel> getAuthenticationDetails(String facilityName) {
        List<TAuthentication> authentications = utilityManager.getAuthenticationDetails(facilityName);
        List<AuthenticationModel> models = new ArrayList<AuthenticationModel>();
        for (TAuthentication authentication : authentications) {
            models.add(new AuthenticationModel(authentication));
        }
        return models;
    }

    @Override
    public List<String> getDatasetTypes(String facilityName) throws TopcatException {
        return utilityManager.getDatasetTypes(getSessionId(), facilityName);
    }

    @Override
    public List<DatafileFormatModel> getDatafileFormats(String facilityName) throws TopcatException {
        List<DatafileFormatModel> models = new ArrayList<DatafileFormatModel>();
        List<TDatafileFormat> result = utilityManager.getDatafileFormats(getSessionId(), facilityName);
        for (TDatafileFormat df : result) {
            models.add(new DatafileFormatModel(facilityName, df.getFormatId(), df.getFormat(), df
                    .getFormatDescription(), df.getFormatVersion(), df.getFormatType()));
        }
        return models;
    }

    @Override
    public DownloadModel prepareDataObjectsForDownload(String dataType, TFacility facility, List<Long> dataObjectList,
            String downloadName) throws TopcatException {
        String sessionId = userManager.getIcatSessionId(getSessionId(), facility.getName());
        Date submitTime = new Date(System.currentTimeMillis());
        Client ids;
        try {
            ids = new Client(facility.getDownloadServiceUrl());
        } catch (BadRequestException e) {
            // TODO
            System.out.println("ERROR in updateDownloadStatus " + e.getMessage());
            throw new InternalException("Error bad url for the download service. " + e.getMessage());
        }
        String preparedId = "";
        try {
            if (dataType.equalsIgnoreCase(Constants.DATA_FILE)) {
                preparedId = ids.prepareDatafiles(sessionId, dataObjectList, false, null);
            } else if (dataType.equalsIgnoreCase(Constants.DATA_SET)) {
                preparedId = ids.prepareDatasets(sessionId, dataObjectList, false, null);
            }
        } catch (ForbiddenException e) {
            throw new SessionException();
        } catch (IDSException e) {
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (IOException e) {
            throw new InternalException("Error trying to contact the download service. " + e.getMessage());
        }
        DownloadModel dm = new DownloadModel(facility.getName(), facility.getDownloadServiceUrl(), preparedId,
                downloadName, submitTime, Constants.STATUS_IN_PROGRESS);
        utilityManager.addMyDownload(getSessionId(), facility.getName(), submitTime, downloadName,
                Constants.STATUS_IN_PROGRESS, null, facility.getDownloadServiceUrl(), preparedId);
        return dm;
    }

}
