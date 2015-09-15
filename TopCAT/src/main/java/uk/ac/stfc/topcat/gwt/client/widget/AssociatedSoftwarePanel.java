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

import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AssociatedSoftwarePanel extends Composite {
    private LayoutContainer mainContainer;
    private EventPipeLine eventBus;
    private Text software;
    private String instrument;

    /**
     * Constructor
     */
    public AssociatedSoftwarePanel() {
        eventBus = EventPipeLine.getInstance();
        instrument = "";

        mainContainer = new LayoutContainer();
        TableLayout tl_mainPanel = new TableLayout();
        tl_mainPanel.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_mainPanel.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        tl_mainPanel.setWidth("705px");
        mainContainer.setLayout(tl_mainPanel);
        mainContainer.add(new Text(""));

        Button btnExport = new Button("Call PanSoft");
        btnExport.setToolTip("Click to call the pansoftImlp");
        btnExport.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                eventBus.showRetrievingData();
                eventBus.getSoftwareRepoService().getAssociatedSoftware(instrument, new AsyncCallback<String>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.hideRetrievingData();
                        eventBus.showMessageDialog("ERROR " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        eventBus.hideRetrievingData();
                        software.setText(result);
                    }
                });
            }
        });
        mainContainer.add(btnExport);

        LabelField label = new LabelField("A list of software that you may find useful");
        mainContainer.add(label);
        software = new Text("");
        mainContainer.add(software);
        mainContainer.add(new Text(""));

        initComponent(mainContainer);
    }

    /**
     * Set the instrument name.
     * 
     * @param instrument
     *            a string containing the name of the instrument
     */
    protected void setInstrument(String instrument) {
        this.instrument = instrument;
    }

}
