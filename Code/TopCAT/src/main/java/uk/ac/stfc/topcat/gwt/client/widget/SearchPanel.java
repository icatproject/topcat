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

import uk.ac.stfc.topcat.core.gwt.module.TAdvancedSearchDetails;
import uk.ac.stfc.topcat.gwt.client.KeywordsSuggestOracle;
import uk.ac.stfc.topcat.gwt.client.autocompletewidget.MultipleTextBox;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.callback.InvestigationSearchCallback;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;

/**
 * This is a composite widget for Search Panel used in the top level tab.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class SearchPanel extends Composite implements InvestigationSearchCallback {

    KeywordsSuggestOracle oracle;
    SuggestBox keywords;
    private MultipleTextBox multipleTextBox;
    private AdvancedSearchSubPanel advancedSearchSubPanel;
    private FacilitiesSearchSubPanel facilitiesSearchSubPanel;

    // Radio button for type of search
    RadioButton rdbtnSearchJustMy; // Search just my data
    RadioButton rdbtnSearchAllData; // Search All data

    WaitDialog waitDialog;
    private ListStore<TopcatInvestigation> investigationList = null;
    PagingModelMemoryProxy invPageProxy = null;
    PagingToolBar toolBar = null;
    Grid<TopcatInvestigation> grid;
    private EventPipeLine eventBus;

    public SearchPanel() {
        waitDialog = new WaitDialog();
        waitDialog.setMessage(" Searching...");
        waitDialog.hide();
        oracle = new KeywordsSuggestOracle();
        // investigationList = new ListStore<TopcatInvestigation>();
        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.setHorizontalAlign(HorizontalAlignment.CENTER);

        FlexTable flexTable = new FlexTable();
        flexTable.setCellSpacing(8);
        flexTable.setCellPadding(2);
        verticalPanel.add(flexTable);
        flexTable.setSize("800px", "20%");

        LabelField lblfldEywords = new LabelField("Keywords");
        flexTable.setWidget(0, 0, lblfldEywords);

        multipleTextBox = new MultipleTextBox();
        multipleTextBox.setWidth("361px");
        keywords = new SuggestBox(oracle, multipleTextBox);
        flexTable.setWidget(0, 1, keywords);

        Button btnSearch = new Button("Search");
        btnSearch.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
                // waitDialog.show();
                // Check whether to do searching my data or all data
                if (rdbtnSearchAllData.getValue()) { // Search all data
                    doSearchAllData();
                } else if (rdbtnSearchJustMy.getValue()) { // Search just my
                                                           // data
                    doSearchJustMyData();
                }
            }
        });
        flexTable.setWidget(0, 2, btnSearch);

        rdbtnSearchJustMy = new RadioButton("new name", "Search Just My Data");
        rdbtnSearchJustMy.setValue(true);
        rdbtnSearchJustMy.setHTML(" Search Just My Data");
        flexTable.setWidget(1, 0, rdbtnSearchJustMy);

        rdbtnSearchAllData = new RadioButton("new name", " Search All Data");
        flexTable.setWidget(1, 1, rdbtnSearchAllData);
        flexTable.getFlexCellFormatter().setColSpan(1, 1, 2);
        flexTable.getFlexCellFormatter().setColSpan(1, 0, 2);
        flexTable.getCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
        flexTable.getCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_CENTER);
        flexTable.getFlexCellFormatter().setColSpan(0, 1, 2);

        ContentPanel cntntpnlAdvancedSearch = new ContentPanel();
        cntntpnlAdvancedSearch.setTitleCollapse(true);
        cntntpnlAdvancedSearch.setFrame(true);
        cntntpnlAdvancedSearch.setExpanded(false);
        cntntpnlAdvancedSearch.setHeading("Advanced Search");
        cntntpnlAdvancedSearch.setCollapsible(true);

        advancedSearchSubPanel = new AdvancedSearchSubPanel();
        advancedSearchSubPanel.setInvSearchCallback(this);
        cntntpnlAdvancedSearch.add(advancedSearchSubPanel);
        advancedSearchSubPanel.setSize("99%", "100%");
        TableData td_cntntpnlAdvancedSearch = new TableData();
        td_cntntpnlAdvancedSearch.setHeight("100%");
        verticalPanel.add(cntntpnlAdvancedSearch, td_cntntpnlAdvancedSearch);
        cntntpnlAdvancedSearch.setSize("705px", "100%");
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        ContentPanel cntntpnlFacilitiesSearch = new ContentPanel();
        cntntpnlFacilitiesSearch.setTitleCollapse(true);
        cntntpnlFacilitiesSearch.setExpanded(false);
        cntntpnlFacilitiesSearch.setFrame(true);
        cntntpnlFacilitiesSearch.setHeading("Facilities Search");
        cntntpnlFacilitiesSearch.setCollapsible(true);

        facilitiesSearchSubPanel = new FacilitiesSearchSubPanel();
        cntntpnlFacilitiesSearch.add(facilitiesSearchSubPanel);
        facilitiesSearchSubPanel.setSize("99%", "100%");
        TableData td_cntntpnlFacilitiesSearch = new TableData();
        td_cntntpnlFacilitiesSearch.setHeight("100%");
        verticalPanel.add(cntntpnlFacilitiesSearch, td_cntntpnlFacilitiesSearch);
        cntntpnlFacilitiesSearch.setSize("705px", "100%");
        verticalPanel.add(new Text(""));

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
        PagingLoader<PagingLoadResult<TopcatInvestigation>> loader = new BasePagingLoader<PagingLoadResult<TopcatInvestigation>>(
                invPageProxy);
        loader.setRemoteSort(true);
        investigationList = new ListStore<TopcatInvestigation>(loader);

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
        grid.setSize("750px", "376px");
        verticalPanel.add(grid);
        grid.setBorders(true);

        // Pagination Bar
        toolBar = new PagingToolBar(15);
        toolBar.bind(loader);
        verticalPanel.add(toolBar);
        verticalPanel.setAutoWidth(true);
        initComponent(verticalPanel);
        verticalPanel.setSize("100%", "100%");
        setMonitorWindowResize(true);
    }

    /**
     * @return the advanced search sub panel
     */
    public AdvancedSearchSubPanel getAdvancedSearchSubPanel() {
        return advancedSearchSubPanel;
    }

    /**
     * @return the facilities search sub panel
     */
    public FacilitiesSearchSubPanel getFacilitiesSearchSubPanel() {
        return facilitiesSearchSubPanel;
    }

    /**
     * This method is an callback for searching just user investigation using
     * basic keywords.
     */
    public void doSearchJustMyData() {
        TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
        searchDetails.setKeywords(multipleTextBox.getMultiText());
        eventBus.searchForMyInvestigation(searchDetails);
    }

    /**
     * This method is an callback for searches for all investigations using
     * basic keywords.
     */
    public void doSearchAllData() {
        TAdvancedSearchDetails searchDetails = new TAdvancedSearchDetails();
        searchDetails.setKeywords(multipleTextBox.getMultiText());
        eventBus.searchForInvestigation(searchDetails);
    }

    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * This method is the callback for the searching for investigations with the
     * input search parameters. Submits an AJAX call. on success the table is
     * set with the results.
     */
    @Override
    public void searchForInvestigation(TAdvancedSearchDetails searchDetails) {
        // This method does the search and add the results to the investigation
        // list
        // Add keywords to the search
        searchDetails.setKeywords(multipleTextBox.getMultiText());
        eventBus.searchForInvestigation(searchDetails);
    }

    /**
     * This method sets the result investigations that will be displayed in the
     * results table.
     * 
     * @param invList
     *            list of investigations
     */
    public void setInvestigations(ArrayList<TopcatInvestigation> invList) {
        invPageProxy.setData(invList);
        toolBar.refresh();
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
