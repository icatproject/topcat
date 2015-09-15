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
import java.util.List;
import java.util.Set;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.gwt.client.model.DownloadModel;
import uk.ac.stfc.topcat.gwt.shared.IdsFlag;
import uk.ac.stfc.topcat.gwt.shared.model.TopcatDataSelection;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The <code>DownloadService</code> interface is used to perform download
 * operations.
 *
 */
@RemoteServiceRelativePath("DownloadService")
public interface DownloadService extends RemoteService {
    /**
     * Download class for simplifying access to the instance of async service.
     */
    public static class Util {
        private static DownloadServiceAsync instance;

        public static DownloadServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(DownloadService.class);
            }
            return instance;
        }
    }

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
     * @throws TopcatException
     */
    /*
    @Deprecated
    public DownloadModel getDatafilesDownloadURL(String facilityName, List<Long> datafileIds, String downloadName)
            throws TopcatException;
    */

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
     * @throws TopcatException
     */
    /*
    @Deprecated
    public DownloadModel getDatasetDownloadURL(String facilityName, Long datasetId, String downloadName)
            throws TopcatException;
    */


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
    public List<DownloadModel> checkForFinalStatus(Set<DownloadModel> downloadQueue) throws TopcatException;

    /**
     * Get a list of downloads for the user.
     *
     * @param facilities
     *            a set containing the facility names
     *
     * @return a list of <code>DownloadModel</code>
     * @throws TopcatException
     */
    public List<DownloadModel> getMyDownloads(Set<String> facilities) throws TopcatException;

    /**
     * Contact the I.D.S. and prepare the download of the given data objects.
     *
     * @param dataType
     *            the type of the data object to be downloaded
     * @param facility
     *            the facility data
     * @param dataObjectList
     *            a list of data object ids
     * @param downloadName
     *            the name to give the down load file
     * @throws TopcatException
     */
    public DownloadModel prepareDataObjectsForDownload(TFacility facility, TopcatDataSelection dataSelection,
            String downloadName, IdsFlag flag) throws TopcatException;


    /**
     * Contact the I.D.S. and initiate the download of the given data objects.
     *
     * @param dataType
     *            the type of the data object to be downloaded
     * @param facility
     *            the facility data
     * @param dataObjectList
     *            a list of data object ids
     * @param downloadName
     *            the name to give the down load file
     * @throws TopcatException
     */
    public DownloadModel directDownloadFromIDS(TFacility facility, TopcatDataSelection dataSelection, String downloadName, IdsFlag flag)
            throws TopcatException;



    /**
     * Contact the server and delete a given download.
     *
     * @param facility
     *            the facility data
     * @param DownloadModel
     *            download to delete
     * @throws TopcatException
     */
    public boolean deleteDownload(String facility, DownloadModel downloadModel) throws TopcatException;


    /**
     * Get the file size from the ids
     *
     * @param facility
     *            the facility data
     * @param DownloadModel
     *            download to delete
     * @throws TopcatException
     */
    public Long getDataSelectionSize(TFacility facility, TopcatDataSelection topcatDataSelection) throws TopcatException;



}
