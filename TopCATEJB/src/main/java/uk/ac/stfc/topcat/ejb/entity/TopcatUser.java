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
package uk.ac.stfc.topcat.ejb.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is User entity.
 * <p>
 *
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@Entity
@Table(name = "TOPCAT_USER")
@NamedQueries({
        @NamedQuery(name = "TopcatUser.findById", query = "SELECT t FROM TopcatUser t WHERE t.id = :id"),
        @NamedQuery(name = "TopcatUser.findByName", query = "SELECT t FROM TopcatUser t WHERE t.name = :name"),
        @NamedQuery(name = "TopcatUser.findByDn", query = "SELECT t FROM TopcatUser t WHERE t.dn = :dn"),
        //@NamedQuery(name = "TopcatUser.findByNameAndServerNotAnonymous", query = "SELECT t FROM TopcatUser t WHERE t.userSurname= :userSurname AND t.serverId.name=:serverName AND t.topcatUserId.displayName <> 'anon'"),
        @NamedQuery(name = "TopcatUser.findByNameAndServer", query = "SELECT t FROM TopcatUser t WHERE t.userSurname= :userSurname AND t.serverId.name=:serverName"),
        @NamedQuery(name = "TopcatUser.findAnonymousUser", query = "SELECT t FROM TopcatUser t WHERE t.name = 'anon'"),
        @NamedQuery(name = "TopcatUser.findByServerId", query = "SELECT t FROM TopcatUser t WHERE t.serverId.id = :serverId"),
})

@XmlRootElement
public class TopcatUser implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;
    @Column(name = "NAME")
    private String name;
    @Column(name = "USER_SURNAME")
    private String userSurname;
    @Column(name = "DN")
    private String dn;
    @JoinColumn(name = "SERVER_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TopcatIcatServer serverId;
    @JoinColumn(name = "TOPCAT_USER_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TopcatUserInfo topcatUserId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId")
    private List<TopcatUserSession> topcatUserSessionList;

    public TopcatUser() {
    }

    public TopcatUser(String name, String dn, TopcatIcatServer serverId, TopcatUserInfo topcatUserId,
            List<TopcatUserSession> topcatUserSessionList) {
        this.name = name;
        this.dn = dn;
        this.serverId = serverId;
        this.topcatUserId = topcatUserId;
        this.topcatUserSessionList = topcatUserSessionList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserSurname() {
        return userSurname;
    }

    public void setUserSurname(String userSurname) {
        this.userSurname = userSurname;
    }

    public String getDn() {
        return dn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public TopcatIcatServer getServerId() {
        return serverId;
    }

    public void setServerId(TopcatIcatServer serverId) {
        this.serverId = serverId;
    }

    public TopcatUserInfo getTopcatUserId() {
        return topcatUserId;
    }

    public void setTopcatUserId(TopcatUserInfo topcatUserId) {
        this.topcatUserId = topcatUserId;
    }

    public List<TopcatUserSession> getTopcatUserSessionList() {
        return topcatUserSessionList;
    }

    public void setTopcatUserSessionList(List<TopcatUserSession> topcatUserSessionList) {
        this.topcatUserSessionList = topcatUserSessionList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are
        // not set
        if (!(object instanceof TopcatUser)) {
            return false;
        }
        TopcatUser other = (TopcatUser) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatUser[id=" + id + "]";
    }

}
