package org.icatproject.topcat.domain;

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
