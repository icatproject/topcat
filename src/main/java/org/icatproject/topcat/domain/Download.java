package org.icatproject.topcat.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(name = "DOWNLOAD")
@CascadeOnDelete
@NamedQueries({
        @NamedQuery(name = "Download.findAll", query = "SELECT d FROM Download d where d.isDeleted = false"),
        @NamedQuery(name = "Download.findById", query = "SELECT d FROM Download d WHERE d.id = :id AND d.isDeleted = false "),
        @NamedQuery(name = "Download.findByPreparedId", query = "SELECT d FROM Download d WHERE d.preparedId = :preparedId AND d.isDeleted = false"),
        @NamedQuery(name = "Download.deleteById", query = "DELETE FROM Download d WHERE d.id = :id AND d.isDeleted = false")
})
@XmlRootElement
public class Download implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FACILITY_NAME", nullable = false)
    private String facilityName;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "TRANSPORT", nullable = false)
    private String transport;

    @Column(name = "TRANSPORT_URL", nullable = false)
    private String transportUrl = "";

    @Column(name = "ICAT_URL", nullable = false)
    private String icatUrl = "";

    @Column(name = "FILE_NAME", nullable = false)
    private String fileName;

    @Column(name = "PREPARED_ID")
    private String preparedId;

    @Column(name = "SESSION_ID", nullable = false)
    private String sessionId;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "IS_EMAIL_SENT")
    private Boolean isEmailSent = false;

    @Column(name = "STATUS")
    @Enumerated(EnumType.STRING)
    private DownloadStatus status;

    @Column(name = "THE_SIZE")
    private long size;

    @Column(name = "IS_TWO_LEVEL")
    private Boolean isTwoLevel;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "download", orphanRemoval = true)
    private List<DownloadItem> downloadItems;

    @Column(name = "CREATED_AT", updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "IS_DELETED")
    private Boolean isDeleted = false;

    @Column(name = "DELETED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedAt;

    @Column(name = "COMPLETED_AT")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;

    public Download() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getTransportUrl() {
        return transportUrl;
    }

    public void setTransportUrl(String transportUrl) {
        this.transportUrl = transportUrl;
    }

    public String getIcatUrl() {
        return icatUrl;
    }

    public void setIcatUrl(String icatUrl) {
        this.icatUrl = icatUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPreparedId() {
        return preparedId;
    }

    public void setPreparedId(String preparedId) {
        this.preparedId = preparedId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsEmailSent() {
        return isEmailSent;
    }

    public void setIsEmailSent(Boolean isEmailSent) {
        this.isEmailSent = isEmailSent;
    }

    public DownloadStatus getStatus() {
        return status;
    }

    public void setStatus(DownloadStatus status) {
        this.status = status;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Boolean getIsTwoLevel() {
        return isTwoLevel;
    }

    public void setIsTwoLevel(Boolean isTwoLevel) {
        this.isTwoLevel = isTwoLevel;
    }

    public List<DownloadItem> getDownloadItems() {
        return downloadItems;
    }

    public void setDownloadItems(List<DownloadItem> downloadItems) {
        this.downloadItems = downloadItems;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    private void createAt() {
        this.createdAt = new Date();
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id);
        sb.append(" ");
        sb.append("facilityName:" + facilityName);
        sb.append(" ");
        sb.append("userName:" + userName);
        sb.append(" ");
        sb.append("transport:" + transport);
        sb.append(" ");
        sb.append("transportUrl:" + transportUrl);
        sb.append(" ");
        sb.append("icatUrl:" + icatUrl);
        sb.append(" ");
        sb.append("fileName:" + fileName);
        sb.append(" ");
        sb.append("preparedId:" + preparedId);
        sb.append(" ");
        sb.append("email:" + email);
        sb.append(" ");
        sb.append("status:" + status);
        sb.append(" ");
        sb.append("isTwoLevel:" + isTwoLevel);
        sb.append(" ");
        sb.append("DownloadItems:" + this.getDownloadItems().size());

        return sb.toString();
    }

        public List<Long> getInvestigationIds(){
        List<Long> out = new ArrayList<Long>();
        for (DownloadItem downloadItem : getDownloadItems()) {
            if (downloadItem.getEntityType() == EntityType.investigation) {
                out.add(downloadItem.getEntityId());
            }
        }
        return out;
    }

    public List<Long> getDatasetIds(){
        List<Long> out = new ArrayList<Long>();
        for (DownloadItem downloadItem : getDownloadItems()) {
            if (downloadItem.getEntityType() == EntityType.dataset) {
                out.add(downloadItem.getEntityId());
            }
        }
        return out;
    }

    public List<Long> getDatafileIds(){
        List<Long> out = new ArrayList<Long>();
        for (DownloadItem downloadItem : getDownloadItems()) {
            if (downloadItem.getEntityType() == EntityType.datafile) {
                out.add(downloadItem.getEntityId());
            }
        }
        return out;
    }

    /*@Override
    public boolean equals(Object object) {
        if (!(object instanceof Order)) {
            return false;
        }
        Download other = (Download) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }

        return true;
    }*/
}
