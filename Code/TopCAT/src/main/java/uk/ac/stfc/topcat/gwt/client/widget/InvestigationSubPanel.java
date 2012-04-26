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

import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigator;
import uk.ac.stfc.topcat.core.gwt.module.TPublication;
import uk.ac.stfc.topcat.core.gwt.module.TShift;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;

import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class InvestigationSubPanel extends Composite {
    private TopcatInvestigation investigationModel = new TopcatInvestigation();
    private ContentPanel contentPanel;
    private LayoutContainer contentlPanelBody;
    private LabelField dataTitle;
    private LabelField dataFacility;
    private LabelField dataInvestigationNumber;
    private LabelField lableFacility;
    private LabelField lableTitle;
    private LabelField lableInvestigationNumber;
    private LabelField lableVisitId;
    private LabelField dataVisitId;
    private LabelField lableInvestigators;
    private LabelField lableProposal;
    private LabelField dataProposal;
    private Button btnShowDataSets;
    private LayoutContainer topContainer;
    private LabelField lableShifts;
    private LabelField lablePublications;
    private FlexTable shiftsTable;
    private FlexTable namesTable;
    private FlexTable publicationsTable;
    private EventPipeLine eventBus;
    private LabelField lableStartDate;
    private LabelField dataStartDate;
    private LabelField lableEndDate;
    private LabelField dataEndDate;
    private HorizontalPanel buttonPanel;
    private LabelField lableProperties;
    private FlexTable propertiesTable;
    private LayoutContainer shiftsContainer;
    private LayoutContainer publicationsContainer;
    private LayoutContainer investigatorsContainer;
    private LayoutContainer proposalContainer;
    private LayoutContainer propertiesContainer;

    /**
     * Constructor
     */
    public InvestigationSubPanel() {
        contentPanel = new ContentPanel();
        contentPanel.setTitleCollapse(true);
        contentPanel.setFrame(true);
        contentPanel.setExpanded(false);
        contentPanel.setHeading("Investigation");
        contentPanel.setCollapsible(true);
        contentPanel.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        contentPanel.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });

        contentlPanelBody = new LayoutContainer();
        TableLayout tl_contentlPanelBody = new TableLayout();
        tl_contentlPanelBody.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_contentlPanelBody.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        tl_contentlPanelBody.setWidth("100%");
        contentlPanelBody.setLayout(tl_contentlPanelBody);

        topContainer = new LayoutContainer();
        TableLayout tl_topContainer = new TableLayout(2);
        tl_topContainer.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_topContainer.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        tl_topContainer.setCellSpacing(5);
        topContainer.setLayout(tl_topContainer);

        buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        TableData td_buttonPanel = new TableData();
        td_buttonPanel.setVerticalAlign(VerticalAlignment.TOP);
        td_buttonPanel.setHorizontalAlign(HorizontalAlignment.LEFT);
        td_buttonPanel.setColspan(2);

        btnShowDataSets = new Button("Show Data Sets");
        btnShowDataSets
                .setToolTip("Click to open a window containing the list of data sets associated with this investigation");
        btnShowDataSets.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                eventBus.showDatasetWindowWithHistory(investigationModel.getFacilityName(),
                        investigationModel.getInvestigationId(), investigationModel.getInvestigationTitle());
            }
        });
        buttonPanel.add(btnShowDataSets);

        // Additional buttons
        // "Download Data Sets"
        // "Download Investigation Metadata"
        // "Edit Permissions"
        
        topContainer.add(buttonPanel, td_buttonPanel);
        buttonPanel.setHeight("50px");

        lableFacility = new LabelField("Facility: ");
        topContainer.add(lableFacility);

        dataFacility = new LabelField("Facility");
        topContainer.add(dataFacility);

        lableTitle = new LabelField("Title: ");
        topContainer.add(lableTitle);

        dataTitle = new LabelField("Title");
        topContainer.add(dataTitle);

        lableInvestigationNumber = new LabelField("Investigation No: ");
        TableData td_lableInvestigationNumber = new TableData();
        td_lableInvestigationNumber.setWidth("105");
        topContainer.add(lableInvestigationNumber, td_lableInvestigationNumber);

        dataInvestigationNumber = new LabelField("Investigation Number");
        topContainer.add(dataInvestigationNumber);

        lableVisitId = new LabelField("Visit Id: ");
        topContainer.add(lableVisitId);

        dataVisitId = new LabelField("Visit Id");
        topContainer.add(dataVisitId);

        lableStartDate = new LabelField("Start Date:");
        topContainer.add(lableStartDate);

        dataStartDate = new LabelField("Start Date");
        topContainer.add(dataStartDate);

        lableEndDate = new LabelField("End Date:");
        topContainer.add(lableEndDate);

        dataEndDate = new LabelField("End Date");
        topContainer.add(dataEndDate);

        contentlPanelBody.add(topContainer);
        topContainer.setSize("100%", "100%");

        // Shifts
        shiftsContainer = new LayoutContainer();
        TableLayout tl_shiftsContainer = new TableLayout(2);
        tl_shiftsContainer.setCellSpacing(5);
        tl_shiftsContainer.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_shiftsContainer.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        shiftsContainer.setLayout(tl_shiftsContainer);

        lableShifts = new LabelField("Shifts:");
        TableData td_lableShifts = new TableData();
        td_lableShifts.setWidth("105");
        shiftsContainer.add(lableShifts, td_lableShifts);

        shiftsTable = new FlexTable();
        shiftsTable.setBorderWidth(1);
        shiftsContainer.add(shiftsTable);

        shiftsTable.getColumnFormatter().setWidth(0, "120px");
        shiftsTable.getColumnFormatter().setWidth(1, "120px");
        contentlPanelBody.add(shiftsContainer);

        // Investigators
        investigatorsContainer = new LayoutContainer();
        TableLayout tl_investigatorsContainer = new TableLayout(2);
        tl_investigatorsContainer.setCellSpacing(5);
        tl_investigatorsContainer.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_investigatorsContainer.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        investigatorsContainer.setLayout(tl_investigatorsContainer);

        lableInvestigators = new LabelField("Investigators:");
        TableData td_lableInvestigators = new TableData();
        td_lableInvestigators.setWidth("105");
        investigatorsContainer.add(lableInvestigators, td_lableInvestigators);

        namesTable = new FlexTable();
        investigatorsContainer.add(namesTable);
        contentlPanelBody.add(investigatorsContainer);

        // Proposals
        proposalContainer = new LayoutContainer();
        proposalContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        lableProposal = new LabelField("Proposal:");
        proposalContainer.add(lableProposal, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        dataProposal = new LabelField("Proposal");
        proposalContainer.add(dataProposal, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));

        contentlPanelBody.add(proposalContainer);

        // Publications
        publicationsContainer = new LayoutContainer();
        publicationsContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        lablePublications = new LabelField("Publications:");
        publicationsContainer.add(lablePublications,
                new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        publicationsTable = new FlexTable();
        publicationsContainer
                .add(publicationsTable, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));
        contentlPanelBody.add(publicationsContainer);

        // Properties
        propertiesContainer = new LayoutContainer();
        propertiesContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        lableProperties = new LabelField("Properties:");
        propertiesContainer.add(lableProperties, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        propertiesTable = new FlexTable();
        propertiesTable.setBorderWidth(1);
        propertiesContainer.add(propertiesTable, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));
        contentlPanelBody.add(propertiesContainer);

        contentPanel.add(contentlPanelBody);
        initComponent(contentPanel);
        initDataBindings();
    }

    /**
     * Get the facility name.
     * 
     * @return the facility name
     */
    public String getFacilityName() {
        return dataFacility.getText();
    }

    /**
     * Erase all data and hide.
     */
    public void reset() {
        investigationModel = new TopcatInvestigation();
        shiftsContainer.hide();
        shiftsTable.removeAllRows();
        investigatorsContainer.hide();
        namesTable.removeAllRows();
        proposalContainer.hide();
        publicationsContainer.hide();
        publicationsTable.removeAllRows();
        propertiesContainer.hide();
        propertiesTable.removeAllRows();
    }

    /**
     * Set the investigation.
     * 
     * @param inv
     *            the investigation
     */
    public void setInvestigation(TInvestigation inv) {
        // For a number of field we have to use the LableField in order to be
        // able to bind to them. As a consequence we want to display the rest of
        // the data as LableFields rather than Text in order to maintain
        // consistent formatting.
        reset();
        contentPanel.expand();
        investigationModel = new TopcatInvestigation(inv.getServerName(), inv.getInvestigationId(),
                inv.getInvestigationName(), inv.getTitle(), inv.getVisitId(), inv.getStartDate(), inv.getEndDate(),
                inv.getProposal());

        // Shifts
        if (inv.getShifts().size() > 0) {
            LabelField startDate = new LabelField("Start Date");
            startDate.setWidth(125);
            shiftsTable.setWidget(0, 0, startDate);
            shiftsTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

            LabelField endDate = new LabelField("End Date");
            endDate.setWidth(125);
            shiftsTable.setWidget(0, 1, endDate);

            shiftsTable.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);

            shiftsTable.setWidget(0, 2, new LabelField("Comment"));
            shiftsTable.getCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
            int i = 1;
            for (TShift shift : inv.getShifts()) {
                shiftsTable.setWidget(i, 0,
                        new LabelField(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT)
                                .format(shift.getStartDate())));
                shiftsTable.getCellFormatter().setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER);

                shiftsTable.setWidget(i, 1,
                        new LabelField(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT)
                                .format(shift.getEndDate())));
                shiftsTable.getCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
                shiftsTable.setWidget(i, 2, new LabelField(shift.getComment()));
                shiftsTable.getCellFormatter().setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_CENTER);
            }
            shiftsContainer.show();
        }

        // Investigators
        if (inv.getInvestigators().size() > 0) {
            int i = 0;
            for (TInvestigator tinvestigator : inv.getInvestigators()) {
                HorizontalPanel investigatorPanel = new HorizontalPanel();
                investigatorPanel.add(new LabelField(tinvestigator.getRole()));
                investigatorPanel.add(new LabelField("&nbsp;-&nbsp;"));
                investigatorPanel.add(new LabelField(tinvestigator.getFullName()));
                namesTable.setWidget(i, 0, investigatorPanel);
                i++;
            }
            investigatorsContainer.show();
        }

        // Proposal
        if (!(inv.getProposal() == null) && !(inv.getProposal().isEmpty())) {
            proposalContainer.show();
        }

        // Publications
        if (inv.getPublications().size() > 0) {
            int i = 0;
            for (TPublication publication : inv.getPublications()) {
                if (publication.getUrl() == null || publication.getUrl().isEmpty()) {
                    publicationsTable.setWidget(i, 0, new LabelField(publication.getFullReference()));
                } else {
                    publicationsTable.setHTML(i, 0,
                            "<a href=\"" + publication.getUrl() + "\">" + publication.getFullReference() + "</a>");
                }
                i++;
            }
            publicationsContainer.show();
        }

        // Parameters
        if (!(inv.getParamName() == null) && !(inv.getParamName().isEmpty())) {
            propertiesTable.setWidget(0, 0, new LabelField(inv.getParamName()));
            propertiesTable.setWidget(0, 1, new LabelField(inv.getParamValue()));
            propertiesContainer.show();
        }

        initDataBindings();
    }

    /**
     * Set the event bus.
     * 
     * @param eventBus
     *            the event bus
     */
    public void setEventBus(EventPipeLine eventBus) {
        this.eventBus = eventBus;
    }

    protected void initDataBindings() {
        FieldBinding fieldBinding = new FieldBinding(dataFacility, "serverName");
        fieldBinding.bind(investigationModel);
        //
        FieldBinding fieldBinding_1 = new FieldBinding(dataTitle, "investigationName");
        fieldBinding_1.bind(investigationModel);
        //
        FieldBinding fieldBinding_2 = new FieldBinding(dataVisitId, "visitId");
        fieldBinding_2.bind(investigationModel);
        //
        FieldBinding fieldBinding_3 = new FieldBinding(dataProposal, "proposal");
        fieldBinding_3.bind(investigationModel);
        //
        FieldBinding fieldBinding_4 = new FieldBinding(dataTitle, "title");
        fieldBinding_4.bind(investigationModel);
        //
        FieldBinding fieldBinding_5 = new FieldBinding(dataInvestigationNumber, "investigationName");
        fieldBinding_5.bind(investigationModel);
        //
        FieldBinding fieldBinding_6 = new FieldBinding(dataStartDate, "formatedStartDate");
        fieldBinding_6.bind(investigationModel);
        //
        FieldBinding fieldBinding_7 = new FieldBinding(dataEndDate, "formatedEndDate");
        fieldBinding_7.bind(investigationModel);
    }
}
