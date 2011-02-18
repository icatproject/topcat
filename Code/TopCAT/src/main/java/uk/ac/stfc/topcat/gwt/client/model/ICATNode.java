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

import java.io.Serializable;
import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * This class represents a generic node in the tree widget. used for browsing data in hierarchy of information.
 * TODO: change it from datamodel to beanmodel
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3   
 */
@SuppressWarnings("serial")
public class ICATNode extends BaseModelData implements Serializable, Comparable {
	ICATNodeType type; // added just to let gwt compiler know that ICATNodeType is serializable
	public ICATNode(){		
	}
	/**
	 * Set the facility name
	 * @param name
	 */
	public void setFacility(String name){
		set("facility",name);
	}

	/**
	 * Set instrument name
	 * @param name
	 */
	public void setInstrumentName(String name) {
		set("instrumentName",name);
	}
	
	/**
	 * Set investigation Id
	 * @param id
	 */
	public void setInvestigationId(String id) {
		set("investigationId",id);
	}

	/**
	 * Set investigation name
	 * @param name
	 */
	public void setInvestigationName(String name) {
		set("investigationName",name);
	}
	
	/**
	 * Set Dataset Id
	 * @param id
	 */
	public void setDatasetId(String id) {
		set("datasetId",id);
	}
	
	/**
	 * Set Dataset Name
	 * @param name
	 */
	public void setDatasetName(String name) {
		set("datasetName",name);
	}

	/**
	 * Set Datafile Id
	 * @param id
	 */
	public void setDatafileId(String id) {
		set("datafileId",id);
	}
	
	/**
	 * Set Datafile name
	 * @param name
	 */
	public void setDatafileName(String name) {
		set("datafileName",name);
	}
	
	/**
	 * This method sets the node to given input type
	 * @param type node type
	 * @param id   node id
	 * @param name node name
	 */
	public void setNode(ICATNodeType type, String id, String name) {
		switch(type) {
		case FACILITY:
			setFacility(name);
			set("name",name);	
			break;
		case INSTRUMENT:
			setInstrumentName(name);
			set("name",name);
			break;
		case CYCLE:
			set("name",name);
			break;
		case INVESTIGATION:
			setInvestigationId(id);
			setInvestigationName(name);
			set("name",name);
			break;
		case DATASET:
			setDatasetId(id);
			setDatasetName(name);
			set("name",name);	
			break;
		case DATAFILE:
			setDatafileId(id);
			setDatafileName(name);
			set("name",name);
			break;
		default:
		}
		set("type",type);
	}
	
	/**
	 * @return node type
	 */
	public ICATNodeType getNodeType() {
		return get("type");
	}
	
	/**
	 * @return facility name
	 */
	public String getFacility() {
		return get("facility");
	}
	
	/**
	 * @return Instrument name
	 */
	public String getInstrumentName() {
		return get("instrumentName");
	}
	
	/**
	 * @return investigation id
	 */
	public String getInvestigationId() {
		return get("investigationId");
	}

	/**
	 * @return investigation name
	 */
	public String getInvestigationName() {
		return get("investigationName");
	}
	
	/**
	 * @return dataset id
	 */
	public String getDatasetId() {
		return get("datasetId");
	}
	
	/**
	 * @return dataset name
	 */
	public String getDatasetName() {
		return get("datasetName");
	}

	/**
	 * @return datafile id
	 */
	public String getDatafileId() {
		return get("datafileId");
	}
	
	/**
	 * @return datafile name
	 */
	public String getDatafileName() {
		return get("datafileName");
	}
	
	/**
	 * @return investigation title
	 */
	public String getTitle() {
		return get("title");
	}
	
	/**
	 * Set Investigation title
	 * @param title
	 */
	public void setTitle(String title) {
		set("title",title);
	}
	
	/**
	 * Returns the Start Date of Cycle
	 * @return
	 */
	public Date getStartDate(){
		return get("startDate");
	}
	
	/**
	 * Set the start date of the cycle
	 * @param startDate
	 */
	public void setStartDate(Date startDate){
		//set("name",getDescription()+"("+startDate+"--"+getEndDate()+")");		
		set("startDate",startDate);
	}

	/**
	 * Returns the end date of cycle
	 * @return
	 */
	public Date getEndDate(){
		return get("endDate");
	}
	
	/**
	 * Sets the end date of the cycle
	 * @param endDate
	 */
	public void setEndDate(Date endDate){
		//set("name",getDescription()+"("+getStartDate()+"--"+endDate+")");		
		set("endDate",endDate);
	}
	
	/**
	 * Returns the cycle description
	 * @return
	 */
	public String getDescription(){
		return get("cycleDescription");
	}
	
	/**
	 * This method sets the cycle description
	 * @param cycleDescription
	 */
	public void setDescription(String cycleDescription){
		set("cycleDescription",cycleDescription);
	}

    @Override
    public int compareTo(Object t) {
        //Name is used for comparision in sorting
        ICATNode tmp = (ICATNode) t;
        return ((String)get("name")).compareToIgnoreCase((String)tmp.get("name"));
    }
}
