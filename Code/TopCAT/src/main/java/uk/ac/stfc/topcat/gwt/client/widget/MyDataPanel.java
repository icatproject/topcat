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
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import uk.ac.stfc.topcat.gwt.client.Resource;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.event.AddInvestigationDetailsEvent;
import uk.ac.stfc.topcat.gwt.client.event.AddMyInvestigationEvent;
import uk.ac.stfc.topcat.gwt.client.event.LogoutEvent;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInvestigationDetailsEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.AddMyInvestigationEventHandler;
import uk.ac.stfc.topcat.gwt.client.eventHandler.LogoutEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;

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
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

import java.util.List;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;

/**
 * This widget shows a tree to browse user data (User is the investigator). the
 * hierarchy is -- Facility -- Instrument -- Investigation -- Dataset or
 * Datafile -- Datafile
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class MyDataPanel extends Composite {

    TreePanel<ICATNode> treeGrid;
    HashMap<String, ArrayList<ICATNode>> logfilesMap = new HashMap<String, ArrayList<ICATNode>>();
    private ListStore<TopcatInvestigation> investigationList = null;
    PagingModelMemoryProxy invPageProxy = null;
    PagingToolBar toolBar = null;
    Grid<TopcatInvestigation> grid;
    private EventPipeLine eventBus;
    private boolean refreshData = true;
    private VerticalPanel investigationPanel;
    private InvestigationSubPanel investigationSubPanel;
    private static final String SOURCE = "MyDataPanel";

    public MyDataPanel() {
        LayoutContainer mainContainer = new LayoutContainer();
        mainContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        ContentPanel contentPanel = new ContentPanel();
        contentPanel.setHeaderVisible(false);
        contentPanel.setCollapsible(true);
        contentPanel.setLayout(new RowLayout(Orientation.VERTICAL));

        VerticalPanel bodyPanel = new VerticalPanel();
        bodyPanel.setHorizontalAlign(HorizontalAlignment.CENTER);

        ToolBar toolMenuBar = new ToolBar();
        ButtonBar buttonBar = new ButtonBar();

        Button btnDownload = new Button("Get Investigations");
        btnDownload.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                // Collect list of investigations
                EventPipeLine.getInstance().getMyInvestigationsInMyDataPanel();
            }
        });
        buttonBar.add(btnDownload);
        toolMenuBar.add(buttonBar);
        contentPanel.setTopComponent(toolMenuBar);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig clmncnfgServerName = new ColumnConfig("serverName", "Facility Name", 150);
        configs.add(clmncnfgServerName);

        ColumnConfig clmncnfgInvestigationNumber = new ColumnConfig("investigationName", "Investigation Number", 150);
        configs.add(clmncnfgInvestigationNumber);

        ColumnConfig clmncnfgVisitId = new ColumnConfig("visitId", "Visit Id", 150);
        configs.add(clmncnfgVisitId);

        ColumnConfig clmncnfgTitle = new ColumnConfig("title", "Title", 150);
        configs.add(clmncnfgTitle);

        ColumnConfig clmncnfgStartDate = new ColumnConfig("startDate", "Start Date", 150);
        clmncnfgStartDate.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));
        configs.add(clmncnfgStartDate);

        ColumnConfig clmncnfgEndDate = new ColumnConfig("endDate", "End Date", 150);
        clmncnfgEndDate.setDateTimeFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT));
        configs.add(clmncnfgEndDate);

        // Pagination
        invPageProxy = new PagingModelMemoryProxy(investigationList);
        PagingLoader<PagingLoadResult<ICATNode>> loader = new BasePagingLoader<PagingLoadResult<ICATNode>>(invPageProxy);
        loader.setRemoteSort(true);
        investigationList = new ListStore<TopcatInvestigation>(loader);
        // WARN: Don't remove this line otherwise the button images don't work
        // on page toolbar
        GXT.isHighContrastMode = false;
        grid = new Grid<TopcatInvestigation>(investigationList, new ColumnModel(configs));
        grid.setAutoExpandColumn("title");
        grid.setAutoExpandMin(200);
        grid.setMinColumnWidth(100);
        grid.setToolTip("\"Double Click\" row to show invetigation details, right click for more options");
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<TopcatInvestigation>>() {
            @Override
            public void handleEvent(GridEvent<TopcatInvestigation> e) {
                eventBus.getInvestigationDetails(e.getModel().getFacilityName(), e.getModel().getInvestigationId(),
                        SOURCE);
            }
        });

        // Context Menu
        Menu contextMenu = new Menu();
        contextMenu.setWidth(160);
        MenuItem showInvestigation = new MenuItem();
        showInvestigation.setText("show investigation details");
        showInvestigation.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconView()));
        contextMenu.add(showInvestigation);
        showInvestigation.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                eventBus.getInvestigationDetails(grid.getSelectionModel().getSelectedItem().getFacilityName(), grid
                        .getSelectionModel().getSelectedItem().getInvestigationId(), SOURCE);
            }
        });
        MenuItem showDS = new MenuItem();
        showDS.setText("show data sets");
        showDS.setIcon(AbstractImagePrototype.create(Resource.ICONS.iconView()));
        contextMenu.add(showDS);
        showDS.addSelectionListener(new SelectionListener<MenuEvent>() {
            public void componentSelected(MenuEvent ce) {
                eventBus.showDatasetWindowWithHistory(grid.getSelectionModel().getSelectedItem().getFacilityName(),
                        grid.getSelectionModel().getSelectedItem().getInvestigationId(), grid.getSelectionModel()
                                .getSelectedItem().getInvestigationTitle());
            }
        });
        grid.setContextMenu(contextMenu);

        grid.setAutoHeight(true);
        bodyPanel.add(grid);
        contentPanel.add(bodyPanel, new RowData(Style.DEFAULT, 376.0, new Margins()));

        // Pagination Bar
        toolBar = new PagingToolBar(15) {
            @Override
            public void refresh() {
                super.refresh();
                if (refreshData) {
                    // Collect list of investigations
                    EventPipeLine.getInstance().getMyInvestigationsInMyDataPanel();
                }
            }
        };
        toolBar.bind(loader);
        contentPanel.setBottomComponent(toolBar);

        mainContainer.add(contentPanel);

        // Investigation detail
        investigationPanel = new VerticalPanel();
        investigationPanel.setBorders(true);
        investigationPanel.setSpacing(20);
        investigationPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        investigationSubPanel = new InvestigationSubPanel();
        TableData td_investigationContentPanel = new TableData();
        td_investigationContentPanel.setHeight("100%");
        td_investigationContentPanel.setWidth("705px");
        investigationPanel.add(investigationSubPanel, td_investigationContentPanel);
        investigationPanel.hide();
        mainContainer.add(investigationPanel);

        initComponent(mainContainer);
        setMonitorWindowResize(true);

        createAddInvestigationDetailsHandler();
        createAddMyInvestigationHandler();
        createLogoutHandler();
    }

    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
        investigationSubPanel.setEventBus(eventBus);
    }

    /**
     * This method sets the width of the search results table.
     * 
     * @param width
     */
    public void setGridWidth(int width) {
        grid.setWidth(width);
    }

    /**
     * Setup a handler to react to AddMyInvestigation events.
     */
    private void createAddMyInvestigationHandler() {
        // react to a new set of investigations being added
        AddMyInvestigationEvent.register(EventPipeLine.getEventBus(), new AddMyInvestigationEventHandler() {
            @Override
            public void addMyInvestigations(AddMyInvestigationEvent event) {
                clearInvestigationList(event.getFacilityName());
                @SuppressWarnings("unchecked")
                List<TopcatInvestigation> investList = (List<TopcatInvestigation>) invPageProxy.getData();
                if (investList != null) {
                    investList.addAll(event.getMyInvestigations());
                } else {
                    investList = new ArrayList<TopcatInvestigation>();
                    investList.addAll(event.getMyInvestigations());
                }
                invPageProxy.setData(investList);
                refreshData = false;
                toolBar.refresh();
                refreshData = true;
            }
        });
    }

    /**
     * Setup a handler to react to add investigation details events.
     */
    private void createAddInvestigationDetailsHandler() {
        AddInvestigationDetailsEvent.registerToSource(EventPipeLine.getEventBus(), SOURCE,
                new AddInvestigationDetailsEventHandler() {
                    @Override
                    public void addInvestigationDetails(AddInvestigationDetailsEvent event) {
                        investigationSubPanel.setInvestigation(event.getInvestigation());
                        investigationPanel.show();
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
                clearInvestigationList(event.getFacilityName());
                String invDetailsFacility = investigationSubPanel.getFacilityName();
                if (!(invDetailsFacility == null) && invDetailsFacility.equalsIgnoreCase(event.getFacilityName())) {
                    investigationPanel.hide();
                    investigationSubPanel.reset();
                }
            }
        });
    }

    /**
     * Remove all investigations for the given facility.
     * 
     * @param facilityName
     */
    private void clearInvestigationList(String facilityName) {
        @SuppressWarnings("unchecked")
        List<TopcatInvestigation> investList = (List<TopcatInvestigation>) invPageProxy.getData();
        if (investList != null) {
            for (Iterator<TopcatInvestigation> it = investList.iterator(); it.hasNext();) {
                if (it.next().getFacilityName().equals(facilityName)) {
                    it.remove();
                }
            }
            invPageProxy.setData(investList);
            refreshData = false;
            toolBar.refresh();
            refreshData = true;
        }
    }
}
