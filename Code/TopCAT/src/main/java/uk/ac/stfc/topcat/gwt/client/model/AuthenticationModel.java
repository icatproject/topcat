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
package uk.ac.stfc.topcat.gwt.client.model;

/**
 * Imports
 */
import java.io.Serializable;

import uk.ac.stfc.topcat.core.gwt.module.TAuthentication;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * This class is a datamodel to hold authentication information
 * 
 */
@SuppressWarnings("serial")
public class AuthenticationModel extends BaseModelData implements Serializable {

    public AuthenticationModel() {
    }

    public AuthenticationModel(TAuthentication tAuthentication) {
        setDisplayName(tAuthentication.getDisplayName());
        setFacilityName(tAuthentication.getFacilityName());
        setType(tAuthentication.getType());
        setPluginName(tAuthentication.getPluginName());
        setUrl(tAuthentication.getUrl());
    }

    /**
     * @return display name
     */
    public String getDisplayName() {
        return get("displayName");
    }

    /**
     * Set displayName
     * 
     * @param displayName
     */
    public void setDisplayName(String displayName) {
        set("displayName", displayName);
    }

    /**
     * @return authentication plugin name
     */
    public String getPluginName() {
        return get("pluginName");
    }

    /**
     * Set plugin name
     * 
     * @param pluginName
     */
    public void setPluginName(String pluginName) {
        set("pluginName", pluginName);
    }

    /**
     * @return authentication service url
     */
    public String getUrl() {
        return get("url");

    }

    /**
     * Set authentication service url
     * 
     * @param url
     */
    public void setUrl(String url) {
        set("url", url);
    }

    /**
     * @return authentication type
     */
    public String getType() {
        return get("type");

    }

    /**
     * Set authentication type
     * 
     * @param type
     */
    public void setType(String type) {
        set("type", type);
    }

    /**
     * @return facility name
     */
    public String getFacilityName() {
        return get("facilityName");

    }

    /**
     * Set facility name
     * 
     * @param facilityName
     */
    public void setFacilityName(String facilityName) {
        set("facilityName", facilityName);
    }

}
