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
package uk.ac.stfc.topcat.gwt.client.manager;

/**
 * Imports
 */
import com.extjs.gxt.ui.client.util.Point;
import com.google.gwt.user.client.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.exception.WindowsNotAvailableExcecption;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.widget.DatafileWindow;
import uk.ac.stfc.topcat.gwt.client.widget.DatasetWindow;
import uk.ac.stfc.topcat.gwt.client.widget.ParameterWindow;

/**
 * This is Floating window manager, manages Dataset, Datafile, Parameter
 * windows. There is a limit on maximum number of windows that can be opened.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class TopcatWindowManager {
    private ArrayList<DatafileWindow> datafileWindowList;
    private ArrayList<DatasetWindow> datasetWindowList;
    private ArrayList<ParameterWindow> parameterWindowList;
    private final static int MAX_NUMBER_WINDOWS = 5;
    private StringBuilder savedHistory;

    /**
     * Contructor, Initializes the Dataset,Datafile,Parameter windows list
     */
    public TopcatWindowManager() {
        datafileWindowList = new ArrayList<DatafileWindow>();
        datasetWindowList = new ArrayList<DatasetWindow>();
        parameterWindowList = new ArrayList<ParameterWindow>();
        savedHistory = new StringBuilder();
        createLogoutHandler();
    }

    /**
     * Creates a Datafile window, recycles used windows when needed.
     * 
     * @return
     * @throws WindowsNotAvailableExcecption
     */
    public DatafileWindow createDatafileWindow() throws WindowsNotAvailableExcecption {
        // Get the view port position
        Point vp = getStartPointofViewport();
        int i = 0;
        // Check for any hidden windows and return that window.
        for (DatafileWindow dfWin : datafileWindowList) {
            i++;
            if (!dfWin.isVisible()) {
                dfWin.show();
                dfWin.setPosition(vp.x + 30 + i * 10, vp.y + i * 10);
                dfWin.hide();
                dfWin.reset();
                return dfWin;
            }
        }
        if (datafileWindowList.size() < MAX_NUMBER_WINDOWS) {
            DatafileWindow dfWin = new DatafileWindow();
            dfWin.show();
            dfWin.setPosition(vp.x + 30 + i * 10, vp.y + i * 10);
            dfWin.hide();
            datafileWindowList.add(dfWin);
            return dfWin;
        }
        throw new WindowsNotAvailableExcecption(
                "Datafile Windows have exceeded their limit, please close some datafile windows");
    }

    /**
     * Creates/Recycles a Datasetwindow and returns the window object.
     * 
     * @return
     * @throws WindowsNotAvailableExcecption
     */
    public DatasetWindow createDatasetWindow() throws WindowsNotAvailableExcecption {
        // Get the view port position
        Point vp = getStartPointofViewport();
        int i = 0;
        // Check for any hidden windows and return that window.
        for (DatasetWindow dsWin : datasetWindowList) {
            i++;
            if (!dsWin.isVisible()) {
                dsWin.show();
                dsWin.setPosition(vp.x + i * 10, vp.y + i * 10);
                dsWin.hide();
                dsWin.reset();
                return dsWin;
            }
        }
        if (datasetWindowList.size() < MAX_NUMBER_WINDOWS) {
            DatasetWindow dsWin = new DatasetWindow();
            dsWin.show();
            dsWin.setPosition(vp.x + i * 10, vp.y + i * 10);
            dsWin.hide();
            datasetWindowList.add(dsWin);
            return dsWin;
        }
        throw new WindowsNotAvailableExcecption(
                "Dataset Windows have exceeded their limit, please close some dataset windows");
    }

    /**
     * Creates/Recycles a ParameterWindow and returns the window object
     * 
     * @return
     * @throws WindowsNotAvailableExcecption
     */
    public ParameterWindow createParameterWindow() throws WindowsNotAvailableExcecption {
        // Get the view port position
        Point vp = getStartPointofViewport();
        int i = 0;
        // Check for any hidden windows and return that window.
        for (ParameterWindow paramWin : parameterWindowList) {
            i++;
            if (!paramWin.isVisible()) {
                paramWin.show();
                paramWin.setPosition(vp.x + 60 + i * 10, vp.y + i * 10);
                paramWin.hide();
                return paramWin;
            }
        }
        if (parameterWindowList.size() < MAX_NUMBER_WINDOWS) {
            ParameterWindow paramWin = new ParameterWindow();
            paramWin.show();
            paramWin.setPosition(vp.x + 60 + i * 10, vp.y + i * 10);
            paramWin.hide();
            parameterWindowList.add(paramWin);
            return paramWin;
        }
        throw new WindowsNotAvailableExcecption(
                "Parameter Windows have exceeded their limit, please close some parameter windows");
    }

    /**
     * Generates the history string based on the currently open windows.
     * 
     * @return
     */
    public String getWindowHistoryString() {
        String history = savedHistory.toString();
        for (DatasetWindow datasetWindow : datasetWindowList) {
            if (datasetWindow.isVisible())
                history += datasetWindow.getHistoryString();
        }
        for (DatafileWindow datafileWindow : datafileWindowList) {
            if (datafileWindow.isVisible())
                history += datafileWindow.getHistoryString();
        }
        for (ParameterWindow parameterWindow : parameterWindowList) {
            if (parameterWindow.isVisible())
                history += parameterWindow.getHistoryString();
        }
        return history;
    }

    /**
     * Closes all windows that are open
     */
    public void closeAllWindows() {
        for (DatasetWindow datasetWindow : datasetWindowList) {
            datasetWindow.hide();
        }
        for (DatafileWindow datafileWindow : datafileWindowList) {
            datafileWindow.hide();
        }
        for (ParameterWindow parameterWindow : parameterWindowList) {
            parameterWindow.hide();
        }
    }

    /**
     * Closes all open windows for the given facility
     */
    private void closeAllWindows(String facilityName) {
        for (DatasetWindow datasetWindow : datasetWindowList) {
            if (datasetWindow.getFacilityName().equals(facilityName)) {
                datasetWindow.hide();
            }
        }
        for (DatafileWindow datafileWindow : datafileWindowList) {
            if (datafileWindow.getFacilityNames().contains(facilityName)) {
                datafileWindow.hide();
            }
        }
        for (ParameterWindow parameterWindow : parameterWindowList) {
            if (parameterWindow.getFacilityName().equals(facilityName)) {
                parameterWindow.hide();
            }
        }
        EventPipeLine.getInstance().getHistoryManager().updateHistory();
    }

    /**
     * Sets All the windows whether the window is verified
     */
    public void setNotVerifiedAllWindows() {
        for (DatasetWindow datasetWindow : datasetWindowList) {
            datasetWindow.setHistoryVerified(false);
        }
        for (DatafileWindow datafileWindow : datafileWindowList) {
            datafileWindow.setHistoryVerified(false);
        }
        for (ParameterWindow parameterWindow : parameterWindowList) {
            parameterWindow.setHistoryVerified(false);
        }
    }

    /**
     * Hides all the windows that are not history verified
     */
    public void hideNotVerifiedWindows() {
        for (DatasetWindow datasetWindow : datasetWindowList) {
            if (!datasetWindow.isHistoryVerified())
                datasetWindow.hide();
        }
        for (DatafileWindow datafileWindow : datafileWindowList) {
            if (!datafileWindow.isHistoryVerified())
                datafileWindow.hide();
        }
        for (ParameterWindow parameterWindow : parameterWindowList) {
            if (!parameterWindow.isHistoryVerified())
                parameterWindow.hide();
        }
    }

    /**
     * This method process the history string and creates/recycles windows based
     * on history string. hides the windows that are not there in the history
     * string. If the history contains a facility to which the use is not logged
     * on then save the current history and display the LoginWidget.
     * 
     * @param history
     */
    public void processHistoryString(String history) {
        setNotVerifiedAllWindows();
        Set<String> loggedInFacilities = EventPipeLine.getInstance().getLoggedInFacilities();
        String loginToFacility = "";
        StringBuilder savedHistory = new StringBuilder();
        // split the models
        String[] historyTokenList = history.split(HistoryManager.seperatorModel);
        // split the model string to find the model type
        for (String hToken : historyTokenList) {
            // split the hToken to model params
            String[] paramList = hToken.split(HistoryManager.seperatorToken);
            // convert each param into keyvalue pairs
            HashMap<String, String> paramMap = new HashMap<String, String>();
            for (String param : paramList) {
                String[] keyvalues = param.split(HistoryManager.seperatorKeyValues);
                try {
                    String key = keyvalues[0];
                    String value = keyvalues[1];
                    paramMap.put(key, value);
                } catch (IndexOutOfBoundsException ex) {
                }
            }

            // now get the type of the model
            if (paramMap.get("Model") != null && paramMap.get("Model").compareToIgnoreCase("Investigation") == 0) {
                if (loggedInFacilities.contains(paramMap.get("ServerName"))) {
                    DatasetWindow dsWin = findDatasetWindow(paramMap.get("ServerName"), paramMap.get("InvestigationId"));
                    if (dsWin == null)
                        EventPipeLine.getInstance().showDatasetWindow(paramMap.get("ServerName"),
                                paramMap.get("InvestigationId"), paramMap.get("InvestigationName"));
                    else {
                        dsWin.show();
                        dsWin.setHistoryVerified(true);
                    }
                } else {
                    savedHistory.append(HistoryManager.seperatorModel).append(hToken);
                    if (loginToFacility.isEmpty()) {
                        loginToFacility = paramMap.get("ServerName");
                    }
                }
            } else if (paramMap.get("Model") != null && paramMap.get("Model").compareToIgnoreCase("Dataset") == 0) {
                // create dataset models
                ArrayList<DatasetModel> dsModelList = new ArrayList<DatasetModel>();
                for (int i = 0; i < paramMap.size(); i++) {
                    if (paramMap.containsKey("DSId-" + i)) {
                        if (loggedInFacilities.contains(paramMap.get("SN-" + i))) {
                            dsModelList.add(new DatasetModel(paramMap.get("SN-" + i), paramMap.get("DSId-" + i),
                                    paramMap.get("DSName-" + i), null, null, null));
                            paramMap.remove("SN-" + i);
                            paramMap.remove("DSId-" + i);
                            paramMap.remove("DSName-" + i);
                        } else {
                            savedHistory.append(HistoryManager.seperatorModel).append(hToken);
                            if (loginToFacility.isEmpty()) {
                                loginToFacility = paramMap.get("ServerName");
                            }
                        }
                    }
                }
                if (dsModelList.size() > 0) {
                    DatafileWindow dfWin = findDatafileWindow(dsModelList);
                    if (dfWin == null)
                        EventPipeLine.getInstance().showDatafileWindow(dsModelList);
                    else {
                        dfWin.show();
                        dfWin.setHistoryVerified(true);
                    }
                }
            } else if (paramMap.get("Model") != null && paramMap.get("Model").compareToIgnoreCase("Parameter") == 0) {
                if (loggedInFacilities.contains(paramMap.get("SN"))) {
                    ParameterWindow paramWin = findParameterWindow(paramMap.get("SN"), paramMap.get("DFId"));
                    if (paramWin == null)
                        EventPipeLine.getInstance().showParameterWindow(paramMap.get("SN"), paramMap.get("DFId"),
                                paramMap.get("DFN"));
                    else {
                        paramWin.show();
                        paramWin.setHistoryVerified(true);
                    }
                } else {
                    savedHistory.append(HistoryManager.seperatorModel).append(hToken);
                    if (loginToFacility.isEmpty()) {
                        loginToFacility = paramMap.get("SN");
                    }
                }
            }
        }
        hideNotVerifiedWindows();
        this.savedHistory = savedHistory;
        // Update the Login info
        if (!loginToFacility.isEmpty()) {
            // we were not logged in so prompt user to log in to this facility
            EventPipeLine.getInstance().setFacilityToLogOn(loginToFacility);
        }
        EventPipeLine.getInstance().checkStillLoggedIn();
    }

    /**
     * Checks all the Datafilewindows whether the input DatasetModel matches to
     * the Datafilewindows info. If exists then return the window otherwise
     * returns null.
     * 
     * @param dsModelList
     * @return
     */
    public DatafileWindow findDatafileWindow(ArrayList<DatasetModel> dsModelList) {
        for (DatafileWindow dfWin : datafileWindowList) {
            if (dfWin.isSameModel(dsModelList)) {
                return dfWin;
            }
        }
        return null;
    }

    /**
     * Checks all the Datasetwindows whether the input facility name and
     * investigation id matches to the Datasetwindows info. If exists then
     * return the window otherwise returns null.
     * 
     * @param serverName
     * @param investigationId
     * @return
     */
    public DatasetWindow findDatasetWindow(String serverName, String investigationId) {
        for (DatasetWindow dsWin : datasetWindowList) {
            if (dsWin.isSameModel(serverName, investigationId))
                return dsWin;
        }
        return null;
    }

    /**
     * Checks all the Parameterwindows whether the input facility name and
     * datafile id matches to the Parameterwindows info. If exists then return
     * the window otherwise returns null.
     * 
     * @param serverName
     * @param datafileId
     * @return
     */
    public ParameterWindow findParameterWindow(String serverName, String datafileId) {
        for (ParameterWindow paramWin : parameterWindowList) {
            if (paramWin.isSameModel(serverName, datafileId))
                return paramWin;
        }
        return null;
    }

    private Point getStartPointofViewport() {
        int left = Window.getScrollLeft();
        int top = Window.getScrollTop();
        return new Point(left, top);
    }

    /**
     * Setup a handler to react to logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                closeAllWindows(event.getFacilityName());
            }
        });
    }

}
