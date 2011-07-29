/**
 * 
 * Copyright (c) 2009-2010
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.exception.ICATMethodNotFoundException;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UtilityLocal;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
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
    }

    /**
     * This method returns list of facilities registered in TopCAT.
     */
    @Override
    public ArrayList<String> getFacilityNames() {
        return utilityManager.getFacilityNames();
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
     */
    @Override
    public ArrayList<String> getInstrumentNames(String facilityName) {
        return utilityManager.getInstrumentNames(getSessionId(), facilityName);
    }

    /**
     * This method returns list of investigation types in a given facility.
     * 
     * @param facilityName
     *            iCAT instance name
     */
    @Override
    public ArrayList<String> getInvestigationTypes(String facilityName) {
        return utilityManager.getInvestigationTypes(getSessionId(), facilityName);
    }

    /**
     * This method constructs a ICATNode child for the input node.
     * 
     * @param node
     * @param isMyData
     * @return list of ICATNode's children
     */
    private ArrayList<ICATNode> getICATNodeChildren(ICATNode node, boolean isMyData) {
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
            result.addAll(createInstrumentsNodesInFacility(node));
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
     */
    private ArrayList<ICATNode> createInstrumentsNodesInFacility(ICATNode node) {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        ArrayList<String> instrumentList = utilityManager.getInstrumentNames(getSessionId(), node.getFacility());
        for (String instrument : instrumentList) {
            ICATNode tnode = new ICATNode();
            tnode.setNode(ICATNodeType.INSTRUMENT, instrument, instrument);
            tnode.setFacility(node.getFacility());
            result.add(tnode);
        }
        return result;
    }

    /**
     * This method gets all the instruments in a given cycle.
     * 
     * @param node
     * @return
     */
    private ArrayList<ICATNode> createCyclesInInstrument(ICATNode node, boolean isMyData) {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        try {
            ArrayList<TFacilityCycle> facilityCycleList = utilityManager.getFacilityCyclesWithInstrument(
                    getSessionId(), node.getFacility(), node.getInstrumentName());
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
        } catch (ICATMethodNotFoundException ex) { // Cycle method is not
                                                   // available try
                                                   // investigations directly
                                                   // from instruments
            result.addAll(createInvestigationNodesInInstrument(node, isMyData));
        }
        return result;
    }

    private ArrayList<ICATNode> createInvestigationNodesInInstrument(ICATNode node, boolean isMyData) {
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
                    inv.getTitle() + "(Id:" + inv.getInvestigationName() + ")");
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
     */
    private ArrayList<ICATNode> createInvestigationNodesInCycle(ICATNode node, boolean isMyData) {
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
     */
    private ArrayList<ICATNode> createDatasetNodesInInvestigation(ICATNode node) {
        ArrayList<ICATNode> result = new ArrayList<ICATNode>();
        ArrayList<TDataset> invList = utilityManager.getDatasetsInServer(getSessionId(), node.getFacility(),
                node.getInvestigationId());
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
     */
    private ArrayList<ICATNode> createDatafileNodesInDataset(ICATNode node) {
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

    /**
     * This method returns creates a parameter model which has list of parameter
     * names and corresponding values for a given datafile id on a server.
     * 
     * @param facilityName
     *            iCAT instance name
     * @param datafileId
     *            input datafile Id
     */
    @Override
    public ArrayList<ParameterModel> getDatafileParameters(String facilityName, String datafileId) {
        return getDatafileParameters(getSessionId(), facilityName, datafileId);
    }

    /**
     * This method returns creates a parameter model which has list of parameter
     * names and corresponding values for a given datafile id on a server.
     * 
     * @param sessionId
     *            user session id
     * @param facilityName
     *            iCAT instance name
     * @param datafileId
     *            input datafile id.
     * @return list of parameters corresponding to datafile
     */
    private ArrayList<ParameterModel> getDatafileParameters(String sessionId, String facilityName, String datafileId) {
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
     */
    @Override
    public ArrayList<DatasetModel> getDatasetsInInvestigations(String facilityName, String investigationId) {
        ArrayList<DatasetModel> result = new ArrayList<DatasetModel>();
        ArrayList<TDataset> dsList = utilityManager.getDatasetsInServer(getSessionId(), facilityName, investigationId);
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
     */
    @Override
    public ArrayList<DatafileModel> getDatafilesInDatasets(ArrayList<DatasetModel> datasets) {
        ArrayList<DatafileModel> result = new ArrayList<DatafileModel>();
        for (DatasetModel dataset : datasets) {
            ArrayList<TDatafile> dfList = utilityManager.getDatafilesInServer(getSessionId(),
                    dataset.getFacilityName(), dataset.getId());
            if (dfList == null)
                continue;
            for (TDatafile df : dfList) {
                if (df.getCreateTime() != null)
                    result.add(new DatafileModel(dataset.getFacilityName(), dataset.getName(), df.getId(),
                            df.getName(), df.getSize().toString(), df.getFormat(), df.getFormatVersion(), df
                                    .getFormatType(), df.getCreateTime(), df.getLocation()));
                else
                    result.add(new DatafileModel(dataset.getFacilityName(), dataset.getName(), df.getId(),
                            df.getName(), df.getSize().toString(), df.getFormat(), df.getFormatVersion(), df
                                    .getFormatType(), null, df.getLocation()));
            }
        }
        return result;
    }

    /**
     * This method returns session id from the session information.
     * 
     * @return user session id
     */
    private String getSessionId() {
        HttpServletRequest request = this.getThreadLocalRequest();
        HttpSession session = request.getSession();
        String sessionId = null;
        if (session.getAttribute("SESSION_ID") == null) { // First time login
            try {
                sessionId = userManager.login();
                session.setAttribute("SESSION_ID", sessionId);
            } catch (AuthenticationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            sessionId = (String) session.getAttribute("SESSION_ID");
        }
        return sessionId;
    }

    /**
     * This method returns list of all children for a given input ICATNode.
     * 
     * @param node
     *            input parent ICATNode information.
     */
    @Override
    public ArrayList<ICATNode> getAllICATNodeChildren(ICATNode node) {
        // TODO Auto-generated method stub
        return getICATNodeChildren(node, false);
    }

    /**
     * This method returns list of all Children but only includes the ones that
     * the user has investigation rights.
     * 
     * @param node
     *            input parent ICATNode information.
     */
    @Override
    public ArrayList<ICATNode> getMyICATNodeChildren(ICATNode node) {
        // TODO Auto-generated method stub
        return getICATNodeChildren(node, true);
    }

    /**
     * This method returns URL string corresponding to a file that contains all
     * the requested datafiles.
     * 
     * @param facilityName
     *            iCAT Instance name
     * @param datafileIds
     *            Array list of datafile ids
     */
    @Override
    public String getDatafilesDownloadURL(String facilityName, ArrayList<Long> datafileIds) {
        // TODO Auto-generated method stub
        return utilityManager.getDatafilesDownloadURL(getSessionId(), facilityName, datafileIds);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getDatasetDownloadURL(java
     * .lang.String, java.lang.Long)
     */
    @Override
    public String getDatasetDownloadURL(String facilityName, Long datasetId) {
        return utilityManager.getDatasetDownloadURL(getSessionId(), facilityName, datasetId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getAllICATNodeDatafiles(uk
     * .ac.stfc.topcat.gwt.client.model.ICATNode)
     */
    @Override
    public HashMap<String, ArrayList<ICATNode>> getAllICATNodeDatafiles(ICATNode node) {
        ArrayList<ICATNode> resultNodes = getICATNodeChildren(node, false);
        if (resultNodes.size() != 0 && resultNodes.get(0).getNodeType() != ICATNodeType.DATAFILE) {
            HashMap<String, ArrayList<ICATNode>> result = new HashMap<String, ArrayList<ICATNode>>();
            result.put("", resultNodes);
            return result;
        } else {
            return createDatafilesHierarchy(resultNodes);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * uk.ac.stfc.topcat.gwt.client.UtilityService#getMyICATNodeDatafiles(uk
     * .ac.stfc.topcat.gwt.client.model.ICATNode)
     */
    @Override
    public HashMap<String, ArrayList<ICATNode>> getMyICATNodeDatafiles(ICATNode node) {
        ArrayList<ICATNode> resultNodes = getICATNodeChildren(node, true);
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
     */
    @Override
    public ArrayList<TInvestigation> getMyInvestigationsInServer(String facilityName) {
        return utilityManager.getMyInvestigationsInServer(getSessionId(), facilityName);
    }
    
}
