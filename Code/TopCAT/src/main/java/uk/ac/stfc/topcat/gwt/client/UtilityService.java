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
import uk.ac.stfc.topcat.gwt.client.exception.SessionException;
import uk.ac.stfc.topcat.gwt.client.model.DatafileModel;
import uk.ac.stfc.topcat.gwt.client.model.DatasetModel;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;
import uk.ac.stfc.topcat.gwt.client.model.ICATNode;
import uk.ac.stfc.topcat.gwt.client.model.ParameterModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;

/**
 * The <code>UtilityService</code> interface is used to perform searches to get
 * the number of facilities in TopCAT, their names, instrument names etc..
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@RemoteServiceRelativePath("UtilityService")
public interface UtilityService extends RemoteService {
    /**
     * Utility class for simplifying access to the instance of async service.
     */
    public static class Util {
        private static UtilityServiceAsync instance;

        public static UtilityServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(UtilityService.class);
            }
            return instance;
        }
    }

    /**
     * Get all facility(iCAT instances) objects.
     * 
     * @return a list of facilities
     */
    public ArrayList<TFacility> getFacilities();

    /**
     * Get a list of facilities registered in TopCAT.
     * 
     * @return a list of strings containing facility names
     */
    public ArrayList<String> getFacilityNames();

    /**
     * Get a list of instrument names for the given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @return a list of strings containing instrument names
     */
    public ArrayList<String> getInstrumentNames(String facilityName);

    /**
     * Get a list of investigation types for the given facility.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @return a list of strings containing investigation types
     */
    public ArrayList<String> getInvestigationTypes(String facilityName);

    /**
     * Get a list of all children for the given ICAT node for which the user has
     * investigation rights.
     * 
     * @param node
     *            a <code>ICATNode</code> containing the parent information
     * @return a list of child <code>ICATNode</code>
     */
    public ArrayList<ICATNode> getMyICATNodeChildren(ICATNode node);

    /**
     * Get a list of all children for the given ICAT node.
     * 
     * @param node
     *            a <code>ICATNode</code> containing the parent information
     * @return a list of child <code>ICATNode</code>
     */
    public ArrayList<ICATNode> getAllICATNodeChildren(ICATNode node);

    /**
     * TODO
     * 
     * @param node
     * @return a map with the key as a string containing TODO and the value as a
     *         list of <code>ICATNode</code>
     */
    public HashMap<String, ArrayList<ICATNode>> getMyICATNodeDatafiles(ICATNode node);

    /**
     * TODO
     * 
     * @param node
     * @return a map with the key as a string containing TODO and the value as a
     *         list of <code>ICATNode</code>
     */
    public HashMap<String, ArrayList<ICATNode>> getAllICATNodeDatafiles(ICATNode node);

    /**
     * Get a list of parameter models which have parameter names and
     * corresponding values for a given facility and data file id.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datafileId
     *            a string containing the data file id
     * @return a list of <code>ParameterModel</code> which contain parameter
     *         names and corresponding values
     */
    public ArrayList<ParameterModel> getDatafileParameters(String facilityName, String datafileId);

    /**
     * Get a list of data sets for the given facility and investigation.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param investigationId
     *            a string containing the investigation id
     * @return a list of <code>DatasetModel</code> containing data set
     *         information
     */
    public ArrayList<DatasetModel> getDatasetsInInvestigations(String facilityName, String investigationId);

    /**
     * Get a list of parameter models which have parameter names and
     * corresponding values for a given facility and data set id.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param datasetId
     *            a string containing the data set id
     * @return a list of <code>ParameterModel</code> which contain parameter
     *         names and corresponding values
     */
    public ArrayList<ParameterModel> getDatasetParameters(String facilityName, String datasetId);

    /**
     * Get a list of data files information corresponding to the given list of
     * data sets.
     * 
     * @param datasets
     *            a list of <code>DatasetModel</code>
     * @return a list of <code>DatafileModel</code> containing data files
     */
    public ArrayList<DatafileModel> getDatafilesInDatasets(ArrayList<DatasetModel> datasets);

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
     * @return a DownloadModel
     */
    public DownloadModel getDatafilesDownloadURL(String facilityName, ArrayList<Long> datafileIds, String downloadName);

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
     * @return a DownloadModel
     */
    public DownloadModel getDatasetDownloadURL(String facilityName, Long datasetId, String downloadName);

    /**
     * Get a list of investigations for the given facility that belong to the
     * user.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @return a list of <code>TInvestigation</code> containing investigations
     */
    public ArrayList<TInvestigation> getMyInvestigationsInServer(String facilityName);

    /**
     * Get additional details about an investigation.
     * 
     * @param facilityName
     *            a string containing the facility name
     * @param investigationId
     *            the investigation id
     * @return a <code>TInvestigation</code> containing additional data
     * @throws SessionException
     */
    public TInvestigation getInvestigationDetails(String facilityName, long investigationId) throws SessionException;

    /**
     * This method returns the server logo URL
     * 
     * @return
     */
    public String getLogoURL();

    /**
     * This method returns the links to be used at the bottom of the page.
     * 
     * @return a map of link name, link
     */
    public Map<String, String> getLinks();

    /**
     * Get a list of downloads for the user.
     * 
     * @param facilities
     *            a set containing the facility names
     * 
     * @return a list of <code>DownloadModel</code>
     */
    public ArrayList<DownloadModel> getMyDownloadList(Set<String> facilities);

    /**
     * Check all of the models for a final status, 'available' or 'ERROR', from
     * the download service.
     * 
     * @param downloadModels
     *            a set of models of the items that have already been requested
     *            from the download service
     * @return a list of <code>DownloadModel</code> that do NOT have a final
     *         status
     */
    public List<DownloadModel> getDownloadStatus(Set<DownloadModel> downloadQueue);

}
