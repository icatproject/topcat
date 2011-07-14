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
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;

import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.PagingModelMemoryProxy;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.i18n.client.DateTimeFormat;
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

    public MyDataPanel() {
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);
        // verticalPanel.setLayoutOnChange(true);
        // verticalPanel.setAutoWidth(true);
        // verticalPanel.setAutoHeight(true);
        // verticalPanel.setSize("100%","100%");
        // verticalPanel.setLayout(new RowLayout(Orientation.HORIZONTAL));
        verticalPanel.setBorders(true);

        ToolBar toolMenuBar = new ToolBar();
        verticalPanel.add(toolMenuBar);
        ButtonBar buttonBar = new ButtonBar();

        Button btnDownload = new Button("Get Investigations");
        btnDownload.addSelectionListener(new SelectionListener<ButtonEvent>() {

            public void componentSelected(ButtonEvent ce) {
                // Collect list of investigations
                EventPipeLine.getInstance().getMyInvestigationsInMyDataPanel();
            }
        });
        buttonBar.add(btnDownload);
        toolMenuBar.add(buttonBar);

        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ColumnConfig clmncnfgServerName = new ColumnConfig("serverName", "Facility Name", 150);
        configs.add(clmncnfgServerName);

        ColumnConfig clmncnfgInvestigationNumber = new ColumnConfig("investigationName", "Investigation Number", 150);
        clmncnfgInvestigationNumber.setToolTip("\"Double Click\" to show datasets");
        configs.add(clmncnfgInvestigationNumber);

        ColumnConfig clmncnfgVisitId = new ColumnConfig("visitId", "Visit Id", 150);
        clmncnfgVisitId.setToolTip("\"Double Click\" to show datasets");
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
        grid.addListener(Events.RowDoubleClick, new Listener<GridEvent<TopcatInvestigation>>() {

            public void handleEvent(GridEvent<TopcatInvestigation> e) {
                TopcatInvestigation inv = (TopcatInvestigation) e.getModel();
                eventBus.showDatasetWindowWithHistory(inv.getFacilityName(), inv.getInvestigationId(),
                        inv.getInvestigationTitle());
            }
        });
        grid.setSize("800px", "376px");
        verticalPanel.add(grid);
        grid.setBorders(true);

        // Pagination Bar
        toolBar = new PagingToolBar(15);
        toolBar.bind(loader);
        verticalPanel.add(toolBar);
        verticalPanel.setAutoWidth(true);
        setMonitorWindowResize(true);

        initComponent(verticalPanel);
    }

    public void clearInvestigationList() {
        invPageProxy.setData(new ArrayList<TopcatInvestigation>());
        toolBar.refresh();
    }

    /**
     * Remove all investigations for the given facility.
     * 
     * @param facilityName
     */
    public void clearInvestigationList(String facilityName) {
        @SuppressWarnings("unchecked")
        List<TopcatInvestigation> investList = (List<TopcatInvestigation>) invPageProxy.getData();
        for (Iterator<TopcatInvestigation> it = investList.iterator(); it.hasNext();) {
            if (it.next().getFacilityName().equals(facilityName)) {
                it.remove();
            }
        }
        invPageProxy.setData(investList);
        toolBar.refresh();
    }

    /**
     * This method sets the result investigations that will be displayed in the
     * results table.
     * 
     * @param invList
     *            list of investigations
     */
    public void addInvestigations(ArrayList<TopcatInvestigation> invList) {
        @SuppressWarnings("unchecked")
        List<TopcatInvestigation> investList = (List<TopcatInvestigation>) invPageProxy.getData();
        if (investList != null) {
            investList.addAll(invList);
        } else {
            investList = new ArrayList<TopcatInvestigation>();
            investList.addAll(invList);
        }
        invPageProxy.setData(investList);
        toolBar.refresh();
    }

    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * This method sets the width of the search results table.
     * 
     * @param width
     */
    public void setGridWidth(int width) {
        grid.setWidth(width);
    }
}
