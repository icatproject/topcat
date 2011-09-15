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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
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
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.event.WindowListener;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridGroupRenderer;
import com.extjs.gxt.ui.client.widget.grid.GroupColumnData;
import com.extjs.gxt.ui.client.widget.grid.GroupingView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
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
    private final UtilityServiceAsync utilityService = GWT.create(UtilityService.class);
    ParameterWindow datafileInfoWindow;
    CheckBoxSelectionModel<DatafileModel> datafileSelectModel;
    GroupingStore<DatafileModel> dfmStore;
    ArrayList<DatasetModel> inputDatasetModels;
    private PagingModelMemoryProxy pageProxy = null;
    private PagingLoader<PagingLoadResult<DatafileModel>> loader = null;
    private PagingToolBar pageBar = null;
    private GroupingView view;
    private Map<Long, Boolean> selectedFiles = new HashMap<Long, Boolean>();
    boolean historyVerified;
    boolean hasData;
    Grid<DatafileModel> grid;

    /** Number of rows of data. */
    private static final int PAGE_SIZE = 20;

    public DatafileWindow() {
        addWindowListener(new WindowListener() {
            @Override
            public void windowHide(WindowEvent we) {
                EventPipeLine.getInstance().getHistoryManager().updateHistory();
            }
        });

        datafileSelectModel = new CheckBoxSelectionModel<DatafileModel>() {
            private boolean allSelected = false;

            @SuppressWarnings("unchecked")
            @Override
            public void deselectAll() {
                super.deselectAll();
                for (DatafileModel m : (List<DatafileModel>) pageProxy.getData()) {
                    m.setSelected(false);
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
                    selectedFiles.put(new Long(m.getId()), true);
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
                    selectedFiles.put(new Long(m.getId()), true);
                }
                if (!allSelected && selectedFiles.size() == ((List<DatafileModel>) pageProxy.getData()).size()) {
                    allSelected = true;
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

        datafileInfoWindow = new ParameterWindow();
        setHeading("Datafile Window");
        setLayout(new RowLayout(Orientation.VERTICAL));
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        configs.add(datafileSelectModel.getColumn());

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
        view = new GroupingView();
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
        Grid<DatafileModel> grid = new Grid<DatafileModel>(dfmStore, cm);
        grid.setHeight("420px");
        grid.setView(view);
        grid.setBorders(true);
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<DatafileModel>>() {
            @Override
            public void handleEvent(GridEvent<DatafileModel> e) {
                DatafileModel datafile = e.getModel();
                EventPipeLine.getInstance().showParameterWindowWithHistory(datafile.getFacilityName(),
                        datafile.getId(), datafile.getName());
            }
        });

        grid.addPlugin(datafileSelectModel);
        grid.setSelectionModel(datafileSelectModel);
        ToolBar toolBar = new ToolBar();
        Button btnView = new Button(" Download", AbstractImagePrototype.create(Resource.ICONS.iconDownload()));
        btnView.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                download();
            }
        });
        toolBar.add(btnView);
        toolBar.add(new SeparatorToolItem());
        setTopComponent(toolBar);

        setLayout(new FitLayout());
        setSize(700, 560);

        // Pagination Bar
        pageBar = new PagingToolBar(PAGE_SIZE);
        pageBar.bind(loader);
        setBottomComponent(pageBar);

        add(grid);
        hasData = true; // ?? but no data is passed in in the constructor
    }

    /**
     * Set the datasets input which are used to get the datafiles corresponding
     * to each dataset and are displayed in the window.
     * 
     * @param datasetList
     *            an array of <code>DatasetModel</code>
     */
    public void setDatasets(ArrayList<DatasetModel> datasetList) {
        inputDatasetModels = datasetList;
        // This is the list of datasets selected to be viewed for datafiles.
        EventPipeLine.getInstance().setDialogBox("  Retrieving data...");
        EventPipeLine.getInstance().showDialogBox();
        utilityService.getDatafilesInDatasets(datasetList, new AsyncCallback<ArrayList<DatafileModel>>() {
            @Override
            public void onSuccess(ArrayList<DatafileModel> result) {
                EventPipeLine.getInstance().hideDialogBox();
                if (result.size() > 0) {
                    setDatafileList(result);
                    hasData = true;
                } else {
                    EventPipeLine.getInstance().showErrorDialog("No files returned");
                    hide();
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                EventPipeLine.getInstance().hideDialogBox();
                // TODO Auto-generated method stub
                dfmStore.removeAll();
                selectedFiles.clear();
                hasData = false;
            }
        });
    }

    /**
     * Set the datafile list to be displayed in the window.
     * 
     * @param datafileList
     *            an array of <code>DatafileModel</code>
     */
    public void setDatafileList(ArrayList<DatafileModel> datafileList) {
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
        pageBar.show();
    }

    /**
     * Get the history of this window.
     * 
     * @return the history string of this window
     */
    public String getHistoryString() {
        String history = "";
        history += HistoryManager.seperatorModel + HistoryManager.seperatorToken + "Model"
                + HistoryManager.seperatorKeyValues + "Dataset";
        int count = 0;
        for (DatasetModel dataset : inputDatasetModels) {
            history += HistoryManager.seperatorToken + "SN-" + count + HistoryManager.seperatorKeyValues
                    + dataset.getFacilityName();
            history += HistoryManager.seperatorToken + "DSId-" + count + HistoryManager.seperatorKeyValues
                    + dataset.getId();
            history += HistoryManager.seperatorToken + "DSName-" + count + HistoryManager.seperatorKeyValues
                    + dataset.getName();
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
        if (!hasData) {
            setDatasets(inputDatasetModels);
        }
        super.show();
    }

    public void reset() {
        inputDatasetModels.clear();
        dfmStore.removeAll();
        selectedFiles.clear();
        pageBar.hide();
    }

    /**
     * Download selected datafiles.
     */
    private void download() {
        if (selectedFiles.size() == 0) {
            EventPipeLine.getInstance().showMessageDialog("No files selected for download");
            return;
        }
        // check that we will be able to download all the files in the available
        // number of download frames
        if (selectedFiles.size() > (Constants.MAX_FILE_DOWNLOAD_PER_BATCH * Constants.MAX_DOWNLOAD_FRAMES)) {
            EventPipeLine.getInstance().showErrorDialog(
                    "Download request for " + selectedFiles.size() + " files exceeds maximum of "
                            + (Constants.MAX_FILE_DOWNLOAD_PER_BATCH * Constants.MAX_DOWNLOAD_FRAMES) + " files");
            return;
        }

        List<Long> selectedItems = new ArrayList<Long>(selectedFiles.keySet());
        @SuppressWarnings("unchecked")
        String facility = ((List<DatafileModel>) pageProxy.getData()).get(0).getFacilityName();
        int batchCount = 0;
        // get a download frame for each batch of data files
        while (selectedItems.size() > Constants.MAX_FILE_DOWNLOAD_PER_BATCH) {
            EventPipeLine.getInstance().downloadDatafiles(facility,
                    selectedItems.subList(0, Constants.MAX_FILE_DOWNLOAD_PER_BATCH));
            selectedItems.subList(0, Constants.MAX_FILE_DOWNLOAD_PER_BATCH).clear();
            batchCount = batchCount + 1;
        }
        EventPipeLine.getInstance().downloadDatafiles(facility, selectedItems);
        batchCount = batchCount + 1;
        if (batchCount > 1) {
            EventPipeLine
                    .getInstance()
                    .showMessageDialog(
                            "Your data is being retrieved from tape and will automatically start downloading shortly " +
                            "as " + batchCount + " files. The status of your download can be seen from the ‘My Downloads Tab’ " +
                            "(you may need to select ‘Show Previous Downloads’), or directly from " +
                            "https://srb.esc.rl.ac.uk/dataportal.");
            // "Download request sent to remote server. Files will be returned in "
            // + batchCount
            // + " batches. See My Downloads tab.");
        } else {
            EventPipeLine.getInstance().showMessageDialog(
                    "Your data is being retrieved from tape and will automatically start downloading shortly " +
                    "as a single file. The status of your download can be seen from the ‘My Downloads Tab’ " +
                    "(you may need to select ‘Show Previous Downloads’), or directly from " +
                    "https://srb.esc.rl.ac.uk/dataportal.");
//            "Download request sent to remote server. See My Downloads tab.");
        }
    }
}
