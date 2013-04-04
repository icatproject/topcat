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
package uk.ac.stfc.topcat.gwt.client.model;

/**
 * Imports
 */
import java.io.Serializable;
import java.util.Date;

import uk.ac.stfc.topcat.gwt.client.Constants;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 */
@SuppressWarnings("serial")
public class DownloadModel extends BaseModelData implements Serializable {
    /** The initial URL. This is used in the equal operator of the object */
    private String m_ulr;

    public DownloadModel() {
    }

    /**
     * Constructor to set download information.
     * 
     * @param facilityName
     * @param submitTime
     * @param downloadName
     * @param status
     * @param expiryTime
     * @param url
     */
    public DownloadModel(String facilityName, Date submitTime, String downloadName, String status, Date expiryTime,
            String url) {
        setFacilityName(facilityName);
        setSubmitTime(submitTime);
        setDownloadName(downloadName);
        setStatus(status);
        setExpiryTime(expiryTime);
        setUrl(url);
        setTimeRemaining(expiryTime);
        m_ulr = url;
    }

    /**
     * Constructor to set download information.
     * 
     * @param facilityName
     *            facility name
     * @param url
     *            url of the download service
     * @param preparedId
     *            id of the download on the download service
     * @param downloadName
     *            the name given by the user
     * @param submitTime
     *            the time the download was requested
     * @param status
     *            the status of the download from the server
     */
    public DownloadModel(String facilityName, String url, String preparedId, String downloadName, Date submitTime,
            String status) {
        setFacilityName(facilityName);
        setUrl(url);
        setPreparedId(preparedId);
        setDownloadName(downloadName);
        setSubmitTime(submitTime);
        setStatus(status);
        setUrl(url);
        m_ulr = url;
    }

    /**
     * @return downloadName
     */
    public String getDownloadName() {
        return get("downloadName");
    }

    /**
     * @return expiryTime
     */
    public Date getExpiryTime() {
        return get("expiryTime");
    }

    /**
     * @return facility name
     */
    public String getFacilityName() {
        return get("facilityName");
    }

    /**
     * @return preparedId
     */
    public String getPreparedId() {
        return get("preparedId");
    }

    /**
     * @return status
     */
    public String getStatus() {
        return get("status");
    }

    /**
     * @return submitTime
     */
    public Date getSubmitTime() {
        return get("submitTime");
    }

    /**
     * @return timeRemaining
     */
    public String getTimeRemaining() {
        return get("timeRemaining");
    }

    /**
     * @return url
     */
    public String getUrl() {
        return get("url");
    }

    /**
     * Refresh the time remaining.
     */
    public void refresh() {
        setTimeRemaining(getExpiryTime());
    }

    /**
     * Set the download name
     * 
     * @param downloadName
     */
    public void setDownloadName(String downloadName) {
        set("downloadName", downloadName);
    }

    /**
     * Set expiryTime
     * 
     * @param expiryTime
     */
    public void setExpiryTime(Date expiryTime) {
        set("expiryTime", expiryTime);
    }

    /**
     * Set the facility name
     * 
     * @param facilityName
     */
    public void setFacilityName(String facilityName) {
        set("facilityName", facilityName);
    }

    /**
     * Set preparedId
     * 
     * @param preparedId
     */
    public void setPreparedId(String preparedId) {
        set("preparedId", preparedId);
    }

    /**
     * Set status
     * 
     * @param status
     */
    public void setStatus(String status) {
        set("status", status);
    }

    /**
     * Set the submitTime
     * 
     * @param submitTime
     */
    public void setSubmitTime(Date submitTime) {
        set("submitTime", submitTime);
    }

    /**
     * Set timeRemaining
     */
    public void setTimeRemaining(Date expiryTime) {
        StringBuilder result = new StringBuilder();
        if (expiryTime != null) {
            long reminingInSeconds = (expiryTime.getTime() - System.currentTimeMillis()) / 1000;
            if (reminingInSeconds < 0) {
                set("timeRemaining", Constants.STATUS_EXPIRED);
                setStatus(Constants.STATUS_EXPIRED);
                return;
            }
            long diff[] = new long[] { 0, 0, 0 };
            /* min */diff[2] = (reminingInSeconds = (reminingInSeconds / 60)) >= 60 ? reminingInSeconds % 60
                    : reminingInSeconds;
            /* hours */diff[1] = (reminingInSeconds = (reminingInSeconds / 60)) >= 24 ? reminingInSeconds % 24
                    : reminingInSeconds;
            /* days */diff[0] = (reminingInSeconds = (reminingInSeconds / 24));
            result.append(diff[0]).append(" day");
            if (diff[0] > 1) {
                result.append("s");
            }
            result.append(", ").append(diff[1]).append(" hour");
            if (diff[1] > 1) {
                result.append("s");
            }
            result.append(", ").append(diff[2]).append(" minute");
            if (diff[2] > 1) {
                result.append("s");
            }
        }
        set("timeRemaining", result.toString());
    }

    /**
     * Set the url
     * 
     * @param url
     */
    public void setUrl(String url) {
        set("url", url);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_ulr == null) ? 0 : m_ulr.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DownloadModel other = (DownloadModel) obj;
        if (m_ulr == null) {
            if (other.m_ulr != null)
                return false;
        } else if (!m_ulr.equals(other.m_ulr))
            return false;
        return true;
    }

}
