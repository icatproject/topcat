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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
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
import uk.ac.stfc.topcat.gwt.shared.IdsFlag;
import uk.ac.stfc.topcat.gwt.shared.model.TopcatDataSelection;


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
    private final static Logger logger = Logger.getLogger(DownloadServiceImpl.class.getName());
    private boolean debugEnabled = logger.isDebugEnabled();
    private boolean infoEnabled = logger.isInfoEnabled();


    /**
     * Servlet Init method.
     */
    @Override
    public void init(ServletConfig conf) throws ServletException {
        super.init(conf);
    }

    @Override
    public List<DownloadModel> getMyDownloads(Set<String> facilities) throws TopcatException {
        if (infoEnabled) {
            logger.info("getMyDownloads: facilities.size() (" + facilities.size() + ")");
        }

        List<DownloadModel> result = new ArrayList<DownloadModel>();
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();

        Date expiryDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(expiryDate);
        c.add(Calendar.DATE, 1);
        expiryDate = c.getTime();

        for (String facilityName : facilities) {
            List<TopcatUserDownload> dlList = downloadManager.getMyDownloads(getSessionId(), facilityName);

            if (infoEnabled) {
                logger.info("getMyDownloads: Facilty (" + facilityName + "), Downloads (" + dlList.size() + ")" );
            }

            if (dlList == null) {
                continue;
            }

            for (TopcatUserDownload dl : dlList) {
                DownloadModel downloadModel = convertDownload(facilityName, facilityMapping.get(facilityName).getDownloadPluginName(), dl);
                if (downloadModel.getPluginName().equalsIgnoreCase(Constants.IDS)) {
                    //clean up download if expired
                    if (downloadModel.getExpiryTime() != null) {
                        Date now = new Date();
                        if (downloadModel.getExpiryTime().after(now)) {
                          downloadManager.delete(getSessionId(), downloadModel.getId());
                        }
                        continue;
                    }

                    //only process in progress or available downloads
                    if (downloadModel.getStatus().equals(Constants.STATUS_IN_PROGRESS) || downloadModel.getStatus().equals(Constants.STATUS_AVAILABLE) || downloadModel.getStatus().equals(Constants.STATUS_IDS_ERROR)) {
                        boolean isAvailable = false;

                        try {
                            isAvailable = isPreparedAvailable(facilityMapping.get(facilityName).getDownloadServiceUrl(), downloadModel);

                            if (isAvailable == true) {
                                downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_AVAILABLE);
                                result.add(downloadModel);
                            } else {
                                downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_IN_PROGRESS);
                                result.add(downloadModel);
                            }
                        } catch (NotFoundException e) {
                            if (downloadModel.getExpiryTime() == null) {
                                downloadModel.setExpiryTime(expiryDate);
                                downloadManager.updateExpiryTime(getSessionId(), downloadModel.getId(), expiryDate);
                            }
                            downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_EXPIRED, e.getMessage());
                            result.add(downloadModel);
                            continue;
                        } catch (org.icatproject.ids.client.InternalException e) {
                            logger.error("getMyDownloads: IDS Internal Exception:" + e.getMessage());
                            String message = "An error occurred trying to retrieve the status of preparedId " + downloadModel.getPreparedId();
                            downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_IDS_ERROR, message);
                            result.add(downloadModel);
                        } catch(BadRequestException | NotImplementedException e) {
                            if (downloadModel.getExpiryTime() == null) {
                                downloadModel.setExpiryTime(expiryDate);
                                downloadManager.updateExpiryTime(getSessionId(), downloadModel.getId(), expiryDate);
                            }
                            downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_ERROR, e.getMessage());
                            result.add(downloadModel);
                            continue;
                        }


                    } else {
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
     * Call the IDS to see if the download is available.
     *
     * @param downloadServiceUrl
     *            a string containing the url of the data service
     * @param downloadModel
     *            the download to check
     * @return <code>true</code> if the download is still available
     * @throws TopcatException
     * @throws NotFoundException
     */
    private boolean isPreparedAvailable(String downloadServiceUrl, DownloadModel downloadModel) throws TopcatException, NotFoundException, BadRequestException, org.icatproject.ids.client.InternalException, NotImplementedException  {
        if (debugEnabled) {
            logger.debug("isPreparedAvailable: downloadServiceUrl (" + downloadServiceUrl + "),  downloadModel.getId() ("
                    + downloadModel.getId() + ")");
        }

        IdsClient ids;
        try {
			ids = getIDSClient(downloadServiceUrl);
		} catch (MalformedURLException e) {
			//invalid url
			logger.error("isPreparedAvailable: " + e.getMessage());
			throw new InternalException("Unable to contact the data service, " + downloadServiceUrl + " url appears to be invalid.");
		}

        return ids.isPrepared(downloadModel.getPreparedId());
    }

    @Override
    public List<DownloadModel> checkForFinalStatus(Set<DownloadModel> downloadModels) throws TopcatException {        List<DownloadModel> finalStateReached = new ArrayList<DownloadModel>();
        Map<String, TFacility> facilityMapping = getDownloadPluginMapping();

        for (DownloadModel downloadModel : downloadModels) {
            DownloadModel updatedDownloadModel = null;
            if (downloadModel.getPluginName().equalsIgnoreCase(Constants.IDS)) {
                updatedDownloadModel = checkForFinalStatusIDS(facilityMapping.get(downloadModel.getFacilityName()).getDownloadServiceUrl(), downloadModel);
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
    private DownloadModel checkForFinalStatusIDS(String downloadServiceUrl, DownloadModel downloadModel) throws TopcatException {
        if (debugEnabled) {
            logger.debug("checkForFinalStatusIDS: downloadModel.getId() (" + downloadModel.getId() + ")");
        }

        DownloadModel updatedDownloadModel = getDownloadStatus(downloadServiceUrl, downloadModel);

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
    private DownloadModel getDownloadStatus(String downloadServiceUrl, DownloadModel downloadModel) throws TopcatException {
        if (debugEnabled) {
            logger.debug("getDownloadStatus: downloadServiceUrl (" + downloadServiceUrl + "), downloadModel.getId() (" + downloadModel.getId() + ")");
        }

        //we just need to check/update downloads with in progress or available status
        if (downloadModel.getStatus().equals(Constants.STATUS_IN_PROGRESS) || downloadModel.getStatus().equals(Constants.STATUS_AVAILABLE)) {

            IdsClient ids;
    		try {
    			ids = getIDSClient(downloadServiceUrl);
    		} catch (MalformedURLException e) {
    			logger.error("getDownloadStatus: " + e.getMessage());
                throw new InternalException("Unable to contact the data service, " + downloadModel.getUrl() + " url appears to be invalid.");
    		}

    		boolean isAvailable = false;

    		try {
                isAvailable = isPreparedAvailable(downloadServiceUrl, downloadModel);

                if (isAvailable == true) {
                    URL downloadUrl = null;

                    if (downloadModel.getDownloadName().isEmpty()) {
                        downloadUrl = ids.getDataUrl(downloadModel.getPreparedId(), null);
                        downloadModel.setUrl(downloadUrl.toString());
                    } else {
                        downloadUrl = ids.getDataUrl(downloadModel.getPreparedId(), downloadModel.getDownloadName());
                        downloadModel.setUrl(downloadUrl.toString());
                    }

                    downloadModel.setStatus(Constants.STATUS_AVAILABLE);
                    downloadModel.setStartDownload(true);
                    downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), downloadModel.getStatus());

                } else if (isAvailable == false) {
                    URL downloadUrl = null;
                    downloadUrl = ids.getDataUrl(downloadModel.getPreparedId(), downloadModel.getDownloadName());                    downloadModel.setUrl(downloadUrl.toString());
                    downloadModel.setStatus(Constants.STATUS_IN_PROGRESS);
                    downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), downloadModel.getStatus());
                }

            } catch (NotFoundException e) {
                downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_EXPIRED, e.getMessage());
            } catch (org.icatproject.ids.client.InternalException e) {
                logger.error("getMyDownloads: IDS Internal Exception:" + e.getMessage());
            } catch(BadRequestException | NotImplementedException e) {
                downloadManager.update(getSessionId(), downloadModel.getId(), downloadModel.getUrl(), Constants.STATUS_ERROR, e.getMessage());
            }
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
        if (debugEnabled) {
            logger.debug("prepareDataObjectsForDownloadIDS: facility(" + facility.toString() + "), dataselection (" +
                    dataSelection.getParameters().toString() + "), downloadName (" + downloadName + "), flag ( " + flag.toString() + " )");
        }

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

            if (e.getMessage().startsWith("Generated URI is of length")) {
                throw new InternalException("Error returned from the download service. " + e.getMessage() + ". Please reduce the number of items selected for download.");
            } else {
                throw new InternalException("Error returned from the download service. " + e.getMessage());
            }
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
                Constants.STATUS_IN_PROGRESS, null, null, facility.getDownloadServiceUrl(), preparedId);
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
        if (debugEnabled) {
            logger.debug("directDownloadFromIDS: facility(" + facility.toString() + "), dataselection (" +
                    dataSelection.getParameters().toString() + "), downloadName (" + downloadName + "), flag ( " + flag.toString() + " )");
        }

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
            if (e.getMessage().startsWith("Generated URI is of length")) {
                throw new InternalException("Error returned from the download service. " + e.getMessage() + ". Please reduce the number of items selected for download.");
            } else {
                throw new InternalException("Error returned from the download service. " + e.getMessage());
            }
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
                if (infoEnabled) {
                    logger.info("directDownloadFromIDS: downloadUrl: " + downloadUrl.toString());
                }
                dm.setUrl(downloadUrl.toString());
            } else {
                logger.error("directDownloadFromIDS: downloadUrl: Unable to retrieve download url from ids");
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
        if (session.getAttribute("SESSION_ID") == null) {
            // First time login
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
        dm.setMessage(dl.getMessage());
        dm.setSubmitTime(dl.getSubmitTime());
        dm.setUrl(dl.getUrl());
        return dm;
    }


    /**
     * Returns server side IDS Flag from client side topcat IdsFlag
     *
     * @param flag
     * @return
     */
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

    /**
     * Returns server side DataSelection object from client side topcat TopcatDataSelection object
     *
     * @param topcatDataselection
     * @return
     */
    private DataSelection convertToIdsDataSelection(TopcatDataSelection topcatDataselection) {
        DataSelection dataSelection = new DataSelection();

        dataSelection.addInvestigations(new ArrayList<Long>(topcatDataselection.getInvestigationIds()));
        dataSelection.addDatafiles(new ArrayList<Long>(topcatDataselection.getDatafileIds()));
        dataSelection.addDatasets(new ArrayList<Long>(topcatDataselection.getDatasetIds()));

        return dataSelection;

    }

    /**
     * Delete a download
     *
     */
    @Override
    public boolean deleteDownload(String facility, DownloadModel downloadModel) throws TopcatException {

        try {
            downloadManager.delete(getSessionId(), downloadModel.getId());

            if (infoEnabled) {
                logger.info("deleteDownload: download with id " + downloadModel.getId() + " named " +  downloadModel.getDownloadName() + " deleted");
            }
        } catch (Exception e) {
            logger.error("deleteDownload:" + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get the size of a download
     */
    @Override
    public Long getDataSelectionSize(TFacility facility, TopcatDataSelection topcatDataSelection) throws TopcatException {
        Long size = 0L;

        IdsClient ids;
        try {
            ids = getIDSClient(facility.getDownloadServiceUrl());
        } catch (MalformedURLException e) {
            //invalid url
            logger.error("getDataSelectionSize: " + e.getMessage());
            throw new InternalException("Unable to contact the data service, " + facility.getDownloadServiceUrl() + " url appears to be invalid.");
        }

        String sessionId = userManager.getIcatSessionId(getSessionId(), facility.getName());
        try {
            size = ids.getSize(sessionId, convertToIdsDataSelection(topcatDataSelection));

            if(infoEnabled) {
                logger.info("getDataSelectionSize: Facility (" + facility + "), Download size (" + size + ")");
            }
        } catch (BadRequestException e) {
            logger.error("getDataSelectionSize: " + "bad request error " + e.getMessage());

            if (e.getMessage().startsWith("Generated URI is of length")) {
                throw new InternalException("Unable to retrieve size from data service: Please reduce the number of items selected.");
            } else {
                throw new InternalException("Unable to retrieve size from data service: " + e.getMessage());
            }
        } catch (IdsException e) {
            logger.error("getDataSelectionSize: " + e.getMessage());
            throw new InternalException("Unable to retrieve size from data service: " + e.getMessage());
        }

        return size;
    }


}
