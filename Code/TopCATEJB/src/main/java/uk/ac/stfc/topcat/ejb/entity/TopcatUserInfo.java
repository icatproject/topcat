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
import java.math.BigDecimal;
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

/**
 * This is UserInfo entity. Abstraction above User, each UserInfo can have one or
 * more User objects.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@Entity
@Table(name = "TOPCAT_USER_INFO")
@NamedQueries({@NamedQuery(name = "TopcatUserInfo.findAll", query = "SELECT t FROM TopcatUserInfo t"),
               @NamedQuery(name = "TopcatUserInfo.findById", query = "SELECT t FROM TopcatUserInfo t WHERE t.id = :id"),
               @NamedQuery(name = "TopcatUserInfo.findByDisplayName", query = "SELECT t FROM TopcatUserInfo t WHERE t.displayName = :displayName"),
               @NamedQuery(name = "TopcatUserInfo.findAnonymousUser", query = "SELECT t FROM TopcatUserInfo t WHERE t.displayName = 'Anonymous'")
})
public class TopcatUserInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;
    @Column(name = "DISPLAY_NAME")
    private String displayName;
    @JoinColumn(name = "HOME_SERVER", referencedColumnName = "ID")
    @ManyToOne
    private TopcatIcatServer homeServer;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "topcatUserId")
    private List<TopcatUser> topcatUserList;

    public TopcatUserInfo() {
    }

    public TopcatUserInfo(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public TopcatIcatServer getHomeServer() {
        return homeServer;
    }

    public void setHomeServer(TopcatIcatServer homeServer) {
        this.homeServer = homeServer;
    }

    public List<TopcatUser> getTopcatUserList() {
        return topcatUserList;
    }

    public void setTopcatUserList(List<TopcatUser> topcatUserList) {
        this.topcatUserList = topcatUserList;
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
        if (!(object instanceof TopcatUserInfo)) {
            return false;
        }
        TopcatUserInfo other = (TopcatUserInfo) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatUserInfo[id=" + id + "]";
    }

}
