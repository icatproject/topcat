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
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is ICAT Server (Facility) entity.
 * <p>
 * @author Mr. Srikanth Nagella
 * @version 1.0,  &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@Entity
@Table(name = "TOPCAT_ICAT_SERVER")
@NamedQueries({@NamedQuery(name = "TopcatIcatServer.findAll", query = "SELECT t FROM TopcatIcatServer t"), 
               @NamedQuery(name = "TopcatIcatServer.findById", query = "SELECT t FROM TopcatIcatServer t WHERE t.id = :id"),
               @NamedQuery(name = "TopcatIcatServer.findByName", query = "SELECT t FROM TopcatIcatServer t WHERE t.name = :name"),
               @NamedQuery(name = "TopcatIcatServer.findByServerUrl", query = "SELECT t FROM TopcatIcatServer t WHERE t.serverUrl = :serverUrl"),
               @NamedQuery(name = "TopcatIcatServer.findByDefaultUser", query = "SELECT t FROM TopcatIcatServer t WHERE t.defaultUser = :defaultUser"),
               @NamedQuery(name = "TopcatIcatServer.findByDefaultPassword", query = "SELECT t FROM TopcatIcatServer t WHERE t.defaultPassword = :defaultPassword")
})
@XmlRootElement
public class TopcatIcatServer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;
    @Basic(optional = false)
    @Column(name = "NAME")
    private String name;
    @Basic(optional = false)
    @Column(name = "SERVER_URL")
    private String serverUrl;
    @Column(name = "DEFAULT_USER")
    private String defaultUser;
    @Column(name = "DEFAULT_PASSWORD")
    private String defaultPassword;
    @Column(name = "PLUGIN_NAME")
    private String pluginName;
    @Column(name = "VERSION")
    private String version;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "serverId")
    private List<TopcatUser> topcatUserList;

    public TopcatIcatServer() {
    }

    public TopcatIcatServer(Long id) {
        this.id = id;
    }

    public TopcatIcatServer(Long id, String serverUrl) {
        this.id = id;
        this.serverUrl = serverUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultUser() {
        return defaultUser;
    }

    public void setDefaultUser(String defaultUser) {
        this.defaultUser = defaultUser;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public List<TopcatUser> getTopcatUserList() {
        return topcatUserList;
    }

    public void setTopcatUserList(List<TopcatUser> topcatUserList) {
        this.topcatUserList = topcatUserList;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
        if (!(object instanceof TopcatIcatServer)) {
            return false;
        }
        TopcatIcatServer other = (TopcatIcatServer) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatIcatServer[id=" + id + "]";
    }

}
