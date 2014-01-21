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
package uk.ac.stfc.topcat.gwt.client.model;

/**
 * Imports
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * This class is a datamodel to hold Parameter information TODO: change it from
 * datamodel to beanmodel
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@SuppressWarnings("serial")
public class ParameterModel extends BaseModelData implements Serializable {
    private List<String> facilityNames = new ArrayList<String>();

    public ParameterModel() {

    }

    /**
     * Constructor
     * 
     * @param facilityName
     *            facility name
     * @param name
     *            parameter name
     */
    public ParameterModel(String facilityName, String name) {
        facilityNames.add(facilityName);
        setName(name);
    }

    /**
     * Constructor
     * 
     * @param name
     *            Parameter name
     * @param units
     *            units of parameter
     * @param value
     *            value of parameter
     */
    public ParameterModel(String name, String units, String value) {
        setName(name);
        setUnits(units);
        setValue(value);
    }

    /**
     * Constructor
     * 
     * @param facilityName
     *            facility name
     * @param name
     *            parameter name
     * @param units
     *            units of parameter
     * @param value
     *            value of parameter
     */
    public ParameterModel(String facilityName, String name, String units, String value) {
        facilityNames.add(facilityName);
        setName(name);
        setUnits(units);
        setValue(value);
    }

    /**
     * @return facility name
     */
    public List<String> getFacilityNames() {
        return facilityNames;
    }

    /**
     * Set facility name
     * 
     * @param facilityName
     */
    public void addFacilityName(String facilityName) {
        facilityNames.add(facilityName);
    }

    /**
     * @return parameter name
     */
    public String getName() {
        return get("name");
    }

    /**
     * Set parameter name
     * 
     * @param name
     */
    public void setName(String name) {
        set("name", name);
    }

    /**
     * @return units of parameter
     */
    public String getUnits() {
        return get("units");
    }

    /**
     * Set the units of parameter
     * 
     * @param units
     */
    public void setUnits(String units) {
        set("units", units);
    }

    /**
     * @return the value of parameter
     */
    public String getValue() {
        return get("value");
    }

    /**
     * Set the value of parameter
     * 
     * @param value
     */
    public void setValue(String value) {
        set("value", value);
    }
}
