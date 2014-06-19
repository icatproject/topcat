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
package uk.ac.stfc.topcat.core.gwt.module;

import java.io.Serializable;

/**
 * This is shared with GWT for facility information.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class TFacility implements Serializable {
    private static final long serialVersionUID = 1L;
    private String downloadPluginName;
    private String downloadTypeName;
    private String downloadServiceUrl;
    private boolean allowUpload;
    private boolean allowCreateDataset;
    private String name;
    private String searchPluginName;
    private String url;
    private String version;
    private Long id;

    public TFacility() {
    }

    public String getDownloadPluginName() {
        return downloadPluginName;
    }

    public void setDownloadPluginName(String downloadPluginName) {
        this.downloadPluginName = downloadPluginName;
    }
    
    public String getDownloadTypeName() {
        return downloadTypeName;
    }

    public void setDownloadTypeName(String downloadTypeName) {
        this.downloadTypeName = downloadTypeName;
    }

    public String getDownloadServiceUrl() {
        return downloadServiceUrl;
    }

    public void setDownloadServiceUrl(String downloadServiceUrl) {
        this.downloadServiceUrl = downloadServiceUrl;
    }    
    
    public boolean isAllowUpload() {
        return allowUpload;
    }

    public void setAllowUpload(boolean allowUpload) {
        this.allowUpload = allowUpload;
    }

    public boolean isAllowCreateDataset() {
        return allowCreateDataset;
    }

    public void setAllowCreateDataset(boolean allowCreateDataset) {
        this.allowCreateDataset = allowCreateDataset;
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSearchPluginName() {
        return searchPluginName;
    }

    public void setSearchPluginName(String searchPluginName) {
        this.searchPluginName = searchPluginName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();
        message.append("TFacility\n");
        message.append("id:").append(id).append("\n");
        message.append("name:").append(name).append("\n");
        message.append("url:").append(url).append("\n");
        message.append("version:").append(version).append("\n");
        message.append("searchPluginName:").append(searchPluginName).append("\n");
        message.append("downloadPluginName:").append(downloadPluginName).append("\n");
        message.append("downloadTypeName:").append(downloadTypeName).append("\n");
        message.append("downloadServiceUrl:").append(downloadServiceUrl);
        
        return message.toString();
    }

    
}
