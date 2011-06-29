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
package uk.ac.stfc.topcat.gwt.client.widget;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.BufferView;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * This is a floating window widget, It shows list of datasets for a given
 * investigation.
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class DatasetWindow extends Window {
    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
    CheckBoxSelectionModel<DatasetModel> datasetSelectModel;
    private ListStore<DatasetModel> datasetList;
    String facilityName;
    String investigationId;
    String investigationName;
    boolean historyVerified;
    boolean hasData;

    public DatasetWindow() {
        // Update the history upon closing of this window.
        addWindowListener(new WindowListener() {
            public void windowHide(WindowEvent we) {
                EventPipeLine.getInstance().getHistoryManager().updateHistory();
            }
        });
        setHeading("");
        datasetSelectModel = new CheckBoxSelectionModel<DatasetModel>();
        datasetList = new ListStore<DatasetModel>();
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        setLayout(new FillLayout(Orientation.HORIZONTAL));

        configs.add(datasetSelectModel.getColumn());

        ColumnConfig clmncnfgName = new ColumnConfig("datasetName", "Name", 150);
        clmncnfgName.setAlignment(HorizontalAlignment.LEFT);
        configs.add(clmncnfgName);

        ColumnConfig clmncnfgStatus = new ColumnConfig("datasetStatus", "Status", 150);
        configs.add(clmncnfgStatus);

        ColumnConfig clmncnfgType = new ColumnConfig("datasetType", "Type", 150);
        configs.add(clmncnfgType);

        ColumnConfig clmncnfgDescription = new ColumnConfig("datasetDescription", "Description", 200);
        configs.add(clmncnfgDescription);

        Grid<DatasetModel> grid = new Grid<DatasetModel>(datasetList, new ColumnModel(configs));
        add(grid);
        grid.setBorders(true);
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<DatasetModel>>() {
            public void handleEvent(GridEvent<DatasetModel> e) {
                ArrayList<DatasetModel> dsmList = new ArrayList<DatasetModel>();
                dsmList.add(e.getModel());
                EventPipeLine.getInstance().showDatafileWindowWithHistory(dsmList);
            }
        });
        grid.addPlugin(datasetSelectModel);
        grid.setSelectionModel(datasetSelectModel);
        BufferView view = new BufferView();
        view.setRowHeight(32);
        grid.setView(view);
        setSize(600, 400);

        ToolBar toolBar = new ToolBar();
        Button btnView = new Button("View", AbstractImagePrototype.create(Resource.ICONS.iconView()));
        btnView.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                viewDatafileWindow();
            }
        });
        toolBar.add(btnView);
        toolBar.add(new SeparatorToolItem());
        setTopComponent(toolBar);
        hasData = true;
    }

    /**
     * This method sets the facility name and investigation id for this window.
     * using this information it contacts the server using GWT-RPC to get the
     * dataset list.
     * 
     * @param facilityName
     * @param investigationId
     */
    public void setDataset(String facilityName, String investigationId) {
        this.facilityName = facilityName;
        this.investigationId = investigationId;
        EventPipeLine.getInstance().setDialogBox("  Retieveing data...");
        EventPipeLine.getInstance().showDialogBox();
        utilityService.getDatasetsInInvestigations(facilityName, investigationId,
                new AsyncCallback<ArrayList<DatasetModel>>() {
                    @Override
                    public void onSuccess(ArrayList<DatasetModel> result) {
                        EventPipeLine.getInstance().hideDialogBox();
                        setDatasetList(result);
                        hasData = true;
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        EventPipeLine.getInstance().hideDialogBox();
                        // TODO Auto-generated method stub
                        datasetList.removeAll();
                        hasData = false;
                    }
                });
    }

    /**
     * This method sets the dataset list in the window
     * 
     * @param datasetsList
     */
    private void setDatasetList(ArrayList<DatasetModel> datasetsList) {
        this.datasetList.removeAll();
        this.datasetList.add(datasetsList);
        if (datasetsList.size() == 1) {
            datasetSelectModel.selectAll();
            viewDatafileWindow();
        }
    }

    /**
     * This method shows the datafile window for the selected datasets.
     */
    public void viewDatafileWindow() {
        // Get all the datasets selected and show the datafile window
        EventPipeLine.getInstance().showDatafileWindowWithHistory(
                new ArrayList<DatasetModel>(datasetSelectModel.getSelectedItems()));
    }

    /**
     * @return the facility name
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * @return the investigation id
     */
    public String getInvestigationId() {
        return investigationId;
    }

    /**
     * @return the investigation title information
     */
    public String getInvestigationTitle() {
        return investigationName;
    }

    /**
     * This method sets the investigation title of the window (Windows Header
     * information)
     * 
     * @param investigationTitle
     */
    public void setInvestigationTitle(String investigationTitle) {
        investigationName = investigationTitle;
        setHeading("Investigation: " + investigationTitle);
    }

    /**
     * @return the history string for this window
     */
    public String getHistoryString() {
        String history = "";
        history += HistoryManager.seperatorModel + HistoryManager.seperatorToken + "Model"
                + HistoryManager.seperatorKeyValues + "Investigation";
        history += HistoryManager.seperatorToken + "ServerName" + HistoryManager.seperatorKeyValues + facilityName;
        history += HistoryManager.seperatorToken + "InvestigationId" + HistoryManager.seperatorKeyValues
                + investigationId;
        history += HistoryManager.seperatorToken + "InvestigationName" + HistoryManager.seperatorKeyValues
                + investigationName;
        return history;
    }

    /**
     * Checks whether the given input information (facility name and
     * investigation id) matches with the window's information.
     * 
     * @param FacilityName
     * @param InvestigationId
     * @return
     */
    public boolean isSameModel(String FacilityName, String InvestigationId) {
        if (facilityName.compareTo(FacilityName) == 0 && investigationId.compareTo(InvestigationId) == 0)
            return true;
        return false;
    }

    /**
     * @return whether the history is verified or not
     */
    public boolean isHistoryVerified() {
        return historyVerified;
    }

    /**
     * Set the window history verified status
     * 
     * @param historyVerified
     */
    public void setHistoryVerified(boolean historyVerified) {
        this.historyVerified = historyVerified;
    }

    @Override
    public void show() {
        if (!hasData) {
            setDataset(facilityName, investigationId);
        }
        super.show();
    }

    public void reset() {
        facilityName = "";
        investigationId = "";
        datasetList.removeAll();
    }
}
