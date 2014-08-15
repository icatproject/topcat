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
import java.util.Iterator;
import java.util.List;

import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddMyDownloadEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.event.UpdateDownloadStatusEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddMyDownloadEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.UpdateDownloadStatusEventHandler;
import uk.ac.stfc.topcat.gwt.client.manager.DownloadManager;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;

/**
 * This widget displays the download requests.
 *
 */
public class MyDownloadPanel extends Composite {
    private TabPanel mainPanel;
    private TabItem myDownloadTab;
    private Grid<DownloadModel> grid;
    private PagingModelMemoryProxy proxy = new PagingModelMemoryProxy(new ArrayList<DownloadModel>());
    private PagingToolBar toolBar = null;
    private EventPipeLine eventBus;
    private PagingLoader<PagingLoadResult<DownloadModel>> loader;
    private boolean remoteRefresh = false;

    public MyDownloadPanel(TabPanel tabPanel, TabItem myDownloadTabItem) {
        myDownloadTab = myDownloadTabItem;
        mainPanel = tabPanel;

        GridCellRenderer<DownloadModel> statusRenderer = new GridCellRenderer<DownloadModel>() {
            @Override
            public Object render(DownloadModel model, String property,
                    ColumnData config, int rowIndex, int colIndex,
                    ListStore<DownloadModel> store, Grid<DownloadModel> grid) {

                Label status = new Label(model.getStatus());
                if (model.getStatus().equalsIgnoreCase(Constants.STATUS_ERROR) || model.getStatus().equalsIgnoreCase(Constants.STATUS_EXPIRED)) {
                    //status.setToolTip(model.getMessage() + ". This download will will remove from " + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT).format(model.getExpiryTime()) + "."); //breaks grid for some reason
                    status.setToolTip(model.getMessage());
                }

                return status;
            }
        };

        GridCellRenderer<DownloadModel> buttonRenderer = new GridCellRenderer<DownloadModel>() {
            //private boolean init;
            @Override
            public Object render(final DownloadModel model, String property, ColumnData config, final int rowIndex,
                    final int colIndex, ListStore<DownloadModel> store, Grid<DownloadModel> grid) {

                Button b = new Button((String) model.get(property), new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        if (model.getStatus().equalsIgnoreCase(Constants.STATUS_AVAILABLE)) {
                            DownloadManager.getInstance().download(model.getFacilityName(), model.getUrl());
                        }
                    }
                });

                if (model.getStatus().equalsIgnoreCase(Constants.STATUS_AVAILABLE)) {
                    b.setEnabled(true);
                } else if (model.getStatus().equalsIgnoreCase(Constants.STATUS_ERROR)) {
                    b.setEnabled(false);
                } else if (model.getStatus().equalsIgnoreCase(Constants.STATUS_EXPIRED)) {
                    b.setEnabled(false);
                } else if (model.getStatus().equalsIgnoreCase(Constants.STATUS_IN_PROGRESS)){
                    b.setEnabled(false);
                }

                b.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);
                b.setText(Constants.DOWNLOAD);
                b.setToolTip("Click to start download");
                return b;
            }
        };


        GridCellRenderer<DownloadModel> deleteButtonRenderer = new GridCellRenderer<DownloadModel>() {
            @Override
            public Object render(final DownloadModel model, String property, ColumnData config, final int rowIndex,
                    final int colIndex, ListStore<DownloadModel> store, Grid<DownloadModel> grid) {

                Button deleteButton = new Button(Constants.DELETE, new SelectionListener<ButtonEvent>() {
                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        DownloadManager.getInstance().deleteDownload(model.getFacilityName(), model);
                    }
                });

                deleteButton.setWidth(grid.getColumnModel().getColumnWidth(colIndex) - 10);

                return deleteButton;
            }
        };

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.setCollapsible(true);
        contentPanel.setLayout(new RowLayout(Orientation.VERTICAL));

        VerticalPanel bodyPanel = new VerticalPanel();
        bodyPanel.setHorizontalAlign(HorizontalAlignment.CENTER);

        List<ColumnConfig> configs = getColumnConfig(buttonRenderer, statusRenderer, deleteButtonRenderer);

        // Pagination
        loader = new BasePagingLoader<PagingLoadResult<DownloadModel>>(proxy);
        loader.setRemoteSort(true);
        ListStore<DownloadModel> store = new ListStore<DownloadModel>(loader);
        store.sort("submitTime", Style.SortDir.DESC);

        grid = new Grid<DownloadModel>(store, new ColumnModel(configs));
        grid.setAutoExpandColumn("downloadName");
        grid.setAutoExpandMin(200);
        grid.setMinColumnWidth(100);
        grid.setHeight("490px");
        bodyPanel.add(grid);
        contentPanel.add(bodyPanel);

        // Pagination Bar
        toolBar = getToolBar();
        toolBar.bind(loader);
        contentPanel.setBottomComponent(toolBar);

        setMonitorWindowResize(true);
        initComponent(contentPanel);
        createAddMyDownloadHandler();
        createUpdateDownloadStatusHandler();
        createTabSelectedHandler();
        createLogoutHandler();
    }

    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * This method sets the width of the my downloads table.
     *
     * @param width
     */
    public void setGridWidth(int width) {
        grid.setWidth(width);
    }

    /**
     * Refresh.
     *
     */
    public void refresh() {
        toolBar.refresh();
    }

    /**
     * Get the list of column configurations. This includes column names and
     * widths
     *
     * @param buttonRenderer
     * @return a list of <code>ColumnConfig</code>
     */
    private List<ColumnConfig> getColumnConfig(GridCellRenderer<DownloadModel> buttonRenderer, GridCellRenderer<DownloadModel> statusRenderer, GridCellRenderer<DownloadModel> deleteButtonRenderer) {
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig column;
        column = new ColumnConfig("facilityName", "Facility Name", 150);
        configs.add(column);

        column = new ColumnConfig("submitTime", "Submit Time", 150);
        column.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));
        configs.add(column);

        column = new ColumnConfig("downloadName", "Download Name", 150);
        configs.add(column);

        column = new ColumnConfig("status", "Status", 150);
        column.setRenderer(statusRenderer);
        configs.add(column);

        column = new ColumnConfig("downloadAvailable", "Download", 100);
        column.setRenderer(buttonRenderer);
        configs.add(column);

        column = new ColumnConfig("delete", "Delete", 100);
        column.setRenderer(deleteButtonRenderer);
        configs.add(column);


        return configs;
    }

    /**
     * Get the paging tool bar. Refresh will call out to the server.
     */
    private PagingToolBar getToolBar() {
        PagingToolBar toolBar = new PagingToolBar(15) {
            @Override
            public void refresh() {
                if (remoteRefresh) {
                    updateDownloadList();
                } else {
                    remoteRefresh = true;
                }
                super.refresh();
            }
        };
        return toolBar;
    }

    /**
     * Remove all downloads for the given facility.
     *
     * @param facilityName
     */
    private void clearDownloadList(String facilityName) {
        @SuppressWarnings("unchecked")
        List<DownloadModel> downloadList = (List<DownloadModel>) proxy.getData();
        if (downloadList != null) {
            for (Iterator<DownloadModel> it = downloadList.iterator(); it.hasNext();) {
                if (it.next().getFacilityName().equals(facilityName)) {
                    it.remove();
                }
            }
            proxy.setData(downloadList);
        }
    }

    /**
     * Add the list of download models to the store.
     *
     * @param dlms
     *            the list of download models to add
     */
    private void loadDownloads(List<DownloadModel> dlms) {
        @SuppressWarnings("unchecked")
        List<DownloadModel> downloadList = (List<DownloadModel>) proxy.getData();
        if (downloadList == null) {
            downloadList = new ArrayList<DownloadModel>();
        }
        downloadList.addAll(dlms);
        proxy.setData(downloadList);
        loader.load();
    }

    /**
     * Call the server to check the statuses of all of the downloads. This will
     * in turn result in an UpdateDownloadStatusEvent being fired.
     */
    private void updateDownloadList() {
        DownloadManager.getInstance().getMyDownloads(eventBus.getLoggedInFacilities());
    }

    /**
     * Setup a handler to react to AddMyDownload events.
     */
    private void createAddMyDownloadHandler() {
        // react to a new set of downloads being added
        AddMyDownloadEvent.register(EventPipeLine.getEventBus(), new AddMyDownloadEventHandler() {
            @Override
            public void addMyDownloads(AddMyDownloadEvent event) {
                loadDownloads(event.getMyDownloads());
                toolBar.refresh();
            }
        });
    }

    /**
     * Setup a handler to react to UpdateDownloadStatus events.
     */
    private void createUpdateDownloadStatusHandler() {
        // react to the download statuses being updated
        UpdateDownloadStatusEvent.register(EventPipeLine.getEventBus(), new UpdateDownloadStatusEventHandler() {
            @Override
            public void updateDownloadStatus(UpdateDownloadStatusEvent event) {
                proxy.setData(event.getDownloads());
                loader.load();
                remoteRefresh = false;
                toolBar.refresh();
            }
        });
    }

    /**
     * Setup a listener to react to Select events.
     */
    private void createTabSelectedHandler() {
        // When the tab is changed update the download info
        mainPanel.addListener(Events.Select, new Listener<TabPanelEvent>() {
            @Override
            public void handleEvent(TabPanelEvent event) {
                if (event.getItem() == myDownloadTab) {
                    updateDownloadList();
                }
            }
        });
    }

    /**
     * Setup a handler to react to Logout events.
     */
    private void createLogoutHandler() {
        LogoutEvent.register(EventPipeLine.getEventBus(), new LogoutEventHandler() {
            @Override
            public void logout(LogoutEvent event) {
                clearDownloadList(event.getFacilityName());
                remoteRefresh = false;
                toolBar.refresh();
            }
        });
    }
}
