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
import uk.ac.stfc.topcat.core.gwt.module.exception.BadParameterException;
import uk.ac.stfc.topcat.core.gwt.module.exception.InsufficientPrivilegesException;
import uk.ac.stfc.topcat.core.gwt.module.exception.InternalException;
import uk.ac.stfc.topcat.core.gwt.module.exception.NotSupportedException;
import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.LoginInterface;
import uk.ac.stfc.topcat.gwt.client.LoginService;
import uk.ac.stfc.topcat.gwt.client.LoginServiceAsync;
import uk.ac.stfc.topcat.gwt.client.SearchService;
import uk.ac.stfc.topcat.gwt.client.SearchServiceAsync;
import uk.ac.stfc.topcat.gwt.client.SoftwareRepoService;
import uk.ac.stfc.topcat.gwt.client.SoftwareRepoServiceAsync;
import uk.ac.stfc.topcat.gwt.client.TOPCATOnline;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.event.AddFacilityEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddInstrumentEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationDetailsEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationTypeEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddMyInvestigationEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginCheckCompleteEvent;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.event.WindowLogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginCheckCompleteEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.WindowLogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.exception.WindowsNotAvailableExcecption;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.manager.TopcatWindowManager;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
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
    private final SoftwareRepoServiceAsync softwareRepoService = GWT.create(SoftwareRepoService.class);
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
    private Set<String> facilitiesToLogOn;
    private LoginWidget loginWidget;
    private WaitDialog retrievingDataDialog;
    private WaitDialog waitDialog;
    private int retrievingDataDialogCount = 0;

    // Panels from Main Window
    TOPCATOnline mainWindow = null;
    LoginPanel loginPanel = null;
    TopcatWindowManager tcWindowManager = null;
    HistoryManager historyManager = null;

    private static EventPipeLine eventPipeline = new EventPipeLine();
    private static EventBus eventBus = null;

    /**
     * Private constructor to make a singleton.
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
        facilitiesToLogOn = new HashSet<String>();
        // Initialise
        loginWidget.setLoginHandler(this);

        // Setup handlers
        createLoginHandler();
        createLogoutHandler();
        createLoginCheckCompleteHandler();
        createLogoutWindowHandler();
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

    public SoftwareRepoServiceAsync getSoftwareRepoService() {
        return softwareRepoService;
    }

    public void initDownloadParameter() {
        paramDownloadForm = new ParameterDownloadForm();
        RootPanel.get().add(paramDownloadForm);
    }

    /**
     * Set the list of facilities that we need to log into in order to view the
     * requested data.
     * 
     * @param facilitiesToLogOn
     *            a set of facility names
     */
    public void setFacilitiesToLogOn(Set<String> facilitiesToLogOn) {
        this.facilitiesToLogOn.addAll(facilitiesToLogOn);
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
     * Set the login info panel.
     * 
     * @param loginPanel
     */
    public void setLoginPanel(LoginPanel loginPanel) {
        this.loginPanel = loginPanel;
    }

    /**
     * Get the set of facility names that the user is logged into.
     * 
     * @return set of facility names
     */
    public Set<String> getLoggedInFacilities() {
        return loggedInFacilities;
    }

    /**
     * Callback on login dialog cancel button.
     */
    @Override
    public void onLoginCancel() {
        loginWidget.hide();
    }

    /**
     * Callback on login dialog ok button.
     */
    @Override
    public void onLoginOk(final String facilityName, String authenticationType, Map<String, String> paramerters) {
        loginWidget.hide();
        waitDialog.setMessage(" Logging In...");
        waitDialog.show();
        // Login to the given facility using username and password
        loginService.login(paramerters, authenticationType, facilityName, new AsyncCallback<String>() {
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
     * This method checks whether the user is already logged into any
     * facilities. If the user is logged in then a login event for that facility
     * will be fired.
     */
    public void checkInitialLoginStatus() {
        for (TFacility facility : facilities) {
            // Keep a track of the number of outstanding callbacks
            facilitiesToCheck = facilitiesToCheck + 1;
            loginService.isUserLoggedIn(facility.getName(), new LoginValidCallback(facility.getName(), true));
        }
    }

    /**
     * This method checks whether the user is still logged into the facilities.
     */
    public void checkStillLoggedIn() {
        for (String facilityName : loggedInFacilities) {
            // Keep a track of the number of outstanding callbacks
            facilitiesToCheck = facilitiesToCheck + 1;
            loginService.isUserLoggedIn(facilityName, new LoginValidCallback(facilityName, false));
        }
    }

    public String encodeUrlDelimiters(String s) {
        if (s == null) {
            return null;
        }
        s = s.replaceAll(";", "%2F");
        s = s.replaceAll("/", "%2F");
        s = s.replaceAll(":", "%3A");
        s = s.replaceAll("\\?", "%3F");
        s = s.replaceAll("&", "%26");
        s = s.replaceAll("\\=", "%3D");
        s = s.replaceAll("\\+", "%2B");
        s = s.replaceAll("\\$", "%24");
        s = s.replaceAll(",", "%2C");
        s = s.replaceAll("#", "%23");
        return s;
    }

    /**
     * This method invokes the AJAX call to get the information from the
     * topcat.properties file. This is used to set data in the header and the
     * footer.
     */
    public void getTopcatProperties() {
        utilityService.getTopcatProperties(new AsyncCallback<Map<String, String>>() {
            @Override
            public void onFailure(Throwable caught) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onSuccess(Map<String, String> result) {
                mainWindow.getHeaderPanel().setLogoURL(result.get(Constants.LOGO_URL));
                mainWindow.getHeaderPanel().setMessage(result.get(Constants.MESSAGE));
                mainWindow.getFooterPanel().setLinks(result);
            }
        });
    }

    /**
     * Check if the login widget needs to be displayed. Only display the login
     * widget once all facilities have been checked and a login is still
     * required.
     */
    public void checkLoginWidgetStatus() {
        // Only continue once all of the login call backs have finished
        if (facilitiesToCheck > 0 || facilities.size() < 1) {
            return;
        }

        // Check if we are required to log into a specific facility
        for (Iterator<String> it = facilitiesToLogOn.iterator(); it.hasNext();) {
            String facility = it.next();
            if (!loginPanel.getFacilityLoginInfoPanel(facility).isValidLogin()) {
                it.remove();
                showLoginWidget(facility);
                return;
            }
        }

        // Check all login's to see whether at least one of them is logged in
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
     * This method shows login dialog box for the given facility.
     * 
     * @param facilityName
     */
    public void showLoginWidget(String facilityName) {
        if (!loginWidget.isVisible()) {
            loginWidget.show(getFacility(facilityName));
        }
    }

    /**
     * Show an error dialog asking to check the login details for the given
     * facility.
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
     * This method logs out the user from the given facility.
     * 
     * @param facilityName
     */
    public void facilityLogout(final String facilityName) {
        waitDialog.setMessage(" Logging Out...");
        waitDialog.show();
        eventBus.fireEventFromSource(new LogoutEvent(facilityName), facilityName);

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
            }
        });
    }

    /**
     * This method loads available facilities from the TopCAT service.
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
     * This method gets all the instrument details for input facility.
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
     * This method searches for all the investigations that match the given
     * search details.
     * 
     * @param searchDetails
     */
    public void searchForInvestigation(final TAdvancedSearchDetails searchDetails) {
        waitDialog.setMessage("  Searching...");
        waitDialog.show();
        searchService.getAdvancedSearchResultsInvestigation(null, searchDetails,
                new AsyncCallback<List<TInvestigation>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        waitDialog.hide();
                        if (caught instanceof SessionException) {
                            checkStillLoggedIn();
                        } else if (caught instanceof BadParameterException) {
                            showErrorDialog("Error " + ((BadParameterException) caught).getMessage());
                        } else if (caught instanceof NotSupportedException) {
                            showErrorDialog("Error " + ((NotSupportedException) caught).getMessage());
                        } else {
                            showErrorDialog("Error retrieving data from server");
                        }
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
     * Get additional details about an investigation. An asynchronous call is
     * made to the server and an <code>AddInvestigationDetailsEvent</code> is
     * fired when the results have been returned.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param investigationId
     *            the investigation id
     * @param sourcePanel
     *            a string containing the name of the calling source panel
     */
    public void getInvestigationDetails(final String facilityName, final String investigationId,
            final String sourcePanel) {
        utilityService.getInvestigationDetails(facilityName, investigationId, new AsyncCallback<TInvestigation>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof SessionException) {
                    // session has probably expired, check all sessions to be
                    // safe
                    checkStillLoggedIn();
                } else {
                    showErrorDialog("Error retrieving data from server for investigation " + investigationId);
                }
            }

            @Override
            public void onSuccess(TInvestigation result) {
                getEventBus().fireEventFromSource(new AddInvestigationDetailsEvent(facilityName, result), sourcePanel);
            }
        });
    }

    /**
     * This method searches for the user investigations that match the given
     * search details.
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
                        if (caught instanceof SessionException) {
                            checkStillLoggedIn();
                        } else {
                            showErrorDialog("Error retrieving data from server");
                        }
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
     * This method searches for the user investigations that belongs to user.
     */
    public void getMyInvestigationsInMyDataPanel() {
        // NOTE: Working without this
        for (TFacility facility : facilities) {
            addMyInvestigations(facility.getName());
        }

    }

    /**
     * Show an alert dialog box.
     * 
     * @param msg
     *            message in the dialog box
     */
    public void showErrorDialog(String msg) {
        MessageBox.alert("Error", msg, null);
    }

    /**
     * Show an info dialog box.
     * 
     * @param msg
     *            message in the dialog box
     */
    public void showMessageDialog(String msg) {
        MessageBox.info("Information", msg, null);
    }

    /**
     * AJAX call to search of datafiles for the input search details in the
     * given facility.
     * 
     * @param facilityName
     *            facility name
     * @param searchDetails
     *            search details
     */
    public void searchForDatafiles(final String facilityName, TAdvancedSearchDetails searchDetails) {
        waitDialog.setMessage("  Searching...");
        waitDialog.show();
        searchService.getAdvancedSearchResultsDatafile(null, facilityName, searchDetails,
                new AsyncCallback<ArrayList<DatafileModel>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        waitDialog.hide();
                        if (caught instanceof SessionException) {
                            checkStillLoggedIn();
                        } else if (caught instanceof BadParameterException) {
                            showErrorDialog("Error from " + facilityName + ". "
                                    + ((BadParameterException) caught).getMessage());
                        } else if (caught instanceof NotSupportedException) {
                            showErrorDialog("Error " + ((NotSupportedException) caught).getMessage());
                        } else {
                            showErrorDialog("Error retrieving data from " + facilityName);
                        }
                    }

                    @Override
                    // On success opens a datafile window to show the results
                    public void onSuccess(ArrayList<DatafileModel> result) {
                        waitDialog.hide();
                        try {
                            if (result.size() > 0) {
                                DatafileWindow datafileWindow = tcWindowManager.createDatafileWindow();
                                datafileWindow.setAdvancedSearchResult(facilityName, result);
                                datafileWindow.show();
                                datafileWindow.setHistoryVerified(true);
                            } else {
                                showMessageDialog("No files returned from " + facilityName);
                            }
                        } catch (WindowsNotAvailableExcecption e) {
                            showErrorDialog(e.getMessage());
                        }
                    }
                });
    }

    /**
     * This method searches for all the datafiles that match the given
     * searchDetails.
     * 
     * @param searchDetails
     */
    public void searchForDatafilesByParameter(TAdvancedSearchDetails searchDetails) {
        for (final String facilityName : searchDetails.getFacilityList()) {
            searchForDatafiles(facilityName, searchDetails);
        }
    }

    /**
     * This method will show the dataset window for the given facility name and
     * investigation id.
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
            DatasetWindow datasetWindow = tcWindowManager.findDatasetWindow(facilityName, investigationId);
            if (datasetWindow == null) {
                datasetWindow = tcWindowManager.createDatasetWindow();
                if (investigationName == null) {
                    setInvestigationTitle(facilityName, investigationId, datasetWindow);
                } else {
                    datasetWindow.setInvestigationTitle(investigationName);
                }
                datasetWindow.setDataset(facilityName, investigationId);
            } else {
                datasetWindow.show();
            }
            datasetWindow.setHistoryVerified(true);
        } catch (WindowsNotAvailableExcecption e) {
            showErrorDialog(e.getMessage());
        }
    }

    /**
     * This method will show the dataset window for the given facility name and
     * investigation id, and also updates the browser history.
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
     * This method will show the datafile window for the given dataset models.
     * 
     * @param datasetModel
     *            list of dataset models
     */
    public void showDatafileWindow(ArrayList<DatasetModel> datasetModel) {
        try {
            DatafileWindow datafileWindow = tcWindowManager.findDatafileWindow(datasetModel);
            if (datafileWindow == null) {
                datafileWindow = tcWindowManager.createDatafileWindow();
                datafileWindow.setDatasets(datasetModel);
            } else {
                datafileWindow.show();
            }
            datafileWindow.setHistoryVerified(true);
        } catch (WindowsNotAvailableExcecption e) {
            showErrorDialog(e.getMessage());
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
     * This method will show the parameter window for the given facility name
     * and data set / file id.
     * 
     * @param facilityName
     *            facility name
     * @param dataType
     *            data set or data file
     * @param dataId
     *            data set or data file id
     * @param dataName
     *            data set or data file name
     */
    public void showParameterWindow(String facilityName, String dataType, String dataId, String dataName) {
        try {
            ParameterWindow paramWindow = tcWindowManager.findParameterWindow(facilityName, dataType, dataId);
            if (paramWindow == null) {
                paramWindow = tcWindowManager.createParameterWindow();
                paramWindow.setDataName(dataType, dataName);
                paramWindow.setDataInfo(facilityName, dataId);
            } else {
                paramWindow.show();
            }
            paramWindow.setHistoryVerified(true);
        } catch (WindowsNotAvailableExcecption e) {
            showErrorDialog(e.getMessage());
        }
    }

    /**
     * This method will the show parameter window and also update the browser
     * history.
     * 
     * @param facilityName
     *            facility name
     * @param dataId
     *            data set or file id
     * @param dataName
     *            data set or file name
     */
    public void showParameterWindowWithHistory(String facilityName, String dataType, String dataId, String dataName) {
        showParameterWindow(facilityName, dataType, dataId, dataName);
        historyManager.updateHistory();
    }

    /**
     * This method will take the input facility name and investigation / data
     * set / file id and downloads the parameter files in CSV format.
     * 
     * @param facilityName
     *            facility name
     * @param dataType
     *            data set or data file
     * @param dataId
     *            investigation or data set or file id
     */
    public void downloadParametersData(final String facilityName, final String dataType, final String dataId) {
        // We cannot easily pass on a SessionException so do a logged in check
        // first
        new LoginCheckCompleteEventHandler() {
            final HandlerRegistration reg = LoginCheckCompleteEvent.registerToSource(getEventBus(), facilityName, this);

            @Override
            public void update(final LoginCheckCompleteEvent event) {
                if (event.getLoggedin()) {
                    paramDownloadForm.setFacilityName(facilityName);
                    paramDownloadForm.setDataType(dataType);
                    paramDownloadForm.setDataId(dataId);
                    paramDownloadForm.submit();
                    reg.removeHandler();
                }
            }
        };
        checkStillLoggedIn();
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

    /**
     * Get the facility for the given facility name.
     * 
     * @param a
     *            string containing the facility name
     * @return a facility
     */
    public TFacility getFacility(String facilityName) {
        for (TFacility facility : facilities) {
            if (facility.getName().equalsIgnoreCase(facilityName)) {
                return facility;
            }
        }
        return new TFacility();
    }

    /**
     * Get the facilities.
     * 
     * @return a list of facilities
     */
    public ArrayList<TFacility> getFacilities() {
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
     * Get the investigations for the facility belonging to the user.
     * 
     * @param facilityName
     */
    public void addMyInvestigations(final String facilityName) {
        showRetrievingData();
        utilityService.getMyInvestigationsInServer(facilityName, new AsyncCallback<List<TInvestigation>>() {
            @Override
            public void onFailure(Throwable caught) {
                hideRetrievingData();
                if (caught instanceof SessionException) {
                    checkStillLoggedIn();
                } else if (caught instanceof InternalException) {
                    showErrorDialog(caught.getMessage());
                } else {
                    showErrorDialog("Error retrieving investigation data from server for " + facilityName);
                }
            }

            @Override
            public void onSuccess(List<TInvestigation> result) {
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
                if (caught instanceof SessionException) {
                    checkStillLoggedIn();
                } else if (caught instanceof InsufficientPrivilegesException) {
                    showErrorDialog("Your user has insufficient privileges to get the instrument names for "
                            + facilityName);
                } else {
                    showErrorDialog("Error retrieving instrument names from server for " + facilityName);
                }
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
     * Get the investigations types for the facility.
     * 
     * @param facilityName
     */
    private void addInvestigationTypes(final String facilityName) {
        showRetrievingData();
        utilityService.getInvestigationTypes(facilityName, new AsyncCallback<ArrayList<String>>() {
            @Override
            public void onFailure(Throwable caught) {
                hideRetrievingData();
                if (caught instanceof SessionException) {
                    checkStillLoggedIn();
                } else if (caught instanceof InsufficientPrivilegesException) {
                    showErrorDialog("Your user has insufficient privileges to get the investigation types for "
                            + facilityName);
                } else {
                    showErrorDialog("Error retrieving investigation types from server for " + facilityName);
                }
            }

            @Override
            public void onSuccess(ArrayList<String> investigationTypeList) {
                ArrayList<InvestigationType> investigationTypes = new ArrayList<InvestigationType>();
                for (String investigationType : investigationTypeList) {
                    investigationTypes.add(new InvestigationType(facilityName, investigationType));
                }
                getEventBus().fireEventFromSource(new AddInvestigationTypeEvent(facilityName, investigationTypes),
                        facilityName);
                hideRetrievingData();
            }
        });
    }

    /**
     * Get the investigation title from the server and then set it in the
     * window.
     * 
     * @param facilityName
     * @param investigationId
     * @param datasetWindow
     */
    private void setInvestigationTitle(String facilityName, final String investigationId,
            final DatasetWindow datasetWindow) {
        utilityService.getInvestigationDetails(facilityName, investigationId, new AsyncCallback<TInvestigation>() {
            @Override
            public void onFailure(Throwable caught) {
                if (caught instanceof SessionException) {
                    // session has probably expired, check all sessions to be
                    // safe
                    checkStillLoggedIn();
                } else {
                    showErrorDialog("Error retrieving data from server for investigation " + investigationId);
                }
            }

            @Override
            public void onSuccess(TInvestigation result) {
                datasetWindow.setInvestigationTitle(result.getTitle());
                datasetWindow.repaint();
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
                loggedInFacilities.add(event.getFacilityName());
                addMyInvestigations(event.getFacilityName());
                addInstruments(event.getFacilityName());
                addInvestigationTypes(event.getFacilityName());
                historyManager.updateHistory();
                if (!facilitiesToLogOn.isEmpty()) {
                    checkLoginWidgetStatus();
                }
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
            }
        });
    }

    /**
     * Setup a handler to react to login check complete events for the facility.
     */
    private void createLoginCheckCompleteHandler() {
        // react to the completion of a login check call back and subsequent
        // processing by the LoginInfoPanel
        LoginCheckCompleteEvent.register(EventPipeLine.getEventBus(), new LoginCheckCompleteEventHandler() {
            @Override
            public void update(final LoginCheckCompleteEvent event) {
                if (facilitiesToCheck > 0) {
                    facilitiesToCheck = facilitiesToCheck - 1;
                }
                checkLoginWidgetStatus();
            }
        });
    }

    /**
     * Setup a handler to react to logout window events for the facility.
     */
    private void createLogoutWindowHandler() {
        WindowLogoutEvent.register(EventPipeLine.getEventBus(), new WindowLogoutEventHandler() {
            @Override
            public void logout(final WindowLogoutEvent event) {
                if (!tcWindowManager.areWindowsInUse(event.getFacilityName())) {
                    // only update the history once all of the widows have shut
                    historyManager.updateHistory();
                }
            }
        });
    }

}