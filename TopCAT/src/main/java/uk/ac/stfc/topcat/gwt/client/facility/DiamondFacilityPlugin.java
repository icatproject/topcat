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
package uk.ac.stfc.topcat.gwt.client.facility;

/**
 * Imports
 */
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;

import com.extjs.gxt.ui.client.widget.Composite;
import com.google.gwt.core.client.GWT;

/**
 * This is Diamond Facility Plugin.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class DiamondFacilityPlugin extends FacilityPlugin {

    private static DiamondFacilityPlugin diamondFacility = GWT.create(DiamondFacilityPlugin.class);
    DiamondSearchWidget widget;

    private DiamondFacilityPlugin() {
        super();
        widget = new DiamondSearchWidget(EventPipeLine.getInstance());
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.stfc.topcat.gwt.client.facility.FacilityPlugin#getGUI()
     */
    @Override
    public Composite getGUI() {
        return widget;
    }

    public static DiamondFacilityPlugin getInstance() {
        return diamondFacility;
    }

    @Override
    public void setFacilityName(String facilityName) {
        widget.setFacilityName(facilityName);

    }
}
