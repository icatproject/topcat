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
package uk.ac.stfc.topcat.gwt.server;

/**
 * Imports
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.ejb.session.UtilityLocal;
import uk.ac.stfc.topcat.gwt.client.model.ParameterModel;

/**
 * Servlet used on server side to copy the parameter values to CSV format file that can be 
 * downloaded by browser.
 * 
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@SuppressWarnings("serial")
public class CopyDataToCSVFile extends HttpServlet {
	private UtilityLocal utilityManager = null;

	/**
	 * Servlet Init method
	 */
	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
		
		try{
			//create initial context
			Context ctx = new InitialContext();
			//Use utitlity session bean manager for getting parameter information
			utilityManager = (UtilityLocal) ctx.lookup("java:global/TopCAT/UtilityBean!uk.ac.stfc.topcat.ejb.session.UtilityLocal");
		}catch(NamingException ex){
			ex.printStackTrace();
		}
	}
	
    /**
     * Servlet post method ovveride function which does the extraction of parameters information of datafile 
     * using the facility name and datafile id from the post request parameters.
     * @param request Servlet request object.
     * @param response Servlet response object
     */
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		System.out.println("Performing request for the parameter csv file conversion");
		String facilityName = request.getParameter("FacilityName");
		String datafileId = request.getParameter("DatafileId");
		ArrayList<ParameterModel> paramList = getDatafileParameters((String)request.getSession().getAttribute("SESSION_ID"),facilityName, datafileId);
		response.setContentType("text/csv");
		response.setHeader("Content-Disposition", "attachment; filename=parameter.csv");		
		try {
			PrintWriter out = response.getWriter();
			out.println("Name,Units,Value");
			for(ParameterModel param:paramList){
				out.println(param.getName()+","+param.getUnits()+","+param.getValue());
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/**
	 * This method returns creates a parameter model which has list of parameter names and corresponding values
	 * for a given datafile id on a server.
	 * @param sessionId Topcat session id
	 * @param facilityName Facility Name
	 * @param datafileId datafile ICAT Id
	 * @return a list of parameters that are associated with the method input.
	 */
	public ArrayList<ParameterModel> getDatafileParameters(String sessionId,String facilityName,
			String datafileId) {
		ArrayList<ParameterModel> result = new ArrayList<ParameterModel>();
		ArrayList<TDatafileParameter> df = utilityManager.getDatafileInfoInServer(sessionId,facilityName,datafileId);
		if(df==null)return result;
		for(TDatafileParameter dfParam : df) {
				result.add(new ParameterModel(dfParam.getName(),dfParam.getUnits(),dfParam.getValue()));
		}
		return result;
	}	
}
