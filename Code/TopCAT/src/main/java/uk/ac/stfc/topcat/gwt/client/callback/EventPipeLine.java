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
package uk.ac.stfc.topcat.gwt.client.callback;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.LoginInterface;
import uk.ac.stfc.topcat.gwt.client.LoginService;
import uk.ac.stfc.topcat.gwt.client.LoginServiceAsync;
import uk.ac.stfc.topcat.gwt.client.SearchService;
import uk.ac.stfc.topcat.gwt.client.SearchServiceAsync;
import uk.ac.stfc.topcat.gwt.client.TOPCATOnline;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.event.AddFacilityEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddInstrumentEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddMyDownloadEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddMyInvestigationEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginInfoPanelUpdateEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginInfoPanelUpdateEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.exception.WindowsNotAvailableExcecption;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.manager.TopcatWindowManager;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;
import uk.ac.stfc.topcat.gwt.client.model.Instrument;
import uk.ac.stfc.topcat.gwt.client.model.InvestigationType;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;
import uk.ac.stfc.topcat.gwt.client.widget.DatafileWindow;
import uk.ac.stfc.topcat.gwt.client.widget.DatasetWindow;
import uk.ac.stfc.topcat.gwt.client.widget.LoginPanel;
import uk.ac.stfc.topcat.gwt.client.widget.LoginWidget;
import uk.ac.stfc.topcat.gwt.client.widget.ParameterDownloadForm;
import uk.ac.stfc.topcat.gwt.client.widget.ParameterWindow;
import uk.ac.stfc.topcat.gwt.client.widget.WaitDialog;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * This is import class which holds all the events. currently doesn't directly
 * inherit from handler manager but a beginning to move in right direction.
 * 
 * This is a singleton and Most of the widget will call this instance to login,
 * search, download, history etc..
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class EventPipeLine implements LoginInterface {
    private final LoginServiceAsync loginService = GWT.create(LoginService.class);
    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
    private final SearchServiceAsync searchService = GWT.create(SearchService.class);
    ArrayList<TFacility> facilities;
    HashMap<String, ListStore<Instrument>> facilityInstrumentMap;
    ParameterDownloadForm paramDownloadForm;
    /**
     * Name of the facilities that have been loaded as a result of the user
     * logging on
     */
    private Set<String> loggedInFacilities = new HashSet<String>();
    private TopcatEvents tcEvents;
    /** Keep a track of outstanding call backs to check login status. */
    private int facilitiesToCheck = 0;
    /** When putting up a login panel prompt the user to log into this facility. */
    private String facilityToLogOn;
    private LoginWidget loginWidget;
    private WaitDialog retrievingDataDialog;
    private WaitDialog waitDialog;
    private int downloadCount = 0;
    private int retrievingDataDialogCount = 0;
    /** Items waiting to be down loaded. */
    private Set<DownloadModel> downloadQueue = new HashSet<DownloadModel>();

    // Panels from Main Window
    TOPCATOnline mainWindow = null;
    LoginPanel loginPanel = null;
    TopcatWindowManager tcWindowManager = null;
    HistoryManager historyManager = null;

    private static EventPipeLine eventPipeline = new EventPipeLine();
    private static EventBus eventBus = null;

    /**
     * Private constructor to make a singleton
     */
    private EventPipeLine() {
        eventBus = new SimpleEventBus();
        loginWidget = new LoginWidget();
        tcWindowManager = new TopcatWindowManager();
        historyManager = new HistoryManager(tcWindowManager);
        retrievingDataDialog = new WaitDialog();
        retrievingDataDialog.setMessage("  Retrieving data...");
        waitDialog = new WaitDialog();
        facilityInstrumentMap = new HashMap<String, ListStore<Instrument>>();
        facilities = new ArrayList<TFacility>();
        tcEvents = new TopcatEvents();
        facilityToLogOn = "";
        // Initialise
        loginWidget.setLoginHandler(this);

        // Create a new timer that calls getDownloadStatus() on the server.
        Timer t = getDownloadStatusTimer();
        // Schedule the timer to run every 5 seconds.
        t.scheduleRepeating(5000);

        // Setup handlers
        createLoginHandler();
        createLogoutHandler();
        createLoginInfoPanelUpdateHandler();
    }

    public static EventPipeLine getInstance() {
        return eventPipeline;
    }

    public static EventBus getEventBus() {
        return eventBus;
    }

    public void setMainWindow(TOPCATOnline topcatOnline) {
        mainWindow = topcatOnline;
    }

    public void initDownloadParameter() {
        paramDownloadForm = new ParameterDownloadForm();
        RootPanel.get().add(paramDownloadForm);
    }

    public void setFacilityToLogOn(String facilityToLogOn) {
        this.facilityToLogOn = facilityToLogOn;
        checkLoginWidgetStatus();
    }

    /**
     * @return history manager
     */
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    /**
     * @return the login panel
     */
    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    /**
     * Set the login info panel
     * 
     * @param loginPanel
     */
    public void setLoginPanel(LoginPanel loginPanel) {
        this.loginPanel = loginPanel;
    }

    /**
     * Get the set of facility names that the user is logged in to.
     * 
     * @return set of facility names
     */
    public Set<String> getLoggedInFacilities() {
        return loggedInFacilities;
    }

    /**
     * Callback on login dialog cancel button
     */
    @Override
    public void onLoginCancel() {
        loginWidget.hide();
    }

    /**
     * Callback on login dialog ok button.
     */
    @Override
    public void onLoginOk(final String facilityName, String username, String password) {
        loginWidget.hide();
        waitDialog.setMessage(" Logging In...");
        waitDialog.show();
        // login to the given facility using username and password
        loginService.login(username, password, facilityName, new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable caught) {
                // Show the RPC error message to the user
                waitDialog.hide();
                eventBus.fireEventFromSource(new LogoutEvent(facilityName), facilityName);
                failureLogin(loginWidget.getFacilityName());
            }

            @Override
            public void onSuccess(String result) {
                waitDialog.hide();
                getEventBus().fireEventFromSource(new LoginEvent(loginWidget.getFacilityName()),
                        loginWidget.getFacilityName());
            }
        });
    }

    /**
     * This method checks whether the user has logged into servers or not.
     */
    public void checkLoginStatus() {
        for (TFacility facility : facilities) {
            facilitiesToCheck = facilitiesToCheck + 1;
            loginService.isUserLoggedIn(facility.getName(), new LoginValidCallback(facility.getName(), true));
        }
    }

    /**
     * This method checks whether the user is still logged into the facilities
     * or not.
     */
    public void checkStillLoggedIn() {
        for (String facilityName : loggedInFacilities) {
            facilitiesToCheck = facilitiesToCheck + 1;
            loginService.isUserLoggedIn(facilityName, new LoginValidCallback(facilityName, false));
        }
    }

    /**
     * This method invokes the AJAX call to get the server logo
     */
    public void getLogoURL() {
        utilityService.getLogoURL(new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onSuccess(String result) {
                mainWindow.getHeaderPanel().setLogoURL(result);
            }
        });
    }

    /**
     * This method invokes the AJAX call to get the links for the footer
     */
    public void getLinks() {
        utilityService.getLinks(new AsyncCallback<Map<String, String>>() {

            @Override
            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onSuccess(Map<String, String> result) {
                mainWindow.getFooterPanel().setLinks(result);
            }
        });
    }

    /**
     * Display the login widget once all facilities have been checked.
     */
    public void checkLoginWidgetStatus() {
        // only continue once all of the login call backs have finished
        if (facilitiesToCheck > 0 || facilities.size() < 1) {
            return;
        }

        // Check if we are required to log into a specific facility
        if (!facilityToLogOn.isEmpty() && !loginPanel.getFacilityLoginInfoPanel(facilityToLogOn).isValidLogin()) {
            showLoginWidget(facilityToLogOn);
            facilityToLogOn = "";
            return;
        }

        // Check all login's whether at least one of them is logged in
        boolean loggedIn = false;
        for (TFacility facility : facilities) {
            if (loginPanel.getFacilityLoginInfoPanel(facility.getName()).isValidLogin()) {
                loggedIn = true;
                break;
            }
        }
        if (!loggedIn) {
            showLoginWidget(facilities.get(0).getName());
        }
    }

    /**
     * This method shows login dialog box for a input facility
     * 
     * @param facilityName
     */
    public void showLoginWidget(String facilityName) {
        if (!loginWidget.isVisible()) {
            loginWidget.setFacilityName(facilityName);
            loginWidget.show();
        }
    }

    /**
     * This method is invoked for the failure to login to facility service,
     * shows an error dialog to check the login details.
     * 
     * @param facilityName
     */
    private void failureLogin(String facilityName) {
        // Process the failure of login
        loginWidget.show();
        // Show an error message.
        showErrorDialog("Error logging in.  Please check username and password");
    }

    /**
     * This method logs out user from a input facility
     * 
     * @param facilityName
     */
    public void facilityLogout(final String facilityName) {
        waitDialog.setMessage(" Logging Out...");
        waitDialog.show();
        loginWidget.setFacilityName(facilityName);

        // logout of the given facility
        loginService.logout(facilityName, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                // Show the RPC error message to the user
                waitDialog.hide();
            }

            @Override
            public void onSuccess(Void result) {
                waitDialog.hide();
                eventBus.fireEventFromSource(new LogoutEvent(facilityName), facilityName);
            }
        });
    }

    /**
     * This method loads available facility from TopCAT service
     */
    public void loadFacilityNames() {
        utilityService.getFacilities(new AsyncCallback<ArrayList<TFacility>>() {
            @Override
            public void onFailure(Throwable caught) {
                showErrorDialog("Error while downloading facility names");
            }

            @Override
            public void onSuccess(ArrayList<TFacility> result) {
                facilities = result;
                getEventBus().fireEvent(new AddFacilityEvent(facilities));
            }
        });
    }

    /**
     * This method gets all the instrument details for input facility
     * 
     * @param facility
     * @return
     */
    public ListStore<Instrument> getFacilityInstruments(String facility) {
        ListStore<Instrument> fInstruments = facilityInstrumentMap.get(facility);
        if (fInstruments == null) { // New Facility
            fInstruments = new ListStore<Instrument>();
            facilityInstrumentMap.put(facility, fInstruments);
        }
        return fInstruments;
    }

    /**
     * This method searches for all the investigations that matches given search
     * details
     * 
     * @param searchDetails
     */
    public void searchForInvestigation(TAdvancedSearchDetails searchDetails) {
        waitDialog.setMessage("  Searching...");
        waitDialog.show();
        searchService.getAdvancedSearchResultsInvestigation(null, searchDetails,
                new AsyncCallback<List<TInvestigation>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        waitDialog.hide();
                        showErrorDialog("Error retrieving data from server");
                    }

                    @Override
                    public void onSuccess(List<TInvestigation> result) {
                        ArrayList<TopcatInvestigation> invList = new ArrayList<TopcatInvestigation>();
                        if (result != null) {
                            for (TInvestigation inv : result)
                                invList.add(new TopcatInvestigation(inv.getServerName(), inv.getInvestigationId(), inv
                                        .getInvestigationName(), inv.getTitle(), inv.getVisitId(), inv.getStartDate(),
                                        inv.getEndDate()));
                        }
                        waitDialog.hide();
                        mainWindow.getMainPanel().getSearchPanel().setInvestigations(invList);
                    }
                });
    }

    /**
     * This method searches for the user investigations that matches given
     * search details
     * 
     * @param searchDetails
     */
    public void searchForMyInvestigation(TAdvancedSearchDetails searchDetails) {
        waitDialog.setMessage("  Searching...");
        waitDialog.show();
        searchService.getSearchResultsMyInvestigationFromKeywords(null, searchDetails.getKeywords(),
                new AsyncCallback<List<TInvestigation>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        waitDialog.hide();
                        showErrorDialog("Error retrieving data from server");
                    }

                    @Override
                    public void onSuccess(List<TInvestigation> result) {
                        ArrayList<TopcatInvestigation> invList = new ArrayList<TopcatInvestigation>();
                        if (result != null) {
                            for (TInvestigation inv : result)
                                invList.add(new TopcatInvestigation(inv.getServerName(), inv.getInvestigationId(), inv
                                        .getInvestigationName(), inv.getTitle(), inv.getVisitId(), inv.getStartDate(),
                                        inv.getEndDate()));
                        }
                        waitDialog.hide();
                        mainWindow.getMainPanel().getSearchPanel().setInvestigations(invList);
                    }
                });
    }

    /**
     * This method searches for the user investigations that belongs to user
     */
    public void getMyInvestigationsInMyDataPanel() {
        waitDialog.setMessage("Getting Investigations...");
        waitDialog.show();
        // NOTE: Working without this
        for (TFacility facility : facilities) {
            addMyInvestigations(facility.getName());
        }

    }

    /**
     * Show an alert Dialog box
     * 
     * @param msg
     *            message in the dialog box
     */
    public void showErrorDialog(String msg) {
        MessageBox.alert("Error", msg, null);
    }

    /**
     * Show an info Dialog box
     * 
     * @param msg
     *            message in the dialog box
     */
    public void showMessageDialog(String msg) {
        MessageBox.info("Information", msg, null);
    }

    /**
     * Download all the datafiles from the facility for the given ids.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datafileList
     *            a list containing data file ids
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
                        mainWindow.getMainPanel().getMyDownloadPanel().addDownload(result);
                        if (result.getStatus().equalsIgnoreCase(Constants.STATUS_IN_PROGRESS)) {
                            downloadQueue.add(result);
                        } else {
                            download(facilityName, result.getUrl());
                        }
                    }
                });
    }

    /**
     * Download the dataset from the facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            the data set id
     * @param downloadName
     *            a string containing a user defined name
     */
    public void downloadDatasets(final String facilityName, final Long datasetId, final String downloadName) {
        utilityService.getDatasetDownloadURL(facilityName, datasetId, downloadName, new AsyncCallback<DownloadModel>() {
            @Override
            public void onFailure(Throwable caught) {
                showErrorDialog("Error retrieving data from server");
            }

            @Override
            public void onSuccess(DownloadModel result) {
                mainWindow.getMainPanel().getMyDownloadPanel().addDownload(result);
                if (result.getStatus().equalsIgnoreCase(Constants.STATUS_IN_PROGRESS)) {
                    downloadQueue.add(result);
                } else {
                    download(facilityName, result.getUrl());
                }
            }
        });
    }

    /**
     * Download the url in a separate window.
     * 
     * @param url
     */
    public void download(String facilityName, final String url) {
        if (!loggedInFacilities.contains(facilityName)) {
            // session logged out so do not download
            return;
        }
        DOM.setElementAttribute(RootPanel.get("__download" + downloadCount).getElement(), "src", url);
        downloadCount = downloadCount + 1;
        if (downloadCount > (Constants.MAX_DOWNLOAD_FRAMES - 1)) {
            downloadCount = 0;
        }
    }

    /**
     * AJAX call to search of datafiles for the input search details in the
     * given facility
     * 
     * @param facilityName
     *            facility name
     * @param searchDetails
     *            search details
     */
    public void searchForDatafiles(String facilityName, TAdvancedSearchDetails searchDetails) {
        waitDialog.setMessage("  Searching...");
        waitDialog.show();
        searchService.getAdvancedSearchResultsDatafile(null, facilityName, searchDetails,
                new AsyncCallback<ArrayList<DatafileModel>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        waitDialog.hide();
                        showErrorDialog("Error retrieving data from server");
                    }

                    @Override
                    // On success opens a datafile window to show the results
                    public void onSuccess(ArrayList<DatafileModel> result) {
                        waitDialog.hide();
                        try {
                            DatafileWindow datafileWindow = tcWindowManager.createDatafileWindow();
                            datafileWindow.setDatafileList(result);
                            datafileWindow.show();
                            datafileWindow.setHistoryVerified(true);
                        } catch (WindowsNotAvailableExcecption e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * This method will show the dataset window for the given facility name,
     * investigation id
     * 
     * @param facilityName
     *            facility name
     * @param investigationId
     *            investigation id
     * @param investigationName
     *            investigation name
     */
    public void showDatasetWindow(String facilityName, String investigationId, String investigationName) {
        try {
            DatasetWindow datasetWindow = tcWindowManager.createDatasetWindow();
            datasetWindow.setInvestigationTitle(investigationName);
            datasetWindow.setDataset(facilityName, investigationId);
            datasetWindow.show();
            datasetWindow.setHistoryVerified(true);
        } catch (WindowsNotAvailableExcecption e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method will show dataset window for the given input facility name,
     * investigation id and also updates the browser history
     * 
     * @param facilityName
     *            facility name
     * @param investigationId
     *            investigation id
     * @param investigationName
     *            investigation name
     */
    public void showDatasetWindowWithHistory(String facilityName, String investigationId, String investigationName) {
        showDatasetWindow(facilityName, investigationId, investigationName);
        historyManager.updateHistory();
    }

    /**
     * This method will show the datafile window for the given input dataset
     * models.
     * 
     * @param datasetModel
     *            list of dataset models
     */
    public void showDatafileWindow(ArrayList<DatasetModel> datasetModel) {
        try {
            DatafileWindow datafileWindow = tcWindowManager.createDatafileWindow();
            datafileWindow.setDatasets(datasetModel);
            datafileWindow.show();
            datafileWindow.setHistoryVerified(true);
        } catch (WindowsNotAvailableExcecption e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method will show the datafile window and also update the browser
     * history.
     * 
     * @param datasetModel
     *            list of dataset models
     */
    public void showDatafileWindowWithHistory(ArrayList<DatasetModel> datasetModel) {
        showDatafileWindow(datasetModel);
        historyManager.updateHistory();
    }

    /**
     * This method will show the parameter window for the given inputs of
     * facility name, datafile id
     * 
     * @param facilityName
     *            facility name
     * @param datafileId
     *            datafile id
     * @param datafileName
     *            datafile name
     */
    public void showParameterWindow(String facilityName, String datafileId, String datafileName) {
        try {
            ParameterWindow paramWindow = tcWindowManager.createParameterWindow();
            paramWindow.setDatafileName(datafileName);
            paramWindow.setDatafileInfo(facilityName, datafileId);
            paramWindow.show();
            paramWindow.setHistoryVerified(true);
        } catch (WindowsNotAvailableExcecption e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This method will call show parameter window and also update the browser
     * history.
     * 
     * @param facilityName
     *            facility name
     * @param datafileId
     *            datafile id
     * @param datafileName
     *            datafile name
     */
    public void showParameterWindowWithHistory(String facilityName, String datafileId, String datafileName) {
        showParameterWindow(facilityName, datafileId, datafileName);
        historyManager.updateHistory();
    }

    /**
     * This method will take the input facility name and datafile id and
     * downloads the parameter files in CSV format.
     * 
     * @param facilityName
     *            facility name
     * @param datafileId
     *            datafile id
     */
    public void downloadParametersData(String facilityName, String datafileId) {
        // set the invisible form for parameter download
        // RootPanel.get("__downloadParameterForm").add(paramDownloadForm);
        // construct servlet request to call copydatatocsvfile servlet
        paramDownloadForm.setFacilityName(facilityName);
        paramDownloadForm.setDatafileId(datafileId);
        paramDownloadForm.submit();
        // RootPanel.get("__downloadParameterForm").remove(paramDownloadForm);
    }

    /**
     * @return the main window
     */
    public TOPCATOnline getMainWindow() {
        return mainWindow;
    }

    /**
     * Show the dialog box.
     */
    public void showRetrievingData() {
        if (retrievingDataDialogCount == 0) {
            retrievingDataDialog.show();
        }
        retrievingDataDialogCount = retrievingDataDialogCount + 1;
    }

    /**
     * Hide the dialog box.
     */
    public void hideRetrievingData() {
        retrievingDataDialogCount = retrievingDataDialogCount - 1;
        if (retrievingDataDialogCount < 1) {
            retrievingDataDialog.hide();
            retrievingDataDialogCount = 0;
        }
    }

    public ArrayList<TFacility> getFacilityNames() {
        return facilities;
    }

    /**
     * Get the topcat events object. Use this to listen for and fire events.
     * 
     * @return the topcat events object
     */
    public TopcatEvents getTcEvents() {
        return tcEvents;
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
                    if (!loggedInFacilities.contains(it.next().getFacilityName())) {
                        it.remove();
                    }
                }
                if (downloadQueue.size() == 0) {
                    // nothing to check
                    return;
                }

                // call the server which will call the download service
                utilityService.getDownloadStatus(downloadQueue, new AsyncCallback<List<DownloadModel>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        // remove items if we have logged out of the facility
                        for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                            if (!loggedInFacilities.contains(it.next().getFacilityName())) {
                                it.remove();
                            }
                        }
                        if (downloadQueue.size() > 0) {
                            showErrorDialog("Error retrieving data from server while trying to get download status");
                        }
                    }

                    @Override
                    public void onSuccess(List<DownloadModel> finalStateReached) {
                        boolean refresh = false;
                        for (DownloadModel download : finalStateReached) {
                            // remove done item from the list
                            downloadQueue.remove(download);
                            if (!loggedInFacilities.contains(download.getFacilityName())) {
                                // session logged out so do not process this
                                // download
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
                            mainWindow.getMainPanel().getMyDownloadPanel().refresh();
                        }
                    }
                });
            }
        };
        return t;
    }

    /**
     * Get the investigations for the facility belonging to the user.
     * 
     * @param facilityName
     */
    public void addMyInvestigations(final String facilityName) {
        showRetrievingData();
        utilityService.getMyInvestigationsInServer(facilityName, new AsyncCallback<ArrayList<TInvestigation>>() {
            @Override
            public void onFailure(Throwable caught) {
                hideRetrievingData();
                showErrorDialog("Error retrieving investigation data from server for " + facilityName);
            }

            @Override
            public void onSuccess(ArrayList<TInvestigation> result) {
                ArrayList<TopcatInvestigation> invList = new ArrayList<TopcatInvestigation>();
                if (result != null) {
                    for (TInvestigation inv : result)
                        invList.add(new TopcatInvestigation(inv.getServerName(), inv.getInvestigationId(), inv
                                .getInvestigationName(), inv.getTitle(), inv.getVisitId(), inv.getStartDate(), inv
                                .getEndDate()));
                }
                getEventBus().fireEventFromSource(new AddMyInvestigationEvent(facilityName, invList), facilityName);
                hideRetrievingData();
            }
        });
    }

    /**
     * Get the download names for the facility belonging to the user.
     * 
     * @param facilityName
     */
    private void addMyDownloads(final String facilityName) {
        showRetrievingData();
        Set<String> facilityNames = new HashSet<String>();
        facilityNames.add(facilityName);
        utilityService.getMyDownloadList(facilityNames, new AsyncCallback<ArrayList<DownloadModel>>() {
            @Override
            public void onFailure(Throwable caught) {
                hideRetrievingData();
                showMessageDialog("Error retrieving download history from server for " + facilityName);
            }

            @Override
            public void onSuccess(ArrayList<DownloadModel> result) {
                getEventBus().fireEventFromSource(new AddMyDownloadEvent(facilityName, result), facilityName);
                hideRetrievingData();
                // Check for downloads that are 'in progress' as we need to add
                // them to the downlod queue
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
     * Get the instrument names for the facility .
     * 
     * @param facilityName
     */
    private void addInstruments(final String facilityName) {
        showRetrievingData();
        utilityService.getInstrumentNames(facilityName, new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                hideRetrievingData();
                showMessageDialog("Error retrieving instrument names from server for " + facilityName);
            }

            @Override
            public void onSuccess(ArrayList<String> instrumentList) {
                ArrayList<Instrument> instruments = new ArrayList<Instrument>();
                for (String instrument : instrumentList) {
                    instruments.add(new Instrument(facilityName, instrument));
                }
                getEventBus().fireEventFromSource(new AddInstrumentEvent(facilityName, instruments), facilityName);
                hideRetrievingData();
                // update list store
                ListStore<Instrument> fInstruments = getFacilityInstruments(facilityName);
                fInstruments.removeAll();
                fInstruments.add(instruments);
            }
        });
    }

    /**
     * Get the investigations names for the facility.
     * 
     * @param facilityName
     */
    private void addInvestigations(final String facilityName) {
        showRetrievingData();
        utilityService.getInvestigationTypes(facilityName, new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                hideRetrievingData();
                showMessageDialog("Error retrieving investigation names from server for " + facilityName);
            }

            @Override
            public void onSuccess(ArrayList<String> investigationTypeList) {
                ArrayList<InvestigationType> investigations = new ArrayList<InvestigationType>();
                for (String investigationType : investigationTypeList) {
                    investigations.add(new InvestigationType(facilityName, investigationType));
                }
                getEventBus()
                        .fireEventFromSource(new AddInvestigationEvent(facilityName, investigations), facilityName);
                hideRetrievingData();
            }
        });
    }

    /**
     * Setup a handler to react to login events and get the investigations for
     * the facility.
     */
    private void createLoginHandler() {
        // react to user logging into a facility
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(final LoginEvent event) {
                loggedInFacilities.add(event.getFacilityName());
                addMyInvestigations(event.getFacilityName());
                addMyDownloads(event.getFacilityName());
                addInstruments(event.getFacilityName());
                addInvestigations(event.getFacilityName());
//                historyManager.updateHistory(); //TODO
                historyManager.processHistory(historyManager.getHistoryString());
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
                ListStore<Instrument> fInstruments = getFacilityInstruments(event.getFacilityName());
                fInstruments.removeAll();
                loggedInFacilities.remove(event.getFacilityName());

                // remove items from the download queue
                for (Iterator<DownloadModel> it = downloadQueue.iterator(); it.hasNext();) {
                    if (it.next().getFacilityName().equals(event.getFacilityName())) {
                        it.remove();
                    }
                }
                // Check if we are required to log into this facility
                if (facilityToLogOn.equals(event.getFacilityName())) {
                    showLoginWidget(facilityToLogOn);
                    facilityToLogOn = "";
                }
            }
        });
    }

    /**
     * Setup a handler to react to logout events for the facility.
     */
    private void createLoginInfoPanelUpdateHandler() {
        // react to an update of the LoginInfoPanel
        LoginInfoPanelUpdateEvent.register(EventPipeLine.getEventBus(), new LoginInfoPanelUpdateEventHandler() {
            @Override
            public void update(final LoginInfoPanelUpdateEvent event) {
                facilitiesToCheck = facilitiesToCheck - 1;
                checkLoginWidgetStatus();
            }
        });
    }
}