/**
 * 
 * Copyright (c) 2009-2012
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
package uk.ac.stfc.topcat.gwt.client.widget;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.event.WindowLogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.model.ParameterModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This class implements the gxt floating window which shows the list of
 * parameters.
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class ParameterWindow extends Window {
    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);

    private ListStore<ParameterModel> parameterStore;
    private boolean historyVerified;
    private String facilityName;
    private String dataType;
    private String dataId;
    private String dataName;
    private boolean awaitingLogin;

    public ParameterWindow() {
        // Listener called when the parameter window is closed.
        addWindowListener(new WindowListener() {
            @Override
            public void windowHide(WindowEvent we) {
                // Update the history to notify the close of parameter window
                EventPipeLine.getInstance().getHistoryManager().updateHistory();
            }
        });
        parameterStore = new ListStore<ParameterModel>();
        setHeading("");
        setLayout(new RowLayout(Orientation.VERTICAL));
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ToolBar toolBar = new ToolBar();

        Button btnExport = new Button("Export");
        btnExport.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                EventPipeLine.getInstance().downloadParametersData(facilityName, dataType, dataId);
            }
        });
        toolBar.add(btnExport);

        SeparatorToolItem separatorToolItem = new SeparatorToolItem();
        toolBar.add(separatorToolItem);
        add(toolBar);

        ColumnConfig clmncnfgName = new ColumnConfig("name", "Name", 150);
        configs.add(clmncnfgName);

        ColumnConfig clmncnfgUnits = new ColumnConfig("units", "Units", 200);
        configs.add(clmncnfgUnits);

        ColumnConfig clmncnfgValue = new ColumnConfig("value", "Value", 150);
        configs.add(clmncnfgValue);

        GridView view = new GridView();
        view.setForceFit(true);
        Grid<ParameterModel> grid = new Grid<ParameterModel>(parameterStore, new ColumnModel(configs));
        grid.setHeight("391px");
        grid.setView(view);
        grid.setBorders(true);
        setSize("500px", "450px");
        add(grid);

        awaitingLogin = false;
        createLoginHandler();
        createLogoutHandler();
    }

    /**
     * This method sets the data set / file name.
     * 
     * @param dataType
     *            data set or data file
     * @param dataName
     *            Data set or file name
     */
    public void setDataName(String dataType, String dataName) {
        this.dataType = dataType;
        this.dataName = dataName;
        if (dataType.equals(Constants.DATA_SET)) {
            setHeading("Dataset: " + dataName);
        } else {
            setHeading("Datafile: " + dataName);
        }
    }

    /**
     * This method sets the data set/file information of the parameter window,
     * this will call the AJAX method to get the parameters information from the
     * server and displayed in this window.
     * 
     * @param facilityName
     *            iCAT instance name
     * @param dataId
     *            Data set or data file id
     */
    public void setDataInfo(String facilityName, String dataId) {
        this.facilityName = facilityName;
        this.dataId = dataId;
        if (EventPipeLine.getInstance().getLoggedInFacilities().contains(facilityName)) {
            awaitingLogin = false;
            loadData();
        } else {
            awaitingLogin = true;
        }
    }

    /**
     * @return the history string corresponding to current window.
     */
    public String getHistoryString() {
        String history = "";
        history += HistoryManager.seperatorModel + HistoryManager.seperatorToken + "Model"
                + HistoryManager.seperatorKeyValues + "Parameter";
        history += HistoryManager.seperatorToken + "SN" + HistoryManager.seperatorKeyValues + facilityName;
        history += HistoryManager.seperatorToken + "DT" + HistoryManager.seperatorKeyValues + dataType;
        history += HistoryManager.seperatorToken + "DId" + HistoryManager.seperatorKeyValues + dataId;
        history += HistoryManager.seperatorToken + "DN" + HistoryManager.seperatorKeyValues + dataName;
        return history;
    }

    /**
     * This method compares the input information with the current window
     * information (such as datafile id and server name). if the match then they
     * return true otherwise false
     * 
     * @param facilityName
     *            the name of the facility
     * @param dataType
     *            data set or data file
     * @param dataId
     *            id of the data set or data file
     */
    public boolean isSameModel(String facilityName, String dataType, String dataId) {
        if (facilityName.compareTo(facilityName) == 0 && this.dataType.compareTo(dataType) == 0
                && this.dataId.compareTo(dataId) == 0)
            return true;
        return false;
    }

    /**
     * @return the historyVerified flag
     */
    public boolean isHistoryVerified() {
        return historyVerified;
    }

    /**
     * Sets the history verified flag
     * 
     * @param historyVerified
     */
    public void setHistoryVerified(boolean historyVerified) {
        this.historyVerified = historyVerified;
    }

    @Override
    public void show() {
        if (awaitingLogin || !EventPipeLine.getInstance().getLoggedInFacilities().contains(facilityName)) {
            return;
        }
        if (facilityName != null && !EventPipeLine.getInstance().getLoggedInFacilities().contains(facilityName)) {
            // trying to use/reuse window but we are not logged in
            awaitingLogin = true;
            return;
        }
        super.show();
    }

    /**
     * Check if the widget is in use by the given facility, i.e. waiting for the
     * user to log in or widget already visible.
     * 
     * @param facilitName
     * @return true if the widget is in use
     */
    public boolean isInUse(String facilitName) {
        if (!facilityName.equals(facilitName)) {
            return false;
        } else {
            return isInUse();
        }
    }

    /**
     * Check if the widget is in use, i.e. waiting for the user to log in or
     * widget already visible.
     * 
     * @return true if the widget is in use
     */
    public boolean isInUse() {
        if (awaitingLogin) {
            return true;
        }
        return super.isVisible();
    }

    /**
     * Clear out data ready for window reuse.
     */
    public void reset() {
        facilityName = "";
        dataType = "";
        dataId = "";
        parameterStore.removeAll();
        awaitingLogin = false;
    }

    /**
     * Call the server to get fresh data.
     */
    private void loadData() {
        EventPipeLine.getInstance().showRetrievingData();
        if (dataType.equals(Constants.DATA_SET)) {
            utilityService.getDatasetParameters(facilityName, dataId, new AsyncCallback<ArrayList<ParameterModel>>() {
                @Override
                public void onSuccess(ArrayList<ParameterModel> result) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    if (result.size() > 0) {
                        setParameterList(result);
                        show();
                        EventPipeLine.getInstance().getHistoryManager().updateHistory();
                    } else {
                        EventPipeLine.getInstance().showMessageDialog("No Parameters");
                        hide();
                        reset();
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    EventPipeLine.getInstance().showErrorDialog("Error retrieving parameters");
                    hide();
                    reset();
                }
            });
        } else {
            utilityService.getDatafileParameters(facilityName, dataId, new AsyncCallback<ArrayList<ParameterModel>>() {
                @Override
                public void onSuccess(ArrayList<ParameterModel> result) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    if (result.size() > 0) {
                        setParameterList(result);
                        show();
                        EventPipeLine.getInstance().getHistoryManager().updateHistory();
                    } else {
                        EventPipeLine.getInstance().showMessageDialog("No Parameters");
                        hide();
                        reset();
                    }
                }

                @Override
                public void onFailure(Throwable caught) {
                    EventPipeLine.getInstance().hideRetrievingData();
                    EventPipeLine.getInstance().showErrorDialog("Error retrieving parameters");
                    hide();
                    reset();
                }
            });
        }
    }

    /**
     * This method sets the parameters that will be displayed in the window.
     * 
     * @param parameterList
     *            list of parameters
     */
    private void setParameterList(ArrayList<ParameterModel> parameterList) {
        parameterStore.removeAll();
        parameterStore.add(parameterList);
    }

    /**
     * Setup a handler to react to logout events.
     */
    private void createLoginHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                if (awaitingLogin && event.getFacilityName().equals(facilityName)) {
                    awaitingLogin = false;
                    loadData();
                }
            }
        });
    }

    /**
     * Setup a handler to react to logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                if (isInUse() && facilityName.equals(event.getFacilityName())) {
                    // When we open a web page with a url a status check is done
                    // on all facilities. We do not want this to remove this
                    // window. However when the user presses the cancel button
                    // on the login widget we do want to remove this window.
                    if (!event.isStatusCheck() || isVisible()) {
                        reset();
                    }
                    hide();
                    EventPipeLine.getEventBus().fireEventFromSource(new WindowLogoutEvent(event.getFacilityName()),
                            event.getFacilityName());
                }
            }
        });
    }
}
