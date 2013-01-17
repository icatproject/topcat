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
import java.util.Date;

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
public class DatafileModel extends DatafileFormatModel implements Serializable {

    public DatafileModel() {
        super();
    }

    /**
     * Constructor to set the datafile information.
     * 
     * @param facilityName
     *            facility Name
     * @param datasetId
     *            dataset id
     * @param datasetName
     *            dataset name
     * @param id
     *            datafile id
     * @param name
     *            datafile name
     * @param description
     *            datafile description
     * @param fileSize
     *            datafile size
     * @param doi
     *            datafile doi
     * @param location
     *            datafile location
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
     * @param createTime
     *            datafile create time
     * @param modTime
     *            datafile mod time
     */
    public DatafileModel(String facilityName, String datasetId, String datasetName, String id, String name,
            String description, String fileSize, String doi, String location, String formatId, String formatName,
            String formatDescription, String formatVersion, String formatType, Date createTime, Date modTime) {
        super(facilityName, formatId, formatName, formatDescription, formatVersion, formatType);
        setDatasetId(datasetId);
        setDatasetName(datasetName);
        setId(id);
        setName(name);
        setDescription(description);
        setFileSize(fileSize);
        setCreateTime(createTime);
        setLocation(location);
        setSelected(false);
    }

    /**
     * Set the dataset id
     * 
     * @param datasetId
     */
    public void setDatasetId(String datasetId) {
        set("datasetId", datasetId);
    }

    /**
     * Set the dataset name
     * 
     * @param datasetName
     */
    public void setDatasetName(String datasetName) {
        set("datasetName", datasetName);
    }

    /**
     * Set the datafile id
     * 
     * @param datafileId
     */
    public void setId(String datafileId) {
        set("datafileId", datafileId);
    }

    /**
     * Set the datafile creation time
     * 
     * @param createTime
     */
    public void setCreateTime(Date createTime) {
        set("datafileCreateTime", createTime);
    }

    /**
     * Set the datafile mod time
     * 
     * @param modTime
     */
    public void setModTime(Date modTime) {
        set("datafileModTime", modTime);
    }

    /**
     * Set the datafile description
     * 
     * @param description
     */
    public void setDescription(String description) {
        set("datafileDescription", description);
    }

    /**
     * Set the datafile doi
     * 
     * @param doi
     */
    public void setDoi(String doi) {
        set("datafileDoi", doi);
    }

    /**
     * Set the datafile size
     * 
     * @param fileSize
     */
    public void setFileSize(String fileSize) {
        set("datafileSize", fileSize);
    }

    /**
     * Set the datafile name
     * 
     * @param name
     */
    public void setName(String name) {
        set("datafileName", name);
    }

    /**
     * Set the datafile location
     * 
     * @param location
     */
    public void setLocation(String location) {
        set("datafileLocation", location);
    }

    /**
     * Set the datafile selected flag
     * 
     * @param selected
     */
    public void setSelected(boolean selected) {
        set("datafileSelected", new Boolean(selected));
    }

    /**
     * @return dataset id
     */
    public String getDatasetId() {
        return get("datasetId");
    }

    /**
     * @return datafile creation time
     */
    public String getCreateTime() {
        return get("datafileCreateTime");
    }

    /**
     * @return datafile mod time
     */
    public String getModTime() {
        return get("datafileModTime");
    }

    /**
     * @return description
     */
    public String getDescription() {
        return get("datafileDescription");
    }

    /**
     * @return doi
     */
    public String getDoi() {
        return get("datafileDoi");
    }

    /**
     * @return datafile size
     */
    public String getFileSize() {
        return get("datafileSize");
    }

    /**
     * @return datafile name
     */
    public String getName() {
        return get("datafileName");
    }

    /**
     * @return datafile id
     */
    public String getId() {
        return get("datafileId");
    }

    /**
     * @return datafile location
     */
    public String getLocation() {
        return get("datafileLocation");
    }

    /**
     * @return datafile selected flag
     */
    public Boolean getSelected() {
        return get("datafileSelected");
    }
}
