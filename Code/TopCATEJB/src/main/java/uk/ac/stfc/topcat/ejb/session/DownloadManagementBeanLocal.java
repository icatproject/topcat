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
package uk.ac.stfc.topcat.ejb.session;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserDownload;

/**
 * This is local interface to the download bean.
 */

@Local
public interface DownloadManagementBeanLocal {

    /**
     * Add a new record.
     * 
     * @param topcatSessionId
     *            a string containing the session id
     * @param facilityName
     *            a string containing the facility name
     * @param submitTime
     * @param downloadName
     * @param status
     * @param expiryTime
     * @param url
     * @param preparedId
     * @return the id of the download
     * @throws TopcatException
     */
    Long add(String sessionId, String facilityName, Date submitTime, String downloadName, String status,
            Date expiryTime, String url, String preparedId) throws TopcatException;

    /**
     * Delete the record with the given id.
     * 
     * @param manager
     * @param topcatSessionId
     *            a string containing the session id
     * @param id
     *            the id of the record to delete
     */
    void delete(String sessionId, Long id);

    @Deprecated
    String getDatafilesDownloadURL(String sessionId, String facilityName, List<Long> datafileIds)
            throws TopcatException;

    @Deprecated
    String getDatasetDownloadURL(String sessionId, String facilityName, Long datasetId) throws TopcatException;

    /**
     * Get a list of downloads for a user, which are associated with the given
     * facility.
     * 
     * @param manager
     * @param topcatSessionId
     *            a string containing the session id
     * @param facilityName
     *            a string containing the facility name
     * @return a list of <code>TopcatUserDownload</code>
     * @throws TopcatException
     */
    List<TopcatUserDownload> getMyDownloads(String sessionId, String facilityName) throws TopcatException;

    /**
     * Update the status and the url of the record with the given id.
     * 
     * @param manager
     * @param topcatSessionId
     *            a string containing the session id
     * @param id
     *            the id of the record to update
     * @param url
     *            the updated url
     * @param status
     *            the updated status
     */
    void update(String sessionId, Long id, String url, String status);

    /**
     * Get the URL of the download server for the given facility
     * 
     * @param facilityName
     * @return the URL of the download server
     * @throws TopcatException
     */
    String getUrl(String facilityName) throws TopcatException;

    @Deprecated
    void updateDownloadStatus(String sessionId, String facilityName, String url, String updatedUrl, String status);

}
