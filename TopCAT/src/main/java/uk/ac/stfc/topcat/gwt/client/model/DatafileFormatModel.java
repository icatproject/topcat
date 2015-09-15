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
import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * This class is a datamodel to hold datafile information TODO: change it from
 * datamodel to beanmodel
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@SuppressWarnings("serial")
public class DatafileFormatModel extends BaseModelData implements Serializable {

    public DatafileFormatModel() {

    }

    /**
     * Constructor to set the datafile information.
     * 
     * @param facilityName
     *            facility Name
     * @param formatId
     *            datafile format id
     * @param formatName
     *            datafile format name
     * @param formatDescription
     *            datafile format
     * @param formatVersion
     *            datafile format version
     * @param formatType
     *            datafile format type
     */
    public DatafileFormatModel(String facilityName, String formatId, String formatName, String formatDescription,
            String formatVersion, String formatType) {
        setFacilityName(facilityName);
        setFormatDescription(formatDescription);
        setFormat(formatName + " (" + formatType + " v" + formatVersion + ")");
        setFormatId(formatId);
        setFormatVersion(formatVersion);
        setFormatType(formatType);
    }

    /**
     * Set the facility name
     * 
     * @param facilityName
     */
    public void setFacilityName(String facilityName) {
        set("facilityName", facilityName);
    }

    /**
     * Set the dataile format type
     * 
     * @param formatType
     */
    public void setFormatType(String formatType) {
        set("datafileFormatType", formatType);
    }

    /**
     * Set the datafile format version
     * 
     * @param formatVersion
     */
    public void setFormatVersion(String formatVersion) {
        set("datafileFormatVersion", formatVersion);
    }

    /**
     * Set the datafile format
     * 
     * @param format
     */
    public void setFormat(String format) {
        set("datafileFormat", format);
    }

    /**
     * Set the datafile format id
     * 
     * @param formatId
     */
    public void setFormatId(String formatId) {
        set("datafileFormatId", formatId);
    }

    /**
     * Set the datafile format description
     * 
     * @param formatDescription
     */
    public void setFormatDescription(String formatDescription) {
        set("datafileFormatDescription", formatDescription);
    }

    /**
     * @return facility name
     */
    public String getFacilityName() {
        return get("facilityName");
    }

    /**
     * @return datafile format type
     */
    public String getFormatType() {
        return get("datafileFormatType");
    }

    /**
     * @return datafile format version
     */
    public String getFormatVersion() {
        return get("datafileFormatVersion");
    }

    /**
     * @return datafile format
     */
    public String getFormat() {
        return get("datafileFormat");
    }

    /**
     * @return datafile format id
     */
    public String getFormatId() {
        return get("datafileFormatId");
    }

    /**
     * @return datafile format description
     */
    public String getFormatDescription() {
        return get("datafileFormatDescription");
    }

}
