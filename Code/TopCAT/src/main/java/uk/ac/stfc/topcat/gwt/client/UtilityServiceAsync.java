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
package uk.ac.stfc.topcat.gwt.client;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.gwt.client.model.AuthenticationModel;
import uk.ac.stfc.topcat.gwt.client.model.DatafileFormatModel;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ParameterModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The <code>UtilityServiceAsync</code> interface is used to perform
 * asynchronous searches to get the number of facilities in TopCAT, their names,
 * instrument names etc..
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public interface UtilityServiceAsync {

    /**
     * Get all facility(iCAT instances) objects.
     * 
     * @param callback
     *            object to be called on completion
     */
    public void getFacilities(AsyncCallback<ArrayList<TFacility>> callback);

    /**
     * Get a list of instrument names for the given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void getInstrumentNames(String facilityName, AsyncCallback<ArrayList<String>> callback);

    /**
     * Get a list of investigation types for the given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void getInvestigationTypes(String facilityName, AsyncCallback<ArrayList<String>> callback);

    /**
     * TODO
     * 
     * @param node
     * @param callback
     *            object to be called on completion
     */
    public void getAllICATNodeDatafiles(ICATNode node, AsyncCallback<HashMap<String, ArrayList<ICATNode>>> callback);

    /**
     * Get a list of parameter models which have parameter names and
     * corresponding values for a given facility and data file id.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datafileId
     *            a string containing the data file id
     * @param callback
     *            object to be called on completion
     */
    public void getDatafileParameters(String facilityName, String datafileId,
            AsyncCallback<ArrayList<ParameterModel>> callback);

    /**
     * Get a list of data sets for the given facility and investigation.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param investigationId
     *            a string containing the investigation id
     * @param callback
     *            object to be called on completion
     */
    public void getDatasetsInInvestigations(String facilityName, String investigationId,
            AsyncCallback<ArrayList<DatasetModel>> callback);

    /**
     * Get a list of parameter models which have parameter names and
     * corresponding values for a given facility and data set id.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            a string containing the data set id
     * @param callback
     *            object to be called on completion
     */
    public void getDatasetParameters(String facilityName, String datasetId,
            AsyncCallback<ArrayList<ParameterModel>> callback);

    /**
     * Get a list of data files information corresponding to the given list of
     * data sets.
     * 
     * @param datasets
     *            a list of <code>DatasetModel</code>
     * @param callback
     *            object to be called on completion
     */
    public void getDatafilesInDatasets(ArrayList<DatasetModel> datasets,
            AsyncCallback<ArrayList<DatafileModel>> callback);

    /**
     * Get the URL of a file that contains all the requested data files for the
     * given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datafileIds
     *            a list containing data file ids
     * @param downloadName
     *            a string containing a user defined name
     * @param callback
     *            object to be called on completion
     */
    public void getDatafilesDownloadURL(String facilityName, ArrayList<Long> datafileIds, String downloadName,
            AsyncCallback<DownloadModel> callback);

    /**
     * Get the URL of a file that contains the requested data set for the given
     * facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            the data set id
     * @param downloadName
     *            a string containing a user defined name
     * @param callback
     *            object to be called on completion
     */
    public void getDatasetDownloadURL(String facilityName, Long datasetId, String downloadName,
            AsyncCallback<DownloadModel> callback);

    /**
     * Get a list of investigations for the given facility that belong to the
     * user.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void getMyInvestigationsInServer(String facilityName, AsyncCallback<ArrayList<TInvestigation>> asyncCallback);

    /**
     * Get additional details about an investigation.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param investigationId
     *            the investigation id
     * @param asyncCallback
     *            object to be called on completion
     */
    public void getInvestigationDetails(String facilityName, String investigationId,
            AsyncCallback<TInvestigation> asyncCallback);

    /**
     * Get the server logo URL
     * 
     * @param asyncCallback
     */
    public void getLogoURL(AsyncCallback<String> asyncCallback);

    /**
     * This method returns the links to be used at the bottom of the page.
     * 
     * @param callback
     *            object to be called on completion
     */
    public void getLinks(AsyncCallback<Map<String, String>> asyncCallback);

    /**
     * Get a list of downloads for the user.
     * 
     * @param facilities
     *            a set containing the facility names
     * @param callback
     *            object to be called on completion
     */
    public void getMyDownloadList(Set<String> facilities, AsyncCallback<ArrayList<DownloadModel>> asyncCallback);

    /**
     * Check all of the models for a final status, 'available' or 'ERROR', from
     * the download service.
     * 
     * @param downloadModels
     *            a set of models of the items that have already been requested
     *            from the download service
     * @param callback
     *            object to be called on completion
     */
    public void getDownloadStatus(Set<DownloadModel> downloadQueue, AsyncCallback<List<DownloadModel>> asyncCallback);

    /**
     * Get a list of parameter names known to a facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void getParameterNames(String facilityName, AsyncCallback<ArrayList<String>> callback);

    /**
     * Get a list of parameter units for the given facility and parameter name.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param name
     *            a string containing the parameter name
     * @param callback
     *            object to be called on completion
     */
    public void getParameterUnits(String facilityName, String name, AsyncCallback<ArrayList<String>> callback);

    /**
     * Get the expected type of the parameter value for the given facility,
     * parameter name and parameter units. If the units are '--ALL--' then
     * return types for all units.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param name
     *            a string containing the parameter name
     * @param units
     *            a string containing the parameter units
     * @param callback
     *            object to be called on completion
     */
    public void getParameterTypes(String facilityName, String name, String units,
            AsyncCallback<ArrayList<String>> callback);

    /**
     * Get the known authentication types for the given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void getAuthenticationTypes(String facilityName, AsyncCallback<List<AuthenticationModel>> callback);

    /**
     * Get a list of data set types for a given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     *            object to be called on completion
     */
    public void getDatasetTypes(String facilityName, AsyncCallback<List<String>> callback);

    /**
     * Get a list of data file formats for a given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param callback
     */
    public void getDatafileFormats(String facilityName, AsyncCallback<List<DatafileFormatModel>> callback);

}
