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
import java.util.Date;
import java.util.List;

/**
 * This is shared with GWT for dataset information. This Class holds the icat
 * investigation and the server to which it is associated
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class TInvestigation implements Serializable, Comparable<TInvestigation> {
    private static final long serialVersionUID = 1L;
    private String investigationId = null;
    private String investigationName = null;
    private String serverName = null;
    private Date startDate = null;
    private Date endDate = null;
    private String title = null;
    private String visitId = null;
    private String instrument = null;
    private String proposal = null;
    private List<TParameter> parameters = null;
    private List<TPublication> publications = null;
    private List<TInvestigator> investigators = null;
    private List<TShift> shifts = null;

    public TInvestigation() {
    }

    public TInvestigation(String invId, String inv, String server) {
        investigationId = invId;
        investigationName = inv;
        serverName = server;
    }

    public TInvestigation(String invId, String invName, String server, String title, Date startDate, Date endDate,
            String visitId) {
        investigationId = invId;
        investigationName = invName;
        serverName = server;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.visitId = visitId;
    }

    public String getInvestigationId() {
        return investigationId;
    }

    public void setInvestigationId(String investigationId) {
        this.investigationId = investigationId;
    }

    public String getInvestigationName() {
        return investigationName;
    }

    public void setInvestigationName(String investigationName) {
        this.investigationName = investigationName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String server) {
        serverName = server;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public List<TPublication> getPublications() {
        return publications;
    }

    public void setPublications(List<TPublication> publications) {
        this.publications = publications;
    }

    public String getProposal() {
        return proposal;
    }

    public void setProposal(String proposal) {
        this.proposal = proposal;
    }

    public List<TParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<TParameter> parameters) {
        this.parameters = parameters;
    }

    public List<TInvestigator> getInvestigators() {
        return investigators;
    }

    public void setInvestigators(List<TInvestigator> investigators) {
        this.investigators = investigators;
    }

    public List<TShift> getShifts() {
        return shifts;
    }

    public void setShifts(List<TShift> shifts) {
        this.shifts = shifts;
    }

    @Override
    public int compareTo(TInvestigation inv) {
        // compare investigationName and then visitId
        int nameCmp = 0;
        try {
            Integer a = Integer.parseInt(investigationName);
            Integer b = Integer.parseInt(inv.investigationName);
            nameCmp = a.compareTo(b);
        } catch (NumberFormatException e) {
            nameCmp = investigationName.compareTo(inv.investigationName);
        }
        if (nameCmp == 0) {
            try {
                Integer a = Integer.parseInt(visitId);
                Integer b = Integer.parseInt(inv.visitId);
                return a.compareTo(b);
            } catch (NumberFormatException e) {
                return visitId.compareTo(inv.visitId);
            }
        }
        return nameCmp;
    }

}
