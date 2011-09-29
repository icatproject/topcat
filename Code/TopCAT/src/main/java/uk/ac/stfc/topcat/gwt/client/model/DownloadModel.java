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
import java.io.Serializable;
import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 */
@SuppressWarnings("serial")
public class DownloadModel extends BaseModelData implements Serializable {
    public DownloadModel() {

    }

    /**
     * Constructor to set dataset information
     * 
     * @param facilityName
     *            facility name
     * @param id
     *            id
     * @param url
     *            url
     */
    public DownloadModel(String facilityName, Date submitTime, String downloadName, String status, long validPeriod,
            String url) {
        setFacilityName(facilityName);
        setSubmitTime(submitTime);
        setDownloadName(downloadName);
        setStatus(status);
        setValidPeriod(validPeriod);
        setUrl(url);
        setTimeRemaining(validPeriod);
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
     * Set the submitTime
     * 
     * @param submitTime
     */
    public void setSubmitTime(Date submitTime) {
        set("submitTime", submitTime);
    }

    /**
     * Set the downloadName
     * 
     * @param downloadName
     */
    public void setDownloadName(String downloadName) {
        set("downloadName", downloadName);
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
     * Set validPeriod
     * 
     * @param validPeriod
     */
    public void setValidPeriod(Long validPeriod) {
        set("validPeriod", validPeriod);
    }

    /**
     * Set url
     * 
     * @param url
     */
    public void setUrl(String url) {
        set("url", url);
    }

    /**
     * Set timeRemaining
     */
    public void setTimeRemaining(long validPeriod) {
        long elapsedTime = System.currentTimeMillis() - getSubmitTime().getTime();
        long diffInSeconds = (validPeriod - elapsedTime) / 1000;
        if (diffInSeconds < 0) {
            set("timeRemaining", "expired");
            setStatus("expired");
            return;
        }
        long diff[] = new long[] { 0, 0, 0 };
        /* min */diff[2] = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        /* hours */diff[1] = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        /* days */diff[0] = (diffInSeconds = (diffInSeconds / 24));
        StringBuilder result = new StringBuilder();
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
        set("timeRemaining", result.toString());
    }

    /**
     * @return facility name
     */
    public String getFacilityName() {
        return get("facilityName");
    }

    /**
     * @return submitTime
     */
    public Date getSubmitTime() {
        return get("submitTime");
    }

    /**
     * @return downloadName
     */
    public String getDownloadName() {
        return get("downloadName");
    }

    /**
     * @return status
     */
    public String getStatus() {
        return get("status");
    }

    /**
     * @return validPeriod
     */
    public long getValidPeriod() {
        return get("validPeriod");
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
        setTimeRemaining(getValidPeriod());
    }

}
