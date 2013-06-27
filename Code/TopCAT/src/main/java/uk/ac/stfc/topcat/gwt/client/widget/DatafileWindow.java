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
package uk.ac.stfc.topcat.gwt.client.widget;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.stfc.topcat.core.gwt.module.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.DownloadButtonEvent;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.LoginEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.event.WindowLogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LoginEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.manager.DownloadManager;
import uk.ac.stfc.topcat.gwt.client.manager.HistoryManager;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * This is a floating window widget, It shows list of datafiles for a given
 * investigation.
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class DatafileWindow extends Window {
    private final UtilityServiceAsync utilityService = UtilityService.Util.getInstance();
    CheckBoxSelectionModel<DatafileModel> datafileSelectionModel = null;
    GroupingStore<DatafileModel> dfmStore;
    ArrayList<DatasetModel> inputDatasetModels = new ArrayList<DatasetModel>();
    private PagingModelMemoryProxy pageProxy = null;
    private PagingLoader<PagingLoadResult<DatafileModel>> loader = null;
    private PagingToolBar pageBar = null;
    private Set<Long> selectedFiles = new HashSet<Long>();

    boolean historyVerified;
    Grid<DatafileModel> grid;
    private Set<String> facilityNames = new HashSet<String>();
    private boolean awaitingLogin;
    private boolean loadingData = false;
    private boolean advancedSearchData = false;

    /** Number of rows of data. */
    private static final int PAGE_SIZE = 20;

    public DatafileWindow() {
        // Listener called when the datafile window is closed.
        addWindowListener(new WindowListener() {
            @Override
            public void windowHide(WindowEvent we) {
                // Go to page one and de-select everything in case we reuse this
                // window to display this data again
                loader.load(0, PAGE_SIZE);
                datafileSelectionModel.deselectAll();
                // Update the history to notify the close of datafile window
                EventPipeLine.getInstance().getHistoryManager().updateHistory();
            }
        });

        datafileSelectionModel = createDatafileSelectionModel();

        setHeading("Datafile Window");
        setLayout(new RowLayout(Orientation.VERTICAL));
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        configs.add(datafileSelectionModel.getColumn());

        ColumnConfig clmncnfgDatasetName = new ColumnConfig("datasetName", "Dataset Name", 150);
        configs.add(clmncnfgDatasetName);

        ColumnConfig clmncnfgFileName = new ColumnConfig("datafileName", "File Name", 150);
        configs.add(clmncnfgFileName);

        ColumnConfig clmncnfgFileLocation = new ColumnConfig("datafileLocation", "File Location", 150);
        configs.add(clmncnfgFileLocation);

        ColumnConfig clmncnfgFileSizeb = new ColumnConfig("datafileSize", "File Size", 150);
        configs.add(clmncnfgFileSizeb);

        ColumnConfig clmncnfgFormat = new ColumnConfig("datafileFormat", "Format", 150);
        configs.add(clmncnfgFormat);

        ColumnConfig clmncnfgFormatVersion = new ColumnConfig("datafileFormatVersion", "Format Version", 150);
        configs.add(clmncnfgFormatVersion);

        ColumnConfig clmncnfgFormatType = new ColumnConfig("datafileFormatType", "Format Type", 150);
        configs.add(clmncnfgFormatType);

        ColumnConfig clmncnfgCreateTime = new ColumnConfig("datafileCreateTime", "Create Time", 150);
        clmncnfgCreateTime.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));
        configs.add(clmncnfgCreateTime);

        final ColumnModel cm = new ColumnModel(configs);
        GroupingView view = new GroupingView();
        view.setShowGroupedColumn(false);
        view.setForceFit(true);
        view.setGroupRenderer(new GridGroupRenderer() {
            @Override
            public String render(GroupColumnData data) {
                String f = cm.getColumnById(data.field).getHeader();
                String l = data.models.size() == 1 ? "Item" : "Items";
                return f + ": " + data.group + " (" + data.models.size() + " " + l + ")";
            }
        });

        // Pagination
        pageProxy = new PagingModelMemoryProxy(dfmStore);
        loader = new BasePagingLoader<PagingLoadResult<DatafileModel>>(pageProxy);
        loader.setRemoteSort(true);
        dfmStore = new GroupingStore<DatafileModel>(loader);
        dfmStore.groupBy("datasetName");

        // Grid
        final Grid<DatafileModel> grid = new Grid<DatafileModel>(dfmStore, cm);
        grid.setHeight("420px");
        grid.setView(view);
        grid.setBorders(true);
        grid.setToolTip("\"Double Click\" row to show data file, right click for more options");
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<DatafileModel>>() {
            @Override
            public void handleEvent(GridEvent<DatafileModel> e) {
                DatafileModel datafile = e.getModel();
                EventPipeLine.getInstance().showParameterWindowWithHistory(datafile.getFacilityName(),
                        Constants.DATA_FILE, datafile.getId(), datafile.getName());
            }
        });

        grid.addPlugin(datafileSelectionModel);
        grid.setSelectionModel(datafileSelectionModel);

        // ToolBar with download button
        ToolBar toolBar = new ToolBar();
        final DownloadButton btnDownload = new DownloadButton();
        btnDownload.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                download(((DownloadButtonEvent) ce).getDownloadName());
            }
        });
        toolBar.add(btnDownload);
        toolBar.add(new SeparatorToolItem());
        setTopComponent(toolBar);

        // Context Menu
        Menu contextMenu = new Menu();
        contextMenu.setWidth(160);
        MenuItem showDS = new MenuItem();
        showDS.setText("show data file");
        showDS.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconView()));
        contextMenu.add(showDS);
        showDS.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                DatafileModel dfm = (DatafileModel) grid.getSelectionModel().getSelectedItem();
                EventPipeLine.getInstance().showParameterWindowWithHistory(dfm.getFacilityName(), Constants.DATA_FILE,
                        dfm.getId(), dfm.getName());
            }
        });
        MenuItem showFS = new MenuItem();
        showFS.setText("download data file");
        showFS.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconView()));
        contextMenu.add(showFS);
        showFS.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                // simulate the download button being pressed
                btnDownload.fireEvent(Events.BeforeSelect, new ButtonEvent(btnDownload));
            }
        });
        grid.setContextMenu(contextMenu);

        setLayout(new FitLayout());
        setSize(700, 400);

        // Pagination Bar
        pageBar = new PagingToolBar(PAGE_SIZE) {
            @Override
            public void refresh() {
                super.refresh();
                if (inputDatasetModels.size() > 0) {
                    // datafiles are from datasets
                    loadData();
                    refresh.show();
                } else {
                    // datafiles are from advanced search, i.e. a selection of
                    // individual data files
                    refresh.hide();
                }
            }
        };
        pageBar.bind(loader);
        setBottomComponent(pageBar);

        add(grid);
        awaitingLogin = false;
        createLoginHandler();
        createLogoutHandler();
    }

    /**
     * Set the datasets input which are used to get the datafiles corresponding
     * to each dataset and are displayed in the window.
     * 
     * @param datasetList
     *            an array of <code>DatasetModel</code>
     */
    public void setDatasets(ArrayList<DatasetModel> datasetList) {
        advancedSearchData = false;
        inputDatasetModels = datasetList;
        facilityNames = new HashSet<String>();
        for (DatasetModel dsm : inputDatasetModels) {
            facilityNames.add(dsm.getFacilityName());
        }
        if (EventPipeLine.getInstance().getLoggedInFacilities().containsAll(facilityNames)) {
            awaitingLogin = false;
            loadData();
        } else {
            awaitingLogin = true;
        }
    }

    /**
     * Set the datafile list to be displayed in the window.
     * 
     * @param datafileList
     *            an array of <code>DatafileModel</code>
     */
    public void setAdvancedSearchResult(String facilityName, ArrayList<DatafileModel> datafileList) {
        loadingData = true;
        advancedSearchData = true;
        facilityNames.add(facilityName);
        setDatafileList(datafileList);
        show();
        EventPipeLine.getInstance().getHistoryManager().updateHistory();
        loadingData = false;
    }

    /**
     * Set the datafile list to be displayed in the window.
     * 
     * @param datafileList
     *            an array of <code>DatafileModel</code>
     */
    private void setDatafileList(ArrayList<DatafileModel> datafileList) {
        dfmStore.removeAll();
        selectedFiles.clear();
        NumberFormat format = NumberFormat.getDecimalFormat();
        // convert Bytes to MegaBytes
        for (DatafileModel dfm : datafileList) {
            Float size = Float.parseFloat(dfm.getFileSize());
            size = size / 1048576.0f;
            dfm.setFileSize(format.format(size.doubleValue()) + " MB");
        }
        pageProxy.setData(datafileList);
        loader.load(0, PAGE_SIZE);
        pageBar.refresh();
    }

    /**
     * Get the history of this window.
     * 
     * @return the history string of this window
     */
    public String getHistoryString() {
        if (advancedSearchData) {
            // we do not have any history for advanced searches
            return "";
        }
        String history = "";
        history += HistoryManager.seperatorModel + HistoryManager.seperatorToken + "Model"
                + HistoryManager.seperatorKeyValues + "Dataset";
        int count = 0;
        for (DatasetModel dataset : inputDatasetModels) {
            history += HistoryManager.seperatorToken + "SN-" + count + HistoryManager.seperatorKeyValues
                    + dataset.getFacilityName();
            history += HistoryManager.seperatorToken + "DSId-" + count + HistoryManager.seperatorKeyValues
                    + dataset.getId();
            count++;
        }
        return history;
    }

    /**
     * Compares the input list of <code>DatasetModel</code>s with the
     * <code>DatasetModel</code>s in the current windows.
     * 
     * @param dsModelList
     *            array of <code>DatasetModel</code>
     * @return <code>true</code> if input list matches current windows
     *         <code>DatasetModel</code>s
     */
    public boolean isSameModel(ArrayList<DatasetModel> dsModelList) {
        if (dsModelList.size() != inputDatasetModels.size()) {
            return false;
        }
        int index = 0;
        for (DatasetModel dsModel : dsModelList) {
            if (dsModel.getFacilityName().compareTo(inputDatasetModels.get(index).getFacilityName()) != 0
                    || dsModel.getId().compareTo(inputDatasetModels.get(index).getId()) != 0)
                return false;
            index++;
        }
        return true;
    }

    /**
     * Check if the history is verified.
     * 
     * @return returns the history verified status
     */
    public boolean isHistoryVerified() {
        if (advancedSearchData) {
            // we do not have any history for advanced searches but we still
            // want to use this window
            return true;
        }
        return historyVerified;
    }

    /**
     * Set the history verified status.
     * 
     * @param historyVerified
     *            history verified status
     */
    public void setHistoryVerified(boolean historyVerified) {
        this.historyVerified = historyVerified;
    }

    @Override
    public void show() {
        if (awaitingLogin || loadingData) {
            return;
        }
        if (!EventPipeLine.getInstance().getLoggedInFacilities().containsAll(facilityNames)) {
            // trying to use window but we are not logged in
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
        if (!facilityNames.contains(facilitName)) {
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
        if (awaitingLogin || loadingData) {
            return true;
        }
        return super.isVisible();
    }

    /**
     * Clear out data ready for window reuse.
     */
    public void reset() {
        facilityNames = new HashSet<String>();
        inputDatasetModels.clear();
        dfmStore.removeAll();
        datafileSelectionModel.refresh();
        selectedFiles.clear();
        awaitingLogin = false;
        advancedSearchData = false;
    }

    /**
     * Get a customised CheckBoxSelectionModel
     * 
     * @return a customised CheckBoxSelectionModel
     */
    private CheckBoxSelectionModel<DatafileModel> createDatafileSelectionModel() {
        CheckBoxSelectionModel<DatafileModel> dfSelectionModel = new CheckBoxSelectionModel<DatafileModel>() {
            private boolean allSelected = false;

            @SuppressWarnings("unchecked")
            @Override
            public void deselectAll() {
                super.deselectAll();
                if (pageProxy.getData() != null) {
                    for (DatafileModel m : (List<DatafileModel>) pageProxy.getData()) {
                        m.setSelected(false);
                    }
                }
                selectedFiles.clear();
                allSelected = false;
            }

            @SuppressWarnings("unchecked")
            @Override
            public void selectAll() {
                super.selectAll();
                if (allSelected) {
                    // no need to loop through every item again
                    return;
                }
                for (DatafileModel m : (List<DatafileModel>) pageProxy.getData()) {
                    m.setSelected(true);
                    selectedFiles.add(new Long(m.getId()));
                }
                allSelected = true;
                setChecked(allSelected);
            }

            @Override
            protected void doDeselect(List<DatafileModel> models, boolean supressEvent) {
                super.doDeselect(models, supressEvent);
                if (supressEvent) {
                    return;
                }
                for (DatafileModel m : models) {
                    m.setSelected(false);
                    selectedFiles.remove(new Long(m.getId()));
                }
                allSelected = false;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void doSelect(List<DatafileModel> models, boolean keepExisting, boolean supressEvent) {
                super.doSelect(models, keepExisting, supressEvent);
                if (supressEvent) {
                    return;
                }
                for (DatafileModel m : models) {
                    m.setSelected(true);
                    selectedFiles.add(new Long(m.getId()));
                }
                if (selectedFiles.size() == ((List<DatafileModel>) pageProxy.getData()).size()) {
                    allSelected = true;
                } else {
                    allSelected = false;
                }
                setChecked(allSelected);
            }

            @Override
            public void refresh() {
                super.refresh();
                List<DatafileModel> previouslySelected = new ArrayList<DatafileModel>();
                for (DatafileModel m : listStore.getModels()) {
                    if (m.getSelected()) {
                        previouslySelected.add(m);
                    } else {
                        allSelected = false;
                    }
                }
                if (previouslySelected.size() > 0) {
                    doSelect(previouslySelected, true, false);
                }
            }

            private void setChecked(boolean checked) {
                if (grid.isViewReady()) {
                    El hd = El.fly(grid.getView().getHeader().getElement()).selectNode("div.x-grid3-hd-checker");
                    if (hd != null) {
                        hd.getParent().setStyleName("x-grid3-hd-checker-on", checked);
                    }
                }
            }
        };

        return dfSelectionModel;
    }

    /**
     * Download selected datafiles.
     * 
     * @param downloadName
     *            the display name for the download
     */
    private void download(String downloadName) {
        if (selectedFiles.size() == 0) {
            EventPipeLine.getInstance().showMessageDialog("No files selected for download");
            return;
        }
        List<Long> selectedItems = new ArrayList<Long>(selectedFiles);
        @SuppressWarnings("unchecked")
        String facility = ((List<DatafileModel>) pageProxy.getData()).get(0).getFacilityName();
        DownloadManager.getInstance().downloadDatafiles(facility, selectedItems, downloadName);
        EventPipeLine.getInstance().showMessageDialog(
                "Your data is being retrieved, this may be from tape, and will automatically start downloading shortly "
                        + "as a single file. The status of your download can be seen from the ‘My Downloads’ tab.");
    }

    /**
     * Call the server to get fresh data.
     */
    private void loadData() {
        if (loadingData) {
            return;
        }
        loadingData = true;
        advancedSearchData = false;
        EventPipeLine.getInstance().showRetrievingData();
        utilityService.getDatafilesInDatasets(inputDatasetModels, new AsyncCallback<ArrayList<DatafileModel>>() {
            @Override
            public void onSuccess(ArrayList<DatafileModel> result) {
                EventPipeLine.getInstance().hideRetrievingData();
                if (result.size() > 0) {
                    setDatafileList(result);
                    loadingData = false;
                    show();
                    EventPipeLine.getInstance().getHistoryManager().updateHistory();
                } else {
                    EventPipeLine.getInstance().showMessageDialog("No files returned");
                    hide();
                    reset();
                }
                loadingData = false;
            }

            @Override
            public void onFailure(Throwable caught) {
                EventPipeLine.getInstance().hideRetrievingData();
                hide();
                reset();
                loadingData = false;
                if (caught instanceof SessionException) {
                    EventPipeLine.getInstance().checkStillLoggedIn();
                } else {
                    EventPipeLine.getInstance().showErrorDialog("Error retrieving datafiles");
                }
            }
        });
    }

    /**
     * Setup a handler to react to logout events.
     */
    private void createLoginHandler() {
        LoginEvent.register(EventPipeLine.getEventBus(), new LoginEventHandler() {
            @Override
            public void login(LoginEvent event) {
                if (awaitingLogin && EventPipeLine.getInstance().getLoggedInFacilities().containsAll(facilityNames)) {
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
                if (isInUse() && facilityNames.contains(event.getFacilityName())) {
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
