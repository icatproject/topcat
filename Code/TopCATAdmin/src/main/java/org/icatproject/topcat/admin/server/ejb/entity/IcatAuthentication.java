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

package org.icatproject.topcat.admin.server.ejb.entity;

import java.io.Serializable;

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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is the ICAT Authentication entity.
 * <p>
 * 
 */
@Entity
@Table(name = "ICAT_AUTHENTICATION")
@NamedQueries({
        @NamedQuery(name = "IcatAuthentication.findAll", query = "SELECT t FROM IcatAuthentication t"),
        @NamedQuery(name = "IcatAuthentication.findById", query = "SELECT t FROM IcatAuthentication t WHERE t.id = :id"),
        @NamedQuery(name = "IcatAuthentication.findByServerName", query = "SELECT t FROM IcatAuthentication t WHERE t.serverId.name=:serverName") })
@XmlRootElement
public class IcatAuthentication implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;

    @Basic(optional = false)
    @Column(name = "AUTHENTICATION_TYPE")
    private String authenticationType;

    @Basic(optional = false)
    @Column(name = "PLUGIN_NAME")
    private String pluginName;

    @Basic(optional = true)
    @Column(name = "AUTHENTICATION_SERVICE_URL")
    private String authenticationServiceUrl;

    @JoinColumn(name = "SERVER_ID", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private TopcatIcatServer serverId;

    public IcatAuthentication() {
    }

    public IcatAuthentication(Long id) {
        this.id = id;
    }

    public IcatAuthentication(TopcatIcatServer serverId, String authenticationType, String pluginName,
            String authenticationServiceUrl) {
        this.serverId = serverId;
        this.authenticationType = authenticationType;
        this.pluginName = pluginName;
        this.authenticationServiceUrl = authenticationServiceUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getAuthenticationServiceUrl() {
        return authenticationServiceUrl;
    }

    public void setAuthenticationServiceUrl(String authenticationServiceUrl) {
        this.authenticationServiceUrl = authenticationServiceUrl;
    }

    public TopcatIcatServer getServerId() {
        return serverId;
    }

    public void setServerId(TopcatIcatServer serverId) {
        this.serverId = serverId;
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
        if (!(object instanceof IcatAuthentication)) {
            return false;
        }
        IcatAuthentication other = (IcatAuthentication) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.IcatAuthentication[id=" + id + "]";
    }

}
