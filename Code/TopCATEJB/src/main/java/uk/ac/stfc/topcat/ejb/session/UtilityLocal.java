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
package uk.ac.stfc.topcat.ejb.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import uk.ac.stfc.topcat.core.exception.AuthenticationException;
import uk.ac.stfc.topcat.core.exception.ICATMethodNotFoundException;
import uk.ac.stfc.topcat.core.gwt.module.TDatafile;
import uk.ac.stfc.topcat.core.gwt.module.TDatafileParameter;
import uk.ac.stfc.topcat.core.gwt.module.TDataset;
import uk.ac.stfc.topcat.core.gwt.module.TDatasetParameter;
import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.core.gwt.module.TFacilityCycle;
import uk.ac.stfc.topcat.core.gwt.module.TInvestigation;
import uk.ac.stfc.topcat.core.gwt.module.TopcatException;
import uk.ac.stfc.topcat.ejb.entity.TopcatUserDownload;

/**
 * This is local interface to utility bean
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */

@Local
public interface UtilityLocal {

    ArrayList<TFacility> getFacilities();

    ArrayList<String> getFacilityNames();

    ArrayList<String> getInstrumentNames(String sessionId, String serverName);

    ArrayList<String> getInvestigationTypes(String sessionId, String serverName);

    ArrayList<TFacilityCycle> getFacilityCyclesWithInstrument(String sessionId, String serverName, String instrument)
            throws ICATMethodNotFoundException;

    ArrayList<TInvestigation> getMyInvestigationsInServer(String sessionId, String serverName);

    ArrayList<TInvestigation> getMyInvestigationsInServerAndInstrument(String sessionId, String serverName,
            String instrumentName) throws TopcatException;

    ArrayList<TInvestigation> getAllInvestigationsInServerAndInstrument(String sessionId, String serverName,
            String instrumentName) throws TopcatException;

    ArrayList<TInvestigation> getMyInvestigationsInServerInstrumentAndCycle(String sessionId, String serverName,
            String instrumentName, TFacilityCycle cycle) throws TopcatException;

    ArrayList<TInvestigation> getAllInvestigationsInServerInstrumentAndCycle(String sessionId, String serverName,
            String instrumentName, TFacilityCycle cycle) throws TopcatException;

    TInvestigation getInvestigationDetails(String sessionId, String serverName, String investigationId)
            throws AuthenticationException;

    ArrayList<TDataset> getDatasetsInServer(String sessionId, String serverName, String investigationId)
            throws AuthenticationException;

    String getDatasetName(String sessionId, String serverName, String datasetId);

    ArrayList<TDatafile> getDatafilesInServer(String sessionId, String serverName, String datasetId);

    ArrayList<TDatasetParameter> getDatasetInfoInServer(java.lang.String sessionId, java.lang.String serverName,
            java.lang.String datasetId);

    ArrayList<TDatafileParameter> getDatafileInfoInServer(java.lang.String sessionId, java.lang.String serverName,
            java.lang.String datafileId);

    ArrayList<String> getParameterNames(String sessionId, String facilityName) throws TopcatException;

    ArrayList<String> getParameterUnits(String sessionId, String facilityName, String name) throws TopcatException;

    ArrayList<String> getParameterTypes(String sessionId, String facilityName, String name, String units)
            throws TopcatException;

    String getDatafilesDownloadURL(String sessionId, String serverName, ArrayList<Long> datafileIds);

    String getDatasetDownloadURL(String sessionId, String serverName, Long datasetId);

    List<TopcatUserDownload> getMyDownloadList(String sessionId, String serverName);

    void addMyDownload(String sessionId, String facilityName, Date submitTime, String downloadName, String status,
            Date expiryTime, String url);

    void updateDownloadStatus(String sessionId, String facilityName, String url, String updatedUrl, String status);
}
