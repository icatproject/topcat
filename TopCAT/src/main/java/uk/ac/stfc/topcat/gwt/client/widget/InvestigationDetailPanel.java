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

import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigator;
import uk.ac.stfc.topcat.core.gwt.module.TParameter;
import uk.ac.stfc.topcat.core.gwt.module.TPublication;
import uk.ac.stfc.topcat.core.gwt.module.TShift;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.model.TopcatInvestigation;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.binding.FieldBinding;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

public class InvestigationDetailPanel extends Composite {
    private TopcatInvestigation investigationModel = new TopcatInvestigation();
    // private ContentPanel contentPanel;
    private LayoutContainer mainPanel;
    private LabelField dataTitle;
    private LabelField dataFacility;
    private LabelField dataInstrument;
    private LabelField dataInvestigationNumber;
    private LabelField lableFacility;
    private LabelField lableInstrument;
    private LabelField lableTitle;
    private LabelField lableInvestigationNumber;
    private LabelField lableVisitId;
    private LabelField dataVisitId;
    private LabelField lableInvestigators;
    private LabelField lableProposal;
    private LabelField dataProposal;
    private LayoutContainer topPanel;
    private LabelField lableShifts;
    private LabelField lablePublications;
    private FlexTable shiftsTable;
    private FlexTable namesTable;
    private FlexTable publicationsTable;
    private LabelField lableStartDate;
    private LabelField dataStartDate;
    private LabelField lableEndDate;
    private LabelField dataEndDate;
    private LabelField lableParameters;
    private FlexTable parametersTable;
    private LayoutContainer shiftsContainer;
    private LayoutContainer publicationsContainer;
    private LayoutContainer investigatorsContainer;
    private LayoutContainer proposalContainer;
    private LayoutContainer parametersContainer;

    /**
     * Constructor
     */
    public InvestigationDetailPanel() {
        mainPanel = new LayoutContainer();
        TableLayout tl_mainPanel = new TableLayout();
        tl_mainPanel.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_mainPanel.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        mainPanel.setLayout(tl_mainPanel);

        topPanel = new LayoutContainer();
        TableLayout tl_topContainer = new TableLayout(2);
        tl_topContainer.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_topContainer.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        tl_topContainer.setCellSpacing(5);
        topPanel.setLayout(tl_topContainer);

        lableFacility = new LabelField("Facility: ");
        topPanel.add(lableFacility);

        dataFacility = new LabelField("Facility");
        topPanel.add(dataFacility);

        lableInstrument = new LabelField("Instrument: ");
        topPanel.add(lableInstrument);

        dataInstrument = new LabelField("Instrument");
        topPanel.add(dataInstrument);

        lableTitle = new LabelField("Title: ");
        topPanel.add(lableTitle);

        dataTitle = new LabelField("Title");
        topPanel.add(dataTitle);

        lableInvestigationNumber = new LabelField("Investigation No: ");
        TableData td_lableInvestigationNumber = new TableData();
        td_lableInvestigationNumber.setWidth("105");
        topPanel.add(lableInvestigationNumber, td_lableInvestigationNumber);

        dataInvestigationNumber = new LabelField("Investigation Number");
        topPanel.add(dataInvestigationNumber);

        lableVisitId = new LabelField("Visit Id: ");
        topPanel.add(lableVisitId);

        dataVisitId = new LabelField("Visit Id");
        topPanel.add(dataVisitId);

        lableStartDate = new LabelField("Start Date:");
        topPanel.add(lableStartDate);

        dataStartDate = new LabelField("Start Date");
        topPanel.add(dataStartDate);

        lableEndDate = new LabelField("End Date:");
        topPanel.add(lableEndDate);

        dataEndDate = new LabelField("End Date");
        topPanel.add(dataEndDate);

        mainPanel.add(topPanel);
        topPanel.setSize("100%", "100%");

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
        mainPanel.add(shiftsContainer);

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
        mainPanel.add(investigatorsContainer);

        // Proposals
        proposalContainer = new LayoutContainer();
        proposalContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        lableProposal = new LabelField("Proposal:");
        proposalContainer.add(lableProposal, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        dataProposal = new LabelField("Proposal");
        proposalContainer.add(dataProposal, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));

        mainPanel.add(proposalContainer);

        // Publications
        publicationsContainer = new LayoutContainer();
        publicationsContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        lablePublications = new LabelField("Publications:");
        publicationsContainer.add(lablePublications,
                new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        publicationsTable = new FlexTable();
        publicationsContainer
                .add(publicationsTable, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));
        mainPanel.add(publicationsContainer);

        // Parameters
        parametersContainer = new LayoutContainer();
        parametersContainer.setLayout(new RowLayout(Orientation.VERTICAL));

        lableParameters = new LabelField("Parameters:");
        parametersContainer.add(lableParameters, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 5, 5, 5)));

        parametersTable = new FlexTable();
        parametersTable.setBorderWidth(1);
        parametersContainer.add(parametersTable, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(0, 5, 5, 5)));
        mainPanel.add(parametersContainer);

        initComponent(mainPanel);
        initDataBindings();
    }

    /**
     * Get the facility name.
     * 
     * @return the facility name
     */
    public String getFacilityName() {
        return (String) dataFacility.getValue();
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
        dataProposal.clear();
        publicationsContainer.hide();
        publicationsTable.removeAllRows();
        parametersContainer.hide();
        parametersTable.removeAllRows();
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
        
        // As of ICAT version 4.3, an investigation can have more than 1 instrument.
        // We list the instruments in a string separated by commas.
        StringBuilder sb = new StringBuilder();
        String separator = "";
        for (String instrumentName : inv.getInstruments()) {
            sb.append(separator);
            sb.append(instrumentName);
            separator = ", ";
        }
        
        investigationModel = new TopcatInvestigation(inv.getServerName(), inv.getFacilityName(), inv.getInvestigationId(),
                inv.getInvestigationName(), inv.getTitle(), inv.getVisitId(), inv.getStartDate(), inv.getEndDate(),
                sb.toString(), inv.getProposal());

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
                i++;
            }
            shiftsContainer.show();
        }

        // Investigators
        if (inv.getInvestigators().size() > 0) {
            int i = 0;
            for (TInvestigator tinvestigator : inv.getInvestigators()) {
                HorizontalPanel investigatorPanel = new HorizontalPanel();
                if (tinvestigator.getRole() != null) {
                    investigatorPanel.add(new LabelField(tinvestigator.getRole()));
                    investigatorPanel.add(new LabelField("&nbsp;-&nbsp;"));
                }
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
        if (inv.getParameters().size() > 0) {
            LabelField name = new LabelField("Name");
            parametersTable.setWidget(0, 0, name);
            LabelField value = new LabelField("Value");
            parametersTable.setWidget(0, 1, value);
            LabelField units = new LabelField("Units");
            parametersTable.setWidget(0, 2, units);
            int i = 1;
            for (TParameter tparameter : inv.getParameters()) {
                parametersTable.setWidget(i, 0, new LabelField(tparameter.getName()));
                parametersTable.setWidget(i, 1, new LabelField(tparameter.getValue()));
                parametersTable.setWidget(i, 2, new LabelField(tparameter.getUnits()));
                i++;
            }
            parametersContainer.show();
        }

        initDataBindings();
        EventPipeLine.getInstance().getTcEvents().fireResize();
    }

    protected void initDataBindings() {
        FieldBinding fieldBinding = new FieldBinding(dataFacility, "facilityName");
        fieldBinding.bind(investigationModel);
        //
        FieldBinding fieldBinding_1 = new FieldBinding(dataVisitId, "visitId");
        fieldBinding_1.bind(investigationModel);
        //
        FieldBinding fieldBinding_2 = new FieldBinding(dataProposal, "proposal");
        fieldBinding_2.bind(investigationModel);
        //
        FieldBinding fieldBinding_3 = new FieldBinding(dataTitle, "title");
        fieldBinding_3.bind(investigationModel);
        //
        FieldBinding fieldBinding_4 = new FieldBinding(dataInvestigationNumber, "investigationName");
        fieldBinding_4.bind(investigationModel);
        //
        FieldBinding fieldBinding_5 = new FieldBinding(dataStartDate, "formatedStartDate");
        fieldBinding_5.bind(investigationModel);
        //
        FieldBinding fieldBinding_6 = new FieldBinding(dataEndDate, "formatedEndDate");
        fieldBinding_6.bind(investigationModel);
        //
        FieldBinding fieldBinding_7 = new FieldBinding(dataInstrument, "instrument");
        fieldBinding_7.bind(investigationModel);
    }

    protected TopcatInvestigation getInvestigationModel() {
        return investigationModel;
    }
}
