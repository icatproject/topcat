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
import java.util.ArrayList;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * This class is a Investigation type used as node in the tree to represent type of the
 * node and values the node holds.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3   
 */
@SuppressWarnings("serial")
public class InvestigationType extends BaseModelData {

	ArrayList<String> serverList;
	public InvestigationType() {	
		serverList=new ArrayList<String>();
	}
	/**
	 * Constructor 
	 * @param name investigation type 
	 */
	public InvestigationType(String name) {
		serverList=new ArrayList<String>();		
		setName(name);
	}
	
	/**
	 * Constructor
	 * @param server facility name
	 * @param name   investigation type
	 */
	public InvestigationType(String server,String name) {
		serverList=new ArrayList<String>();		
		addServer(server);
		setName(name);
	}
		
	
	/**
	 * Set the investigation type
	 * @param name
	 */
	public void setName(String name) {
		set("name",name);
		set("displayName", getName()+"( "+getServer()+" ) ");		
	}
	
	/**
	 * @return investigation type
	 */
	public String getName(){
		return get("name");
	}
	
	/**
	 * Adds the facility to the investigation type
	 * @param server
	 */
	public void addServer(String server) {
		serverList.add(server);
		set("displayName", getName()+"( "+getServer()+" ) ");	
	}

	/**
	 * @return facilities list for the investigation type
	 */
	public String getServer() {
		String servername ="";
		for(int count=0;count<serverList.size();count++){
			if(count==serverList.size()-1)
				servername+=serverList.get(count);
			else
				servername+=serverList.get(count)+",";
		}
		return servername ;
	}	
}
