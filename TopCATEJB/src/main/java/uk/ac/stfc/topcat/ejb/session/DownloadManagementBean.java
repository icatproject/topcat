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

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import uk.ac.stfc.topcat.core.gwt.module.exception.TopcatException;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserDownload;
import uk.ac.stfc.topcat.ejb.manager.DownloadManager;

/**
 * This is an download bean implementation which is used to get and set data
 * about user download requests.
 */
@Stateless
public class DownloadManagementBean implements DownloadManagementBeanLocal {
    private DownloadManager downloadManager;
    @PersistenceContext(unitName = "TopCATEJBPU")
    protected EntityManager manager;

    public DownloadManagementBean() {
        downloadManager = new DownloadManager();
    }

    @Override
    public Long add(String sessionId, String facilityName, Date submitTime, String downloadName, String status, String message,
            Date expiryTime, String url, String preparedId) throws TopcatException {
        return downloadManager.add(manager, sessionId, facilityName, submitTime, downloadName, status, message, expiryTime, url,
                preparedId);
    }

    @Override
    public void delete(String sessionId, Long id) {
        downloadManager.delete(manager, sessionId, id);
    }


    @Deprecated
    @Override
    public String getDatafilesDownloadURL(String sessionId, String facilityName, List<Long> datafileIds)
            throws TopcatException {
        return downloadManager.getDatafilesDownloadURL(manager, sessionId, facilityName, datafileIds);
    }

    @Deprecated
    @Override
    public String getDatasetDownloadURL(String sessionId, String facilityName, Long datasetId) throws TopcatException {
        return downloadManager.getDatasetDownloadURL(manager, sessionId, facilityName, datasetId);
    }


    @Override
    public List<TopcatUserDownload> getMyDownloads(String sessionId, String facilityName) throws TopcatException {
        return downloadManager.getMyDownloads(manager, sessionId, facilityName);
    }

    @Override
    public void update(String sessionId, Long id, String url, String status, String message) {
        downloadManager.update(manager, sessionId, id, url, status, message);
    }

    @Override
    public void update(String sessionId, Long id, String url, String status) {
        downloadManager.update(manager, sessionId, id, url, status);
    }




    @Override
    public String getUrl(String facilityName) throws TopcatException {
        return downloadManager.getUrl(manager, facilityName);
    }

    /*
    @Deprecated
    @Override
    public void updateDownloadStatus(String sessionId, String facilityName, String url, String updatedUrl, String status) {
        downloadManager.updateDownloadStatus(manager, sessionId, facilityName, url, updatedUrl, status);
    }
    */

    @Override
    public void updateExpiryTime(String sessionId, Long id, Date expiryTime) {
        downloadManager.updateExpiryTime(manager, sessionId, id, expiryTime);
    }


}
