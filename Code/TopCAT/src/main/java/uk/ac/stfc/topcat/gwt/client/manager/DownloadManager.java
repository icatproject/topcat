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
package uk.ac.stfc.topcat.gwt.client.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;
import com.google.gwt.user.client.ui.RootPanel;

import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddMyDownloadEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;

public class DownloadManager {
    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
    /** Items waiting to be down loaded. */
    private Set<DownloadModel> downloadQueue = new HashSet<DownloadModel>();
    private int downloadCount = 0;
    private boolean statusErrorVissable = false;
    EventPipeLine eventPipeLine = EventPipeLine.getInstance();

    /** An instance of this class. */
    private static DownloadManager downloadManager = new DownloadManager();

    /**
     * Private constructor to make a singleton.
     */
    private DownloadManager() {
        // Create a new timer that calls getDownloadStatus() on the server.
        Timer t = getDownloadStatusTimer();
        // Schedule the timer to run every 5 seconds.
        t.scheduleRepeating(5000);

        createLoginHandler();
        createLogoutHandler();
    }

    /**
     * Get the DownloadManager.
     * 
     * @return an instance of the DownloadManager
     */
    public static DownloadManager getInstance() {
        return downloadManager;
    }

    /**
     * Down load all the data files from the facility for the given ids.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datafileList
     *            a list containing data file ids
     * @param downloadName
     *            a string containing a user defined name to give the down load
     *            file
     */
    public void downloadDatafiles(final String facilityName, final List<Long> datafileList, final String downloadName) {
        utilityService.getDatafilesDownloadURL(facilityName, (ArrayList<Long>) datafileList, downloadName,
                new AsyncCallback<DownloadModel>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        showErrorDialog("Error retrieving data from server");
                    }

                    @Override
                    public void onSuccess(DownloadModel result) {
                        eventPipeLine.getMainWindow().getMainPanel().getMyDownloadPanel().addDownload(result);
                        if (result.getStatus().equalsIgnoreCase(Constants.STATUS_IN_PROGRESS)) {
                            downloadQueue.add(result);
                        } else {
                            download(facilityName, result.getUrl());
                        }
                    }
                });
    }

    /**
     * Down load the data set from the facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            the data set id
     * @param downloadName
     *            a string containing a user defined name to give the down load
     *            file
     */
    public void downloadDataset(final String facilityName, final Long datasetId, final String downloadName) {
        utilityService.getDatasetDownloadURL(facilityName, datasetId, downloadName, new AsyncCallback<DownloadModel>() {
            @Override
            public void onFailure(Throwable caught) {
                showErrorDialog("Error retrieving data from server");
            }

            @Override
            public void onSuccess(DownloadModel result) {
                eventPipeLine.getMainWindow().getMainPanel().getMyDownloadPanel().addDownload(result);
                if (result.getStatus().equalsIgnoreCase(Constants.STATUS_IN_PROGRESS)) {
                    downloadQueue.add(result);
                } else {
                    download(facilityName, result.getUrl());
                }
            }
        });
    }

    /**
     * Initiate a down load in a separate window.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param url
     *            a string containing the url to contact
     */
    public void download(String facilityName, final String url) {
        if (!eventPipeLine.getLoggedInFacilities().contains(facilityName)) {
            // session logged out so do not down load
            return;
        }
        DOM.setElementAttribute(RootPanel.get("__download" + downloadCount).getElement(), "src", url);
        downloadCount = downloadCount + 1;
        if (downloadCount > (Constants.MAX_DOWNLOAD_FRAMES - 1)) {
            downloadCount = 0;
        }
    }

    /**
     * Create a new timer that calls getDownloadStatus() on the server.
     * 
     * @return a timer that call getDownloadStatus on the server
     */
    private Timer getDownloadStatusTimer() {
        Timer t = new Timer() {
            @Override
            public void run() {
                // remove items if we have logged out of the facility
                for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                    if (!eventPipeLine.getLoggedInFacilities().contains(it.next().getFacilityName())) {
                        it.remove();
                    }
                }
                if (downloadQueue.size() == 0) {
                    // nothing to check
                    return;
                }

                // call the server which will call the down load service
                utilityService.getDownloadStatus(downloadQueue, new AsyncCallback<List<DownloadModel>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof StatusCodeException) {
                            // There were problems communicating with the
                            // server, 200 was not returned. As this is on a
                            // timer we do not want to keep on spamming the user
                            // with error messages every n seconds.
                            return;
                        }

                        // remove items if we have logged out of the facility
                        for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                            if (!eventPipeLine.getLoggedInFacilities().contains(it.next().getFacilityName())) {
                                it.remove();
                            }
                        }
                        if (downloadQueue.size() > 0) {
                            // Only show error message if a message is not
                            // already being displayed
                            if (statusErrorVissable != true) {
                                statusErrorVissable = true;
                                String msg = "Error retrieving data from server while trying to get download status: "
                                        + caught.toString();
                                MessageBox.alert("Error", msg, new Listener<MessageBoxEvent>() {
                                    public void handleEvent(MessageBoxEvent be) {
                                        statusErrorVissable = false;
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onSuccess(List<DownloadModel> finalStateReached) {
                        boolean refresh = false;
                        for (DownloadModel download : finalStateReached) {
                            // remove done item from the list
                            downloadQueue.remove(download);
                            if (!eventPipeLine.getLoggedInFacilities().contains(download.getFacilityName())) {
                                // session logged out so do not process this
                                // down load
                                continue;
                            }
                            if (download.getStatus().equalsIgnoreCase(Constants.STATUS_AVAILABLE)) {
                                download(download.getFacilityName(), download.getUrl());
                            } else if (download.getStatus().equalsIgnoreCase(Constants.STATUS_ERROR)) {
                                showErrorDialog("Error retrieving data from server for download "
                                        + download.getDownloadName());
                            }
                            refresh = true;
                        }
                        if (refresh) {
                            eventPipeLine.getMainWindow().getMainPanel().getMyDownloadPanel().refresh();
                        }
                    }
                });
            }
        };
        return t;
    }

    /**
     * Show an alert dialog box.
     * 
     * @param msg
     *            message in the dialog box
     */
    private void showErrorDialog(String msg) {
        MessageBox.alert("Error", msg, null);
    }

    /**
     * Get the list of down loads belonging to the user for the given facility.
     * If any are in a state of in progress, add them to the downloadQueue.
     * 
     * @param facilityName
     *            a string containing the facility name
     */
    private void getMyDownloadList(final String facilityName) {
        eventPipeLine.showRetrievingData();
        Set<String> facilityNames = new HashSet<String>();
        facilityNames.add(facilityName);
        utilityService.getMyDownloadList(facilityNames, new AsyncCallback<ArrayList<DownloadModel>>() {
            @Override
            public void onFailure(Throwable caught) {
                eventPipeLine.hideRetrievingData();
                showErrorDialog("Error retrieving download history from server for " + facilityName);
            }

            @Override
            public void onSuccess(ArrayList<DownloadModel> result) {
                EventPipeLine.getEventBus().fireEventFromSource(new AddMyDownloadEvent(facilityName, result),
                        facilityName);
                eventPipeLine.hideRetrievingData();
                // Check for down loads that are 'in progress' as we need to add
                // them to the down load queue
                for (Iterator<DownloadModel> it = result.iterator(); it.hasNext();) {
                    if (!it.next().getStatus().equals(Constants.STATUS_IN_PROGRESS)) {
                        it.remove();
                    }
                }
                if (result.size() > 0) {
                    downloadQueue.addAll(result);
                }
            }
        });
    }

    /**
     * Setup a handler to react to login events for the facility.
     */
    private void createLoginHandler() {
        // react to user logging into a facility
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(final LoginEvent event) {
                getMyDownloadList(event.getFacilityName());
            }
        });
    }

    /**
     * Setup a handler to react to logout events for the facility.
     */
    private void createLogoutHandler() {
        // react to user logging out of a facility
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(final LogoutEvent event) {
                // remove items from the down load queue
                for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                    if (it.next().getFacilityName().equals(event.getFacilityName())) {
                        it.remove();
                    }
                }
            }
        });
    }

}
