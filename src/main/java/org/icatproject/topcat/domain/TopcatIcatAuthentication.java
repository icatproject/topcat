package org.icatproject.topcat.domain;

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
@Table(name = "TOPCAT_ICAT_AUTHENTICATION")
@NamedQueries({
        @NamedQuery(name = "TopcatIcatAuthentication.findAll", query = "SELECT t FROM TopcatIcatAuthentication t"),
        @NamedQuery(name = "TopcatIcatAuthentication.findById", query = "SELECT t FROM TopcatIcatAuthentication t WHERE t.id = :id"),
        @NamedQuery(name = "TopcatIcatAuthentication.findByServerName", query = "SELECT t FROM TopcatIcatAuthentication t WHERE t.serverId.name=:serverName"),
        @NamedQuery(name = "TopcatIcatAuthentication.deleteByServerId", query = "DELETE FROM TopcatIcatAuthentication t WHERE t.serverId.id = :serverId"),
})
@XmlRootElement
public class TopcatIcatAuthentication implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "ID")
    private Long id;

    @Basic(optional = false)
    @Column(name = "DISPLAY_NAME")
    private String displayName;
    
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

    public TopcatIcatAuthentication() {
    }

    public TopcatIcatAuthentication(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
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
        if (!(object instanceof TopcatIcatAuthentication)) {
            return false;
        }
        TopcatIcatAuthentication other = (TopcatIcatAuthentication) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatIcatAuthentication[id=" + id + "]";
    }

}
