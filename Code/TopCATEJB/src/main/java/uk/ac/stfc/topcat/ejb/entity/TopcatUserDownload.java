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
        @NamedQuery(name = "TopcatUserDownload.findAll", query = "SELECT t FROM TopcatUserDownload t"),
        @NamedQuery(name = "TopcatUserDownload.findById", query = "SELECT t FROM TopcatUserDownload t WHERE t.id = :id"),
        @NamedQuery(name = "TopcatUserDownload.findByUserId", query = "SELECT t FROM TopcatUserDownload t WHERE t.userId = :userId") })
@XmlRootElement
public class TopcatUserDownload implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "SUBMIT_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date submitTime;

    @Column(name = "URL")
    private String url;

    @JoinColumn(name = "USER_ID", referencedColumnName = "ID")
    @ManyToOne(optional = true)
    private TopcatUser userId;

    @Column(name = "VALID_PERIOD")
    private long validPeriod;

    public TopcatUserDownload() {
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

    public Long getValidPeriod() {
        return this.validPeriod;
    }

    public void setValidPeriod(Long validPeriod) {
        this.validPeriod = validPeriod;
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