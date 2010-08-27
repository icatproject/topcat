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

import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * This class is a datamodel to hold Investigation information
 * TODO: change it from datamodel to beanmodel
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3   
 */
@SuppressWarnings("serial")
public class TopcatInvestigation extends BaseModel {

	/**
	 * Constructor
	 * @param serverName facility name
	 * @param investigationName Investigation name
	 */
	public TopcatInvestigation(String serverName,String investigationName) {
		set("serverName",serverName);
		set("investigationName",investigationName);		
	}
	
	/**
	 * Constructor
	 * @param serverName facility name
	 * @param investigationId investigation id
	 * @param investigationName Investigation name
	 * @param title Investigation title
	 * @param startDate Investigation start date
	 * @param endDate Investigation end date
	 */
	public TopcatInvestigation(String serverName,String investigationId, String investigationName,String title,Date startDate,Date endDate) {
		set("serverName",serverName);
		set("investigationName",investigationName);	
		set("investigationId",investigationId);
		set("title",title);
		set("startDate",startDate);
		set("endDate",endDate);
	}	
	
	/**
	 * @return facility name
	 */
	public String getFacilityName() {
		return get("serverName");
	}
	
	/**
	 * @return investigation name
	 */
	public String getInvestigationName() {
		return get("investigationName");
	}
	
	/**
	 * @return investigation id
	 */
	public String getInvestigationId() {
		return get("investigationId");
	}
	
	/**
	 * @return investigation title
	 */
	public String getInvestigationTitle(){
		return get("title");
	}
}
