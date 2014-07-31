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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.icatproject.ids.client.BadRequestException;
import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient;
import org.icatproject.ids.client.IdsClient.Flag;
import org.icatproject.ids.client.IdsClient.Status;
import org.icatproject.ids.client.IdsException;
import org.icatproject.ids.client.InsufficientPrivilegesException;
import org.icatproject.ids.client.NotFoundException;
import org.icatproject.ids.client.NotImplementedException;

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
import uk.ac.stfc.topcat.gwt.shared.model.TopcatDataSelection;
import uk.ac.stfc.topcat.gwt.shared.IdsFlag;


/**
 * This is servlet implementation of Download methods.
 *
 */
@SuppressWarnings("serial")
public class DownloadServiceImpl extends UrlBasedRemoteServiceServlet implements DownloadService {
    @EJB
    private DownloadManagementBeanLocal downloadManager;
    @EJB
    private UtilityLocal utilityManager;
    @EJB
    private UserManagementBeanLocal userManager;
    //private static String RESTFUL_DOWNLOAD_SERVICE = "restfulDownload";
    private final static Logger logger = Logger.getLogger(DownloadServiceImpl.class.getName());
    //path of the ids url

    /**
     * Servlet Init method.
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
    }

    @Override
    public List<DownloadModel> getMyDownloads(Set<String> facilities) throws TopcatException {
        logger.debug("getMyDownloads called");

        if (logger.isInfoEnabled()) {
            logger.info("getMyDownloads: facilities.size() (" + facilities.size() + ")");
        }
        List<DownloadModel> result = new ArrayList<DownloadModel>();
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();
        for (String facilityName : facilities) {
            List<TopcatUserDownload> dlList = downloadManager.getMyDownloads(getSessionId(), facilityName);

            if (logger.isInfoEnabled()) {
                logger.info("getMyDownloads: Number of myDownloads (" + dlList.size() + ")");
            }

            if (dlList == null)
                continue;
            for (TopcatUserDownload dl : dlList) {
                DownloadModel downloadModel = convertDownload(facilityName, facilityMapping.get(facilityName)
                        .getDownloadPluginName(), dl);
                if (downloadModel.getPluginName().equalsIgnoreCase(Constants.IDS)) {
                    boolean isAvailable = false;
                    try {
                        isAvailable = isAvailableIDS(facilityMapping.get(facilityName).getDownloadServiceUrl(), downloadModel);
                    } catch(NotFoundException e) {
                        if (downloadModel.getExpiryTime() == null) {
                            Date dt = new Date();
                            Calendar c = Calendar.getInstance();
                            c.setTime(dt);
                            c.add(Calendar.DATE, 1);
                            dt = c.getTime();

                            downloadModel.setExpiryTime(dt);
                            downloadManager.updateExpiryTime(getSessionId(), downloadModel.getId(), dt);
                        }

                        downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(),
                                Constants.STATUS_EXPIRED);
                        result.add(downloadModel);
                        //skip loop
                        continue;
                    }

                    if (isAvailable == true) {
                        downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(),
                                Constants.STATUS_AVAILABLE);
                        result.add(downloadModel);
                    } else {
                        downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(),
                                Constants.STATUS_IN_PROGRESS);
                        result.add(downloadModel);
                    }

                    //clean up downloads that has expired
                    if (downloadModel.getExpiryTime() != null) {
                        Date now = new Date();
                        if (downloadModel.getExpiryTime().after(now)) {
                          downloadManager.delete(getSessionId(), downloadModel.getId());
                        }
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
     * @throws NotFoundException
     */
    private boolean isAvailableIDS(String downloadServiceUrl, DownloadModel downloadModel) throws TopcatException, NotFoundException {
        logger.debug("isAvailableIDS called");

        if (logger.isDebugEnabled()) {
            logger.debug("isAvailableIDS: downloadServiceUrl (" + downloadServiceUrl + "),  downloadModel.getId() ("
                    + downloadModel.getId() + ")");
        }

        IdsClient ids;
        boolean isPrepared = false;

        try {
			ids = getIDSClient(downloadServiceUrl);
		} catch (MalformedURLException e) {
			//invalid url
			logger.error("isAvailableIDS: " + e.getMessage());
			throw new InternalException("Unable to contact the data service, " + downloadServiceUrl + " url appears to be invalid.");
		}
        try {
            isPrepared = ids.isPrepared(downloadModel.getPreparedId());
        } catch (NotFoundException e) {
            // assume this one is finished, possibly expired
            if (logger.isTraceEnabled()) {
                logger.trace("isAvailableIDS: NotFoundException downloadModel.getId() (" + downloadModel.getId() + ")" + e.getMessage());
            }
            //downloadManager.delete(getSessionId(), downloadModel.getId());
            //downloadModel.setStatus(Constants.STATUS_EXPIRED);
            //return false;
            if (e.getMessage().contains("is not known")) {
                downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_EXPIRED);
            } else {
                downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_ERROR);
            }

            throw new NotFoundException("isAvailableIDS: NotFoundException: " + e.getMessage());
        } catch (IdsException e) {
            // something has gone wrong
            logger.error("isAvailableIDS: " + e.getMessage());
            throw new InternalException("Error returned from the data service. " + e.getMessage());
        }

        return isPrepared;
    }

    @Override
    public List<DownloadModel> checkForFinalStatus(Set<DownloadModel> downloadModels) throws TopcatException {

        logger.debug("checkForFinalStatus called");

        //if (logger.isInfoEnabled()) {
            logger.info("checkForFinalStatus: downloadModels.size() (" + downloadModels.size() + ")");
        //}
        List<DownloadModel> finalStateReached = new ArrayList<DownloadModel>();
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();
        for (DownloadModel downloadModel : downloadModels) {
            DownloadModel updatedDownloadModel = null;
            if (downloadModel.getPluginName().equalsIgnoreCase(Constants.IDS)) {

                logger.debug("checkForFinalStatus: " + downloadModel.getDownloadName());

                updatedDownloadModel = checkForFinalStatusIDS(facilityMapping.get(downloadModel.getFacilityName())
                        .getDownloadServiceUrl(), downloadModel);
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

        logger.debug("getStatusIDS called");

        if (logger.isDebugEnabled()) {
            logger.debug("getStatusIDS: downloadServiceUrl (" + downloadServiceUrl + "), downloadModel.getId() (" + downloadModel.getId() + ")");
        }
        IdsClient ids;
		try {
			ids = getIDSClient(downloadServiceUrl);
		} catch (MalformedURLException e) {
			logger.error("getStatusIDS: " + e.getMessage());
            throw new InternalException("Unable to contact the data service, " + downloadModel.getUrl() + " url appears to be invalid.");
		}

        boolean isPrepared;

        try {
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: preparedId (" + downloadModel.getPreparedId() + ")");
            }
            isPrepared = ids.isPrepared(downloadModel.getPreparedId());

            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: isPrepared:" + isPrepared);
            }
        } catch (NotFoundException e) {
            // assume this one is finished, possibly expired
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: NotFoundException downloadModel.getId() (" + downloadModel.getId() + ")" + e.getMessage());
            }

            isPrepared = false;
            downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_EXPIRED);

            //downloadManager.delete(getSessionId(), downloadModel.getId());
            //downloadModel.setStatus(Constants.STATUS_EXPIRED);
            return downloadModel;
        } catch (BadRequestException e) {
            logger.error("getStatusIDS: BadRequestException " + e.getMessage());
            throw new InternalException("Error returned from the data service. " + e.getMessage());
        } catch (IdsException e) {
            // something has gone wrong
            logger.error("getStatusIDS: IdsException" + e.getMessage());
            throw new InternalException("Error returned from the data service. " + e.getMessage());
        }


        if (isPrepared == true) {
            // this one is recently finished
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: status updated to " + Constants.STATUS_AVAILABLE);
            }

            URL downloadUrl = null;

            if (downloadModel.getDownloadName().isEmpty()) {
                downloadUrl = ids.getDataUrl(downloadModel.getPreparedId(), null);
                logger.debug("getStatusIDS: downloadUrl: " + downloadUrl.toString());
                downloadModel.setUrl(downloadUrl.toString());

            } else {
                downloadUrl = ids.getDataUrl(downloadModel.getPreparedId(), downloadModel.getDownloadName());
                logger.debug("getStatusIDS: downloadUrl: " + downloadUrl.toString());
                downloadModel.setUrl(downloadUrl.toString());
            }

            downloadModel.setStatus(Constants.STATUS_AVAILABLE);
            downloadModel.setStartDownload(true);
            downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), downloadModel.getStatus());

        } else if (isPrepared == false) {
            // this one is recently finished
            // TODO what to do, give the user an incomplete set?
            if (logger.isTraceEnabled()) {
                logger.trace("getStatusIDS: status updated to " + Constants.STATUS_IN_PROGRESS);
            }

            URL downloadUrl = null;

            downloadUrl = ids.getDataUrl(downloadModel.getPreparedId(), downloadModel.getDownloadName());

            logger.debug("getStatusIDS: downloadUrl: " + downloadUrl.toString());

            downloadModel.setUrl(downloadUrl.toString());
            downloadModel.setStatus(Constants.STATUS_IN_PROGRESS);
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
    public DownloadModel prepareDataObjectsForDownload(TFacility facility, TopcatDataSelection dataSelection,
            String downloadName, IdsFlag flag) throws TopcatException {

        if (facility.getDownloadPluginName().equalsIgnoreCase(Constants.IDS)) {
            return prepareDataObjectsForDownloadIDS(facility, dataSelection, downloadName, flag);
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
    private DownloadModel prepareDataObjectsForDownloadIDS(TFacility facility,
            TopcatDataSelection dataSelection, String downloadName, IdsFlag flag) throws TopcatException {

        logger.debug("prepareDataObjectsForDownloadIDS: facility(" + facility.toString() + "), dataselection (" +
                dataSelection.getParameters().toString() + "), downloadName (" + downloadName + "), flag ( " + flag.toString() + " )");

        String sessionId = userManager.getIcatSessionId(getSessionId(), facility.getName());
        Date submitTime = new Date(System.currentTimeMillis());

        IdsClient ids;
		try {
			ids = getIDSClient(facility.getDownloadServiceUrl());
		} catch (MalformedURLException e) {
			logger.error("prepareDataObjectsForDownloadIDS: getIDSClient " + e.getMessage());
            throw new InternalException("Error trying to contact the download service at " + facility.getDownloadServiceUrl() + " url appears to be invalid.");
        }
        String preparedId = "";
        DataSelection idsDataSelection = convertToIdsDataSelection(dataSelection);

        try {
            preparedId = ids.prepareData(sessionId, idsDataSelection, getCompressionFlag(flag));

        } catch (InsufficientPrivilegesException e) {
            throw new SessionException();
        } catch (NotImplementedException e) {
            logger.error("prepareDataObjectsForDownloadIDS: " + "not implemented error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (BadRequestException e) {
            logger.error("prepareDataObjectsForDownloadIDS: " + "bad request error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (NotFoundException e) {
            logger.error("prepareDataObjectsForDownloadIDS: " + "not found error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (org.icatproject.ids.client.InternalException e) {
            logger.error("prepareDataObjectsForDownloadIDS: " + "internal error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
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
     * Call out to the IDS and return a download url.
     *
     * @param dataType
     * @param facility
     * @param dataObjectList
     * @param downloadName
     * @param flag
     * @return ok string
     * @throws TopcatException
     * @throws UnsupportedEncodingException
     * @throws MalformedURLException
     */
    @Override
    public DownloadModel directDownloadFromIDS(TFacility facility,
            TopcatDataSelection dataSelection, String downloadName, IdsFlag flag) throws TopcatException {

        logger.debug("prepareDataObjectsForDownloadIDS: facility(" + facility.toString() + "), dataselection (" +
                dataSelection.getParameters().toString() + "), downloadName (" + downloadName + "), flag ( " + flag.toString() + " )");

        String sessionId = userManager.getIcatSessionId(getSessionId(), facility.getName());
        IdsClient ids;
        try {
            ids = getIDSClient(facility.getDownloadServiceUrl());
        } catch (MalformedURLException e) {
            logger.error("directDownloadFromIDS: getIDSClient " + e.getMessage());
            throw new InternalException("Error trying to contact the download service at " + facility.getDownloadServiceUrl() + " url appears to be invalid.");
        }

        DataSelection idsDataSelection = convertToIdsDataSelection(dataSelection);

        Status status = null;
        try {
            status = ids.getStatus(sessionId, idsDataSelection);
        } catch (InsufficientPrivilegesException e) {
            throw new SessionException();
        } catch (NotImplementedException e) {
            logger.error("directDownloadFromIDS: " + "not implemented error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (BadRequestException e) {
            logger.error("directDownloadFromIDS: " + "bad request error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (NotFoundException e) {
            logger.error("directDownloadFromIDS: " + "not found error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        } catch (org.icatproject.ids.client.InternalException e) {
            logger.error("directDownloadFromIDS: " + "internal error " + e.getMessage());
            throw new InternalException("Error returned from the download service. " + e.getMessage());
        }

        DownloadModel dm = new DownloadModel();

        URL downloadUrl = null;
        if (status.equals(Status.ONLINE)) {
            //make sure downloadName is not an empty string. Use null if empty.
            if (downloadName != null) {
                if (downloadName.trim().isEmpty()) {
                    downloadName  = null;
                }
            }

            downloadUrl = ids.getDataUrl(sessionId, idsDataSelection, getCompressionFlag(flag), downloadName);

            if (downloadUrl != null) {
                logger.debug("directDownloadFromIDS: downloadUrl: " + downloadUrl.toString());
                dm.setUrl(downloadUrl.toString());
            } else {
                logger.debug("directDownloadFromIDS: downloadUrl: Unable to retrieve download url from ids");
                throw new TopcatException("Unable to retrieve download url from ids");
            }
        }

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
     * @throws MalformedURLException
     */
    private IdsClient getIDSClient(String url) throws InternalException, MalformedURLException {
        logger.debug("getIDSClient: url (" + url + ")");

        URL urlObject = new URL(url);

        IdsClient ids = null;
        ids = new IdsClient(urlObject);
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

    /*
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
    */

    /*
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
    */

    /*
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
    */

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


    private Flag getCompressionFlag(IdsFlag flag) {
        if (flag == IdsFlag.ZIP_AND_COMPRESS) {
            return Flag.ZIP_AND_COMPRESS;
        }

        if (flag == IdsFlag.ZIP) {
            return Flag.ZIP;
        }

        if (flag == IdsFlag.COMPRESS) {
            return Flag.COMPRESS;
        }

        return Flag.NONE;

    }

    private DataSelection convertToIdsDataSelection(TopcatDataSelection topcatDataselection) {
        DataSelection dataSelection = new DataSelection();

        dataSelection.addInvestigations(new ArrayList<Long>(topcatDataselection.getInvestigationIds()));
        dataSelection.addDatafiles(new ArrayList<Long>(topcatDataselection.getDatafileIds()));
        dataSelection.addDatasets(new ArrayList<Long>(topcatDataselection.getDatasetIds()));

        return dataSelection;

    }


}
