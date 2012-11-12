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
package uk.ac.stfc.topcat.core.gwt.module;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is shared with GWT for advanced search details.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class TAdvancedSearchDetails implements Serializable {

    protected String propostaltitle;
    protected String proposalAbstract;
    protected String sample;
    protected ArrayList<String> investigatorNameList;
    protected String datafileName;
    protected Date startDate;
    protected Date endDate;
    protected String rbNumberStart;
    protected String rbNumberEnd;
    protected String grantId;
    protected ArrayList<String> facilityList;
    protected ArrayList<String> investigationTypeList;
    protected ArrayList<String> instrumentList;
    protected Boolean datafileNameCaseSensitive;
    protected ArrayList<String> keywordsList;
    protected String parameterName;
    protected String parameterUnits;
    protected String parameterValue;
    protected String parameterValueMax;
    protected boolean searchAllData = false;

    public TAdvancedSearchDetails() {
        investigatorNameList = new ArrayList<String>();
        facilityList = new ArrayList<String>();
        investigationTypeList = new ArrayList<String>();
        instrumentList = new ArrayList<String>();
        keywordsList = new ArrayList<String>();
    }

    public String getDatafileName() {
        return datafileName;
    }

    public void setDatafileName(String datafileName) {
        this.datafileName = datafileName;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public ArrayList<String> getFacilityList() {
        return facilityList;
    }

    public void setFacilityList(ArrayList<String> facilityList) {
        this.facilityList = facilityList;
    }

    public String getGrantId() {
        return grantId;
    }

    public void setGrantId(String grantId) {
        this.grantId = grantId;
    }

    public ArrayList<String> getInstrumentList() {
        return instrumentList;
    }

    public void setInstrumentList(ArrayList<String> instrumentList) {
        if (instrumentList != null)
            this.instrumentList = instrumentList;
    }

    public ArrayList<String> getInvestigationTypeList() {
        return investigationTypeList;
    }

    public void setInvestigationTypeList(ArrayList<String> investigationTypeList) {
        if (investigationTypeList != null)
            this.investigationTypeList = investigationTypeList;
    }

    public ArrayList<String> getInvestigatorNameList() {
        return investigatorNameList;
    }

    public void setInvestigatorNameList(ArrayList<String> investigatorNames) {
        this.investigatorNameList = investigatorNames;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String name) {
        this.parameterName = name;
    }

    public String getParameterUnits() {
        return parameterUnits;
    }

    public void setParameterUnits(String units) {
        this.parameterUnits = units;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String value) {
        this.parameterValue = value;
    }

    public String getParameterValueMax() {
        return parameterValueMax;
    }

    public void setParameterValueMax(String value) {
        this.parameterValueMax = value;
    }

    public String getProposalAbstract() {
        return proposalAbstract;
    }

    public void setProposalAbstract(String proposalAbstract) {
        this.proposalAbstract = proposalAbstract;
    }

    public String getPropostaltitle() {
        return propostaltitle;
    }

    public void setPropostaltitle(String propostaltitle) {
        this.propostaltitle = propostaltitle;
    }

    public boolean getSearchAllData() {
        return searchAllData;
    }

    public void setSearchAllData(boolean searchAllData) {
        this.searchAllData = searchAllData;
    }

    public String getRbNumberStart() {
        return rbNumberStart;
    }

    public void setRbNumberStart(String rbNumberStart) {
        this.rbNumberStart = rbNumberStart;
    }

    public String getRbNumberEnd() {
        return rbNumberEnd;
    }

    public void setRbNumberEnd(String rbNumberEnd) {
        this.rbNumberEnd = rbNumberEnd;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public ArrayList<String> getKeywords() {
        return keywordsList;
    }

    public void setKeywords(List<String> keywords) {
        if (keywords != null) {
            this.keywordsList.clear();
            this.keywordsList.addAll(keywords);
        }
    }
}
