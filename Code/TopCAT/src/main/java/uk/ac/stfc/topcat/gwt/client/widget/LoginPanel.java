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

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
/**
 * This is a widget that holds logininfopanel for each facility (ICAT instance).
 * It is created dynamically while the TopCAT is loading in the browser by quering the server to get the list of facilities.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class LoginPanel extends Composite {
	private VerticalPanel verticalPanel;
	HashMap<String ,LoginInfoPanel> listFacilityLogin;
	public LoginPanel() {
		listFacilityLogin = new HashMap<String,LoginInfoPanel>();
		verticalPanel = new VerticalPanel();
		verticalPanel.setHorizontalAlign(HorizontalAlignment.RIGHT);
		initComponent(verticalPanel);
	}

	protected VerticalPanel getVerticalPanel() {
		return verticalPanel;
	}

	public LoginInfoPanel getFacilityLoginInfoPanel(String facilityName){
		return listFacilityLogin.get(facilityName);		
	}
	
	/**
	 * Creates the LoginInfoPanel for each facility and added to the widget
	 * @param eventPipeLine
	 * @param facilities
	 */
	public void createICATLoginLinks(EventPipeLine eventPipeLine,ArrayList<TFacility> facilities) {
		verticalPanel.removeAll();
		listFacilityLogin.clear();
		for(TFacility facility: facilities) {
			//create a link which opens the login widget
			LoginInfoPanel infoPanel = new LoginInfoPanel();
			infoPanel.setFacility(facility);
			infoPanel.setEventPipeLine(eventPipeLine);
			listFacilityLogin.put(facility.getName(),infoPanel);
			verticalPanel.add(infoPanel);
		}
		verticalPanel.layout();
	}

}
