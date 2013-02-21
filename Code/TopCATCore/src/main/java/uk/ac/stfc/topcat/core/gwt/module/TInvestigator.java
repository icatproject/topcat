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

/**
 * This is shared with GWT for investigation information. This Class holds
 * details about an investigator.
 */
public class TInvestigator implements Serializable, Comparable<TInvestigator> {
    private String facilityUserId;
    private String federalId;
    private String fullName;
    private String role;

    public TInvestigator() {
    }

    public TInvestigator(String facilityUserId, String federalId, String fullName, String role) {
        this.facilityUserId = facilityUserId;
        this.federalId = federalId;
        this.fullName = fullName;
        this.role = role;
    }

    /**
     * @return the facilityUserId
     */
    public String getFacilityUserId() {
        return facilityUserId;
    }

    /**
     * @return the federalId
     */
    public String getFederalId() {
        return federalId;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @return the role
     */
    public String getRole() {
        return role;
    }

    public int compareTo(TInvestigator inv) {
        // compare role and then full name
        int nameCmp = 0;
        try {
            Integer a = Integer.parseInt(role);
            Integer b = Integer.parseInt(inv.getRole());
            nameCmp = a.compareTo(b);
        } catch (NumberFormatException e) {
            // If alpha do reverse compare as we want principal experimenter
            // ahead of experimenter
            if (role != null && inv.getRole() != null) {
                nameCmp = inv.getRole().compareTo(role);
            }
        }
        if (nameCmp == 0) {
            try {
                Integer a = Integer.parseInt(fullName);
                Integer b = Integer.parseInt(inv.fullName);
                return a.compareTo(b);
            } catch (NumberFormatException e) {
                return fullName.compareTo(inv.fullName);
            }
        }
        return nameCmp;
    }
}
