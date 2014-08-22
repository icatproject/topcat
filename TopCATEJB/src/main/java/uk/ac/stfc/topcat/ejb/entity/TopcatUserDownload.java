package uk.ac.stfc.topcat.ejb.entity;

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.Date;

/**
 * The persistent class for the TOPCAT_USER_DOWNLOAD database table.
 *
 */
@Entity
@Table(name = "TOPCAT_USER_DOWNLOAD")
@NamedQueries({
        @NamedQuery(name = "TopcatUserDownload.findByUserId", query = "SELECT t FROM TopcatUserDownload t WHERE t.userId = :userId"),
        @NamedQuery(name = "TopcatUserDownload.updateStatus", query = "UPDATE TopcatUserDownload t SET t.url = :updatedUrl, t.status = :status WHERE t.url = :url"),
        @NamedQuery(name = "TopcatUserDownload.updateExpiryTimeById", query = "UPDATE TopcatUserDownload t SET t.expiryTime = :expiryTime WHERE t.id = :id"),
        @NamedQuery(name = "TopcatUserDownload.updateById", query = "UPDATE TopcatUserDownload t SET t.url = :url, t.status = :status WHERE t.id = :id"),
        @NamedQuery(name = "TopcatUserDownload.updateWithMessageById", query = "UPDATE TopcatUserDownload t SET t.url = :url, t.status = :status, t.message = :message WHERE t.id = :id"),
        @NamedQuery(name = "TopcatUserDownload.deleteById", query = "DELETE from TopcatUserDownload t where t.id = :id"),
        @NamedQuery(name = "TopcatUserDownload.deleteByIdandUserId", query = "DELETE from TopcatUserDownload t where t.id = :id and t.userId = :userId"),
        @NamedQuery(name = "TopcatUserDownload.cleanup", query = "DELETE from TopcatUserDownload t where CURRENT_TIMESTAMP > t.expiryTime"),
        @NamedQuery(name = "TopcatUserDownload.deleteByUserId", query = "DELETE from TopcatUserDownload t where t.userId.id = :userId"),

})

@XmlRootElement
public class TopcatUserDownload implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EXPIRY_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryTime;

    @Column(name = "NAME")
    private String name;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "SUBMIT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submitTime;

    @Column(name = "URL")
    private String url;

    @Column(name = "PREPARED_ID")
    private String preparedId;

    @Column(name = "MESSAGE")
    private String message;

    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    @ManyToOne(optional = true)
    private TopcatUser userId;

    public TopcatUserDownload() {
    }

    public Date getExpiryTime() {
        return this.expiryTime;
    }

    public void setExpiryTime(Date expiryTime) {
        this.expiryTime = expiryTime;
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

    public String getPreparedId() {
        return preparedId;
    }

    public void setPreparedId(String preparedId) {
        this.preparedId = preparedId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TopcatUser getUserId() {
        return userId;
    }

    public void setUserId(TopcatUser userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
        if (!(object instanceof TopcatUserDownload)) {
            return false;
        }
        TopcatUserDownload other = (TopcatUserDownload) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatUserDownload[id=" + id + "]";
    }
}