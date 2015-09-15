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
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * This class is implementation of hidden form window that submits the datafile
 * and facility info to the server and on submit which downloads the parameters
 * file corresponding to input.
 * 
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class ParameterDownloadForm extends Composite {
    private FixedFormPanel formPanel;
    private VerticalPanel verticalPanel;
    private Hidden hiddenFacilityName;
    private Hidden hiddenDataType;
    private Hidden hiddenDataId;

    public ParameterDownloadForm() {
        super();
        formPanel = new FixedFormPanel();
        formPanel.setAction(GWT.getModuleBaseURL() + "/CopyDataToCSVFile");
        formPanel.setMethod(FormPanel.Method.POST);
        initWidget(formPanel);

        verticalPanel = new VerticalPanel();
        formPanel.add(verticalPanel);
        verticalPanel.setSize("100%", "100%");

        hiddenFacilityName = new Hidden("FacilityName");
        verticalPanel.add(hiddenFacilityName);

        hiddenDataType = new Hidden("DataType");
        verticalPanel.add(hiddenDataType);

        hiddenDataId = new Hidden("DataId");
        verticalPanel.add(hiddenDataId);
    }

    public void setFacilityName(String facilityName) {
        hiddenFacilityName.setValue(facilityName);
    }

    public void setDataType(String dataType) {
        hiddenDataType.setValue(dataType);
    }

    public void setDataId(String datafileId) {
        hiddenDataId.setValue(datafileId);
    }

    /**
     * This method submits the form to servlet
     */
    public void submit() {
        formPanel.submit();
    }

}
