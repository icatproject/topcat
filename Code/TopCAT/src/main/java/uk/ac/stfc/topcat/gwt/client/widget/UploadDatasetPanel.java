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

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;

public class UploadDatasetPanel extends Composite {

    /**
     * Constructor
     */
    public UploadDatasetPanel() {
        LayoutContainer mainContainer = new LayoutContainer();
        TableLayout tl_mainPanel = new TableLayout();
        tl_mainPanel.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_mainPanel.setCellHorizontalAlign(HorizontalAlignment.CENTER);
        tl_mainPanel.setWidth("100%");
        mainContainer.setLayout(tl_mainPanel);

        LayoutContainer mainPanel = new LayoutContainer();
        tl_mainPanel = new TableLayout();
        tl_mainPanel.setCellVerticalAlign(VerticalAlignment.TOP);
        tl_mainPanel.setCellHorizontalAlign(HorizontalAlignment.LEFT);
        tl_mainPanel.setWidth("705px");
        mainPanel.setLayout(tl_mainPanel);

        LabelField lable = new LabelField("Upload your dataset here");
        mainPanel.add(new Text(""));
        mainPanel.add(lable);
        mainPanel.add(new Text(""));

        mainContainer.add(mainPanel);
        initComponent(mainContainer);
    }

}
