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

import com.extjs.gxt.ui.client.data.BaseModelData;
/**
 * This class is a datamodel to hold dataset information
 * TODO: change it from datamodel to beanmodel
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3   
 */
@SuppressWarnings("serial")
public class DatasetModel extends BaseModelData implements Serializable{
	public DatasetModel() {
		
	}

	/**
	 * Constructor to set dataset information
	 * @param facilityName facility name
	 * @param datasetId    dataset id
	 * @param datasetName  dataset name
	 * @param datasetStatus dataset status
	 * @param datasetType   dataset type
	 * @param datasetDescription dataset description
	 */
	public DatasetModel(String facilityName,String datasetId,String datasetName,String datasetStatus,String datasetType,String datasetDescription) {
		setFacilityName(facilityName);
		setId(datasetId);
		setName(datasetName);
		setStatus(datasetStatus);
		setType(datasetType);
		setDescription(datasetDescription);
	}
	
	/**
	 * Constructor to set dataset information
	 * @param facilityName facility name
	 * @param datasetId    dataset id
	 * @param datasetName  dataset name
	 * @param datasetStatus dataset status
	 * @param datasetType   dataset type
	 * @param datasetDescription dataset description
	 * @param datasetSample dataset sample
	 */
	public DatasetModel(String facilityName,String datasetId,String datasetName,String datasetStatus,String datasetType,String datasetDescription, String datasetSample) {
		setFacilityName(facilityName);
		setId(datasetId);		
		setName(datasetName);
		setStatus(datasetStatus);
		setType(datasetType);
		setDescription(datasetDescription);
		setSample(datasetSample);
	}
	
	/**
	 * Set the facility name
	 * @param facilityName
	 */
	public void setFacilityName(String facilityName) {
		set("facilityName",facilityName);
	}
	
	/**
	 * Set the dataset id
	 * @param datasetId
	 */
	public void setId(String datasetId) {
		set("datasetId",datasetId);
	}
	
	/**
	 * Set dataset name
	 * @param datasetName
	 */
	public void setName(String datasetName) {
		set("datasetName",datasetName);
	}
	
	/**
	 * Set dataset status
	 * @param datasetStatus
	 */
	public void setStatus(String datasetStatus) {
		set("datasetStatus",datasetStatus);
	}
	
	/**
	 * Set dataset type
	 * @param datasetType
	 */
	public void setType(String datasetType) {
		set("datasetType",datasetType);
	}
	
	/**
	 * Set dataset description
	 * @param datasetDescription
	 */
	public void setDescription(String datasetDescription) {
		set("datasetDescription",datasetDescription);
	}
	
	/**
	 * Set dataset sample name
	 * @param datasetSample
	 */
	public void setSample(String datasetSample) {
		set("datasetSample",datasetSample);
	}
	
	/**
	 * @return facility name
	 */
	public String getFacilityName() {
		return get("facilityName");
	}
	
	/**
	 * @return dataset id
	 */
	public String getId() {
		return get("datasetId");
	}
	
	/**
	 * @return dataset name
	 */
	public String getName(){
		return get("datasetName");
	}
	
	/**
	 * @return dataset status
	 */
	public String getStatus() {
		return get("datasetStatus");
	}
	
	/**
	 * @return dataset type
	 */
	public String getType() {
		return get("datasetType");
	}

	/**
	 * @return dataset description
	 */
	public String getDescription() {
		return get("datasetDescription");
	}

	/**
	 * @return dataset sample
	 */
	public String getSample() {
		return get("datasetSample");
	}
}
