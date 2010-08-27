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
package uk.ac.stfc.topcat.gwt.client.model;
/**
 * Imports
 */
import java.io.Serializable;
import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModelData;
/**
 * This class is a datamodel to hold datafile information
 * TODO: change it from datamodel to beanmodel
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3   
 */
@SuppressWarnings("serial")
public class DatafileModel extends BaseModelData implements Serializable {
	
	public DatafileModel() {
		
	}
	/**
	 * Constructor to set the datafile information.
	 * @param facilityName facility Name
	 * @param datasetName  dataset name
	 * @param datafileId   datafile id
	 * @param name		   datafile name
	 * @param fileSize     datafile size
	 * @param format	   datafile format
	 * @param formatVersion datafile format version
	 * @param formatType    datafile format type
	 * @param createTime    datafile creation time
	 */
	public DatafileModel(String facilityName,String datasetName,String datafileId,String name,String fileSize,String format,String formatVersion,String formatType,Date createTime){
		setFacilityName(facilityName);
		setDatasetName(datasetName);
		setId(datafileId);		
		setName(name);
		setFileSize(fileSize);
		setFormat(format);
		setFormatVersion(formatVersion);
		setFormatType(formatType);
		setCreateTime(createTime);
	}

	/**
	 * Set the facility name
	 * @param facilityName
	 */
	public void setFacilityName(String facilityName) {
		set("facilityName",facilityName);
	}
	
	/**
	 * Set the dataset name 
	 * @param datasetName
	 */
	public void setDatasetName(String datasetName) {
		set("datasetName",datasetName);
	}
	
	/**
	 * Set the datafile id
	 * @param datafileId
	 */
	public void setId(String datafileId) {
		set("datafileId",datafileId);
	}
	
	/**
	 * Set the datafile creation time
	 * @param createTime
	 */
	public void setCreateTime(Date createTime) {
		set("datafileCreateTime",createTime);
	}

	/**
	 * Set the dataile format type
	 * @param formatType
	 */
	public void setFormatType(String formatType) {
		set("datafileFormatType",formatType);
	}

	/**
	 * Set the datafile format version
	 * @param formatVersion
	 */
	public void setFormatVersion(String formatVersion) {
		set("datafileFormatVersion",formatVersion);
	}

	/**
	 * Set the datafile format
	 * @param format
	 */
	public void setFormat(String format) {
		set("datafileFormat",format);
	}

	/**
	 * Set the datafile size
	 * @param fileSize
	 */
	public void setFileSize(String fileSize) {
		set("datafileSize",fileSize);
	}

	/**
	 * Set the datafile name
	 * @param name
	 */
	public void setName(String name) {
		set("datafileName",name);		
	}

	/**
	 * @return facility name
	 */
	public String getFacilityName() {
		return get("facilityName");
	}
	
	/**
	 * @return datafile creation time
	 */
	public String getCreateTime() {
		return get("datafileCreateTime");
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
}
