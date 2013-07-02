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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.apache.log4j.Logger;
import org.icatproject.idsclient.Client;
import org.icatproject.idsclient.Status;
import org.icatproject.idsclient.exceptions.BadRequestException;
import org.icatproject.idsclient.exceptions.ForbiddenException;
import org.icatproject.idsclient.exceptions.IDSException;
import org.icatproject.idsclient.exceptions.NotFoundException;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserDownload;
import uk.ac.stfc.topcat.ejb.session.DownloadManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal;
import uk.ac.stfc.topcat.ejb.session.UtilityLocal;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.DownloadService;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * This is servlet implementation of Download methods.
 * 
 */
@SuppressWarnings("serial")
public class DownloadServiceImpl extends RemoteServiceServlet implements DownloadService {
    private DownloadManagementBeanLocal downloadManager = null;
    private UtilityLocal utilityManager = null;
    private UserManagementBeanLocal userManager = null;
    private static String RESTFUL_DOWNLOAD_SERVICE = "restfulDownload";
    private final static Logger logger = Logger.getLogger(DownloadServiceImpl.class.getName());

    /**
     * Servlet Init method.
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
        try {
            // create initial context
            Context ctx = new InitialContext();
            downloadManager = (DownloadManagementBeanLocal) ctx
                    .lookup("java:global/TopCAT/DownloadManagementBean!uk.ac.stfc.topcat.ejb.session.DownloadManagementBeanLocal");
            utilityManager = (UtilityLocal) ctx
                    .lookup("java:global/TopCAT/UtilityBean!uk.ac.stfc.topcat.ejb.session.UtilityLocal");
            userManager = (UserManagementBeanLocal) ctx
                    .lookup("java:global/TopCAT/UserManagementBean!uk.ac.stfc.topcat.ejb.session.UserManagementBeanLocal");
        } catch (NamingException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<DownloadModel> getMyDownloads(Set<String> facilities) throws TopcatException {
        if (logger.isInfoEnabled()) {
            logger.info("getMyDownloads: facilities.size() (" + facilities.size() + ")");
        }
        List<DownloadModel> result = new ArrayList<DownloadModel>();
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();
        for (String facilityName : facilities) {
            List<TopcatUserDownload> dlList = downloadManager.getMyDownloads(getSessionId(), facilityName);
            if (dlList == null)
                continue;
            for (TopcatUserDownload dl : dlList) {
                DownloadModel downloadModel = convertDownload(facilityName, facilityMapping.get(facilityName)
                        .getDownloadPluginName(), dl);
                if (downloadModel.getPluginName().equalsIgnoreCase(Constants.IDS)) {
                    if (isAvailableIDS(facilityMapping.get(facilityName).getDownloadServiceUrl(), downloadModel)) {
                        result.add(downloadModel);
                    }
                } else if (downloadModel.getPluginName().equalsIgnoreCase(Constants.RESTFUL_DOWNLOAD)) {
                    downloadModel.refresh();
                    if (!downloadModel.getStatus().equals(Constants.STATUS_EXPIRED)) {
                        result.add(downloadModel);
                    }
                } else {
                    logger.error("getMyDownloadList: unsupported download plugin: " + downloadModel.getPluginName());
                    throw new InternalException("Error getMyDownloadList only suports IDS and not "
                            + downloadModel.getPluginName());
                }
            }
        }
        return result;
    }

    /**
     * Call the IDS to see if the download is still available.
     * 
     * @param downloadServiceUrl
     *            a string containing the url of the data service
     * @param downloadModel
     *            the download to check
     * @return <code>true</code> if the download is still available
     * @throws TopcatException
     */
    private boolean isAvailableIDS(String downloadServiceUrl, DownloadModel downloadModel) throws TopcatException {
        if (logger.isDebugEnabled()) {
            logger.debug("isAvailableIDS: downloadServiceUrl (" + downloadServiceUrl + "),  downloadModel.getId() ("
                    + downloadModel.getId() + ")");
        }
        Client ids = getIDSClient(downloadServiceUrl);
        try {
            ids.getStatus(downloadModel.getPreparedId());
        } catch (NotFoundException e) {
            // assume this one is finished, possibly expired
            if (logger.isTraceEnabled()) {
                logger.trace("isAvailableIDS: NotFoundException downloadModel.getId() (" + downloadModel.getId() + ")");
            }
            downloadManager.delete(getSessionId(), downloadModel.getId());
            downloadModel.setStatus(Constants.STATUS_EXPIRED);
            return false;
        } catch (IDSException e) {
            // something has gone wrong
            logger.error("isAvailableIDS: " + e.getMessage());
            throw new InternalException("Error returned from the data service. " + e.getMessage());
        } catch (IOException e) {
            // cannot contact IDS, network issues?
            logger.error("isAvailableIDS: " + e.getMessage());
            throw new InternalException("Unable to contact the data service, " + downloadModel.getUrl());
        }
        return true;
    }

    @Override
    public List<DownloadModel> checkForFinalStatus(Set<DownloadModel> downloadModels) throws TopcatException {
        if (logger.isInfoEnabled()) {
            logger.info("checkForFinalStatus: downloadModels.size() (" + downloadModels.size() + ")");
        }
        List<DownloadModel> finalStateReached = new ArrayList<DownloadModel>();
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();
        for (DownloadModel downloadModel : downloadModels) {
            DownloadModel updatedDownloadModel = null;
            if (downloadModel.getPluginName().equalsIgnoreCase(Constants.IDS)) {
                updatedDownloadModel = checkForFinalStatusIDS(facilityMapping.get(downloadModel.getFacilityName())
                        .getDownloadServiceUrl(), downloadModel);
            } else {
                updatedDownloadModel = checkForFinalStatusRDS(downloadModel);
            }
            if (updatedDownloadModel != null) {
                finalStateReached.add(updatedDownloadModel);
            }
        }
        return finalStateReached;
    }

    /**
     * Mimic the behaviour of the legacy code.
     * 
     * @param downloadServiceUrl
     *            a string containing the url of the data service
     * @param downloadModel
     * @return an updated DataModel is returned if the status is a final status
     *         otherwise null is returned
     * @throws TopcatException
     */
    private DownloadModel checkForFinalStatusIDS(String downloadServiceUrl, DownloadModel downloadModel)
            throws TopcatException {
        if (logger.isDebugEnabled()) {
            logger.debug("checkForFinalStatusIDS: downloadModel.getId() (" + downloadModel.getId() + ")");
        }
        DownloadModel updatedDownloadModel = getStatusIDS(downloadServiceUrl, downloadModel);
        if (updatedDownloadModel.getStatus().equalsIgnoreCase(Constants.STATUS_AVAILABLE)
                || updatedDownloadModel.getStatus().equalsIgnoreCase(Constants.STATUS_ERROR)
                || updatedDownloadModel.getStatus().equalsIgnoreCase(Constants.STATUS_EXPIRED)) {
            return updatedDownloadModel;
        }
        return null;
    }

    /**
     * Get the status of the download from the data service. If it has changed
     * update the database. This method uses calls to the IDS.
     * 
     * @param downloadServiceUrl
     *            a string containing the url of the data service
     * @param downloadModel
     *            the download to check
     * @return an updated DataModel is returned if the status has changed
     *         otherwise the original model is returned. If the download is no
     *         longer available then a <code>null</code> is returned.
     * @throws TopcatException
     */
    private DownloadModel getStatusIDS(String downloadServiceUrl, DownloadModel downloadModel) throws TopcatException {
        if (logger.isDebugEnabled()) {
            logger.debug("getStatusIDS: downloadServiceUrl (" + downloadServiceUrl + "), downloadModel.getId() ("
                    + downloadModel.getId() + ")");
        }
        Client ids = getIDSClient(downloadServiceUrl);
        Status status = null;
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: ids.getStatus (" + downloadModel.getPreparedId() + ")");
            }
            status = ids.getStatus(downloadModel.getPreparedId());
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: ids.getStatus returned:" + status);
            }
        } catch (NotFoundException e) {
            // assume this one is finished, possibly expired
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: NotFoundException downloadModel.getId() (" + downloadModel.getId() + ")");
            }
            downloadManager.delete(getSessionId(), downloadModel.getId());
            downloadModel.setStatus(Constants.STATUS_EXPIRED);
            return downloadModel;
        } catch (IDSException e) {
            // something has gone wrong
            logger.error("getStatusIDS: " + e.getMessage());
            throw new InternalException("Error returned from the data service. " + e.getMessage());
        } catch (IOException e) {
            // cannot contact IDS, network issues?
            logger.error("getStatusIDS: " + e.getMessage());
            throw new InternalException("Unable to contact the data service, " + downloadModel.getUrl());
        }

        if (status == Status.ONLINE && !downloadModel.getStatus().equalsIgnoreCase(Constants.STATUS_AVAILABLE)) {
            // this one is recently finished
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: status updated to " + Constants.STATUS_AVAILABLE);
            }
            try {
                downloadModel.setUrl(ids.getDataUrl(downloadModel.getPreparedId(), downloadModel.getDownloadName()));
            } catch (UnsupportedEncodingException e) {
                logger.error("getStatusIDS: " + e.getMessage());
                throw new InternalException("Error returned from the data service. " + e.getMessage());
            }
            downloadModel.setStatus(Constants.STATUS_AVAILABLE);
            downloadModel.setStartDownload(true);
            downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(),
                    downloadModel.getStatus());

        } else if (status == Status.INCOMPLETE && !downloadModel.getStatus().equalsIgnoreCase(Constants.STATUS_ERROR)) {
            // this one is recently finished
            // TODO what to do, give the user an incomplete set?
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: status updated to " + Constants.STATUS_ERROR);
            }
            try {
                downloadModel.setUrl(ids.getDataUrl(downloadModel.getPreparedId(), downloadModel.getDownloadName()));
            } catch (UnsupportedEncodingException e) {
                logger.error("getStatusIDS: " + e.getMessage());
                throw new InternalException("Error returned from the data service. " + e.getMessage());
            }
            downloadModel.setStatus(Constants.STATUS_ERROR);
            downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(),
                    downloadModel.getStatus());
        }

        if (logger.isTraceEnabled()) {
            logger.trace("getStatusIDS: return downloadModel id ( " + downloadModel.getId() + ") status ( "
                    + downloadModel.getStatus() + ")");
        }
        return downloadModel;
    }

    @Override
    public DownloadModel prepareDataObjectsForDownload(String dataType, TFacility facility, List<Long> dataObjectList,
            String downloadName) throws TopcatException {
        if (logger.isInfoEnabled()) {
            logger.info("prepareDataObjectsForDownload: dataType (" + dataType + "), facility(" + facility.toString()
                    + "), dataObjectList.size() (" + dataObjectList.size() + "), downloadName (" + downloadName + ")");
        }
        if (facility.getDownloadPluginName().equalsIgnoreCase(Constants.IDS)) {
            return prepareDataObjectsForDownloadIDS(dataType, facility, dataObjectList, downloadName);
        } else {
            // oops, not an IDS
            logger.error("prepareDataObjectsForDownload only suports IDS and not " + facility.getDownloadPluginName());
            throw new InternalException("Error prepareDataObjectsForDownload only suports IDS and not "
                    + facility.getDownloadPluginName());
        }
    }

    /**
     * Call out to the IDS and construct a DownloadModel.
     * 
     * @param dataType
     * @param facility
     * @param dataObjectList
     * @param downloadName
     * @return a DownloadModel
     * @throws TopcatException
     */
    private DownloadModel prepareDataObjectsForDownloadIDS(String dataType, TFacility facility,
            List<Long> dataObjectList, String downloadName) throws TopcatException {
        if (logger.isDebugEnabled()) {
            logger.debug("prepareDataObjectsForDownloadIDS: dataType (" + dataType + "), facility("
                    + facility.toString() + "), dataObjectList.size() (" + dataObjectList.size() + "), downloadName ("
                    + downloadName + ")");
        }
        String sessionId = userManager.getIcatSessionId(getSessionId(), facility.getName());
        Date submitTime = new Date(System.currentTimeMillis());
        Client ids = getIDSClient(facility.getDownloadServiceUrl());
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
            logger.error("prepareDataObjectsForDownloadIDS: " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (IOException e) {
            logger.error("prepareDataObjectsForDownloadIDS: " + e.getMessage());
            throw new InternalException("Error trying to contact the download service. " + e.getMessage());
        }
        DownloadModel dm = new DownloadModel();
        dm.setDownloadName(downloadName);
        dm.setFacilityName(facility.getName());
        dm.setPluginName(Constants.IDS);
        dm.setPreparedId(preparedId);
        dm.setStartDownload(false);
        dm.setStatus(Constants.STATUS_IN_PROGRESS);
        dm.setSubmitTime(submitTime);
        dm.setUrl(facility.getDownloadServiceUrl());
        Long id = downloadManager.add(getSessionId(), facility.getName(), submitTime, downloadName,
                Constants.STATUS_IN_PROGRESS, null, facility.getDownloadServiceUrl(), preparedId);
        dm.setId(id);
        return dm;
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

    /**
     * Get a map of facility names to <code>TFacility</code>. This involves a
     * database lookup.
     * 
     * @return a map of facility names to <code>TFacility</code>.
     */
    private Map<String, TFacility> getDownloadPluginMapping() {
        Map<String, TFacility> downloadPlugins = new HashMap<String, TFacility>();
        List<TFacility> facilities = utilityManager.getFacilities();
        for (TFacility facility : facilities) {
            downloadPlugins.put(facility.getName(), facility);
        }
        return downloadPlugins;
    }

    /**
     * Create a data service client.
     * 
     * @param url
     *            a string containing the url of the icat data service
     * @return an Icat Data Service <code>Client</code>
     * @throws InternalException
     */
    private Client getIDSClient(String url) throws InternalException {
        if (logger.isDebugEnabled()) {
            logger.debug("getIDSClient: url (" + url + ")");
        }
        Client ids = null;
        try {
            ids = new Client(url);
        } catch (BadRequestException e) {
            logger.error("getIDSClient: " + e.getMessage());
            throw new InternalException("Error bad url for the download service. " + e.getMessage());
        }
        return ids;
    }

    /**
     * Create a DownloadModel from a TopcatUserDownload
     * 
     * @param facilityName
     *            a sting containing the name of the facility
     * @param pluginName
     *            a string containing the name of the download plugin
     * @param dl
     *            a TopcatUserDownload
     * @return a DownloadModel
     */
    private DownloadModel convertDownload(String facilityName, String pluginName, TopcatUserDownload dl) {
        DownloadModel dm = new DownloadModel();
        dm.setDownloadName(dl.getName());
        dm.setFacilityName(facilityName);
        dm.setId(dl.getId());
        dm.setPluginName(pluginName);
        dm.setPreparedId(dl.getPreparedId());
        dm.setStartDownload(false);
        dm.setStatus(dl.getStatus());
        dm.setSubmitTime(dl.getSubmitTime());
        dm.setUrl(dl.getUrl());
        return dm;
    }

    @Deprecated
    private DownloadModel checkForFinalStatusRDS(DownloadModel downloadModel) throws SessionException {
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
                    downloadManager.delete(getSessionId(), downloadModel.getId());
                    downloadModel.setStatus(Constants.STATUS_EXPIRED);
                    return downloadModel;
                } else {
                    logger.error("checkForFinalStatusRDS: " + e.getMessage());
                    throw new IOException(e);
                }
            }
            String inputLine = in.readLine();
            if (inputLine.equalsIgnoreCase("COMPLETED")) {
                // this one is finished
                downloadModel.setStatus(Constants.STATUS_AVAILABLE);
                downloadModel.setStartDownload(true);
                downloadModel.setUrl(requestUrl + "/Download?Filename=" + downloadModel.getDownloadName());
                downloadManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(), requestUrl,
                        requestUrl + "/Download?Filename=" + downloadModel.getDownloadName(),
                        Constants.STATUS_AVAILABLE);
                return downloadModel;
            } else if (inputLine.equalsIgnoreCase("ERROR")) {
                // this one is finished
                downloadModel.setStatus(Constants.STATUS_ERROR);
                downloadModel.setUrl(requestUrl + "/Download?Filename=" + downloadModel.getDownloadName());
                downloadManager.updateDownloadStatus(getSessionId(), downloadModel.getFacilityName(), requestUrl,
                        requestUrl + "/Download?Filename=" + downloadModel.getDownloadName(), Constants.STATUS_ERROR);
                return downloadModel;
            }
            in.close();
        } catch (IOException e) {
            // download service unavailable
            logger.error("checkForFinalStatusRDS: " + e.getMessage());
            try {
                in.close();
            } catch (IOException e1) {
            } catch (NullPointerException e1) {
            }
        }
        return null;
    }

    @Deprecated
    @Override
    public DownloadModel getDatafilesDownloadURL(String facilityName, List<Long> datafileIds, String downloadName)
            throws TopcatException {
        if (logger.isInfoEnabled()) {
            logger.info("getDatafilesDownloadURL: facilityName (" + facilityName + "), datafileIds.size ("
                    + datafileIds.size() + "), downloadName (" + downloadName + ")");
        }
        String status;
        boolean startDownload = false;
        Date submitTime = new Date(System.currentTimeMillis());
        Date expiryTime = null;
        Long id = null;
        String url = downloadManager.getDatafilesDownloadURL(getSessionId(), facilityName, datafileIds);
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();
        String downloadPluginName = facilityMapping.get(facilityName).getDownloadPluginName();
        if (downloadPluginName != null && downloadPluginName.equalsIgnoreCase(RESTFUL_DOWNLOAD_SERVICE)) {
            status = Constants.STATUS_IN_PROGRESS;
            expiryTime = getExpiryTime(url);
            id = downloadManager.add(getSessionId(), facilityName, submitTime, downloadName, status, expiryTime, url,
                    null);
        } else {
            status = Constants.STATUS_AVAILABLE;
            startDownload = true;
        }
        DownloadModel dm = new DownloadModel();
        dm.setDownloadName(downloadName);
        dm.setExpiryTime(expiryTime);
        dm.setFacilityName(facilityName);
        dm.setId(id);
        dm.setPluginName(downloadPluginName);
        dm.setStartDownload(startDownload);
        dm.setStatus(status);
        dm.setSubmitTime(submitTime);
        dm.setUrl(url);
        return dm;
    }

    @Deprecated
    @Override
    public DownloadModel getDatasetDownloadURL(String facilityName, Long datasetId, String downloadName)
            throws TopcatException {
        if (logger.isInfoEnabled()) {
            logger.info("getDatasetDownloadURL: facilityName (" + facilityName + "), datasetId (" + datasetId
                    + "), downloadName (" + downloadName + ")");
        }
        String status;
        boolean startDownload = false;
        Date submitTime = new Date(System.currentTimeMillis());
        Date expiryTime = null;
        Long id = null;
        String url = downloadManager.getDatasetDownloadURL(getSessionId(), facilityName, datasetId);
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();
        String downloadPluginName = facilityMapping.get(facilityName).getDownloadPluginName();
        if (downloadPluginName.equalsIgnoreCase(RESTFUL_DOWNLOAD_SERVICE)) {
            status = Constants.STATUS_IN_PROGRESS;
            expiryTime = getExpiryTime(url);
            id = downloadManager.add(getSessionId(), facilityName, submitTime, downloadName, status, expiryTime, url,
                    null);
        } else {
            status = Constants.STATUS_AVAILABLE;
            startDownload = true;
        }
        DownloadModel dm = new DownloadModel();
        dm.setDownloadName(downloadName);
        dm.setExpiryTime(expiryTime);
        dm.setFacilityName(facilityName);
        dm.setId(id);
        dm.setPluginName(downloadPluginName);
        dm.setStartDownload(startDownload);
        dm.setStatus(status);
        dm.setSubmitTime(submitTime);
        dm.setUrl(url);
        return dm;
    }

    @Deprecated
    private Date getExpiryTime(String url) {
        Date expiryTime = null;
        try {
            URL statusUrl = new URL(url + "/ExpiryTime");
            BufferedReader in = new BufferedReader(new InputStreamReader(statusUrl.openStream()));
            SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
            expiryTime = df.parse(in.readLine());
            in.close();
        } catch (IOException e) {
            logger.error("getExpiryTime: " + e.getMessage());
        } catch (ParseException e) {
            logger.error("getExpiryTime: " + e.getMessage());
        }
        return expiryTime;
    }
}
