package org.icatproject.topcat.domain;

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
 * This is UserInfo entity. Abstraction above User, each UserInfo can have one
 * or more User objects.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
@Entity
@Table(name = "TOPCAT_USER_INFO")
@NamedQueries({
        @NamedQuery(name = "TopcatUserInfo.findAll", query = "SELECT t FROM TopcatUserInfo t"),
        @NamedQuery(name = "TopcatUserInfo.findById", query = "SELECT t FROM TopcatUserInfo t WHERE t.id = :id"),
        @NamedQuery(name = "TopcatUserInfo.findByDisplayName", query = "SELECT t FROM TopcatUserInfo t WHERE t.displayName = :displayName"),
        @NamedQuery(name = "TopcatUserInfo.findAnonymousUser", query = "SELECT t FROM TopcatUserInfo t WHERE t.displayName = 'Anonymous'"),
        @NamedQuery(name = "TopcatUserInfo.deleteByServerId", query = "DELETE FROM TopcatUserInfo t WHERE t.homeServer.id = :serverId"),
})
@XmlRootElement
public class TopcatUserInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
        // TODO: Warning - this method won't work in the case the id fields are
        // not set
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
