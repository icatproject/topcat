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
package uk.ac.stfc.topcat.ejb.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is User session entity.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@Entity
@Table(name = "TOPCAT_USER_SESSION")
@NamedQueries({
    @NamedQuery(name = "TopcatUserSession.findAll", query = "SELECT t FROM TopcatUserSession t"),
    @NamedQuery(name = "TopcatUserSession.findById", query = "SELECT t FROM TopcatUserSession t WHERE t.id = :id"),
    @NamedQuery(name = "TopcatUserSession.findByTopcatSessionId", query = "SELECT t FROM TopcatUserSession t WHERE t.topcatSessionId = :topcatSessionId"),
    @NamedQuery(name = "TopcatUserSession.findByIcatSessionId", query = "SELECT t FROM TopcatUserSession t WHERE t.icatSessionId = :icatSessionId"),
    @NamedQuery(name = "TopcatUserSession.findByExpiryDate", query = "SELECT t FROM TopcatUserSession t WHERE t.expiryDate = :expiryDate"),
    @NamedQuery(name = "TopcatUserSession.findByTopcatSessionIdAndAnonymous", query = "SELECT t FROM TopcatUserSession t WHERE t.topcatSessionId = :topcatSessionId OR (t.userId.topcatUserId.displayName = 'Anonymous' AND t.userId.serverId.id NOT IN (SELECT tus.userId.serverId.id FROM TopcatUserSession tus WHERE tus.topcatSessionId = :topcatSessionId ))"),
    @NamedQuery(name = "TopcatUserSession.findByTopcatSessionIdAndServerName",query = "SELECT t FROM TopcatUserSession t WHERE t.topcatSessionId = :topcatSessionId AND t.userId.serverId.name = :serverName"),
    @NamedQuery(name = "TopcatUserSession.findByAnonymousAndServerName",query = "SELECT t FROM TopcatUserSession t WHERE t.topcatSessionId IS NULL AND t.userId.serverId.name = :serverName"),
    @NamedQuery(name = "TopcatUserSession.findByAnonymous",query = "SELECT t FROM TopcatUserSession t WHERE t.topcatSessionId IS NULL"),
    @NamedQuery(name = "TopcatUserSession.deleteSessionByUserId",query = "DELETE FROM TopcatUserSession t WHERE t.userId.id = :userId")
})
@XmlRootElement

public class TopcatUserSession implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "TOPCAT_SESSION_ID")
    private String topcatSessionId;
    @Column(name = "ICAT_SESSION_ID")
    private String icatSessionId;
    @Column(name = "EXPIRY_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TopcatUser userId;

    public TopcatUserSession() {
    }

    public Long getId() {
        return id;
    }

    public String getTopcatSessionId() {
        return topcatSessionId;
    }

    public void setTopcatSessionId(String topcatSessionId) {
        this.topcatSessionId = topcatSessionId;
    }

    public String getIcatSessionId() {
        return icatSessionId;
    }

    public void setIcatSessionId(String icatSessionId) {
        this.icatSessionId = icatSessionId;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public TopcatUser getUserId() {
        return userId;
    }

    public void setUserId(TopcatUser userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TopcatUserSession)) {
            return false;
        }
        TopcatUserSession other = (TopcatUserSession) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatUserSession[id=" + id + "]";
    }

}
