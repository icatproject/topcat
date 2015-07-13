package org.icatproject.topcat.domain;

import java.io.Serializable;
import java.util.List;

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

@Entity
@Table(name = "Download")
@NamedQueries({
        @NamedQuery(name = "Download.findAll", query = "SELECT d FROM Download d"),
        @NamedQuery(name = "Download.findById", query = "SELECT d FROM Download d WHERE d.id = :id"),
        @NamedQuery(name = "Download.findByPreparedId", query = "SELECT d FROM Download d WHERE d.preparedId = :preparedId"),
        @NamedQuery(name = "Download.findByFacilityName", query = "SELECT d FROM Download d WHERE d.facilityName = :facilityName"),
        @NamedQuery(name = "Download.findByFacilityNameAndStatus", query = "SELECT d FROM Download d WHERE d.facilityName = :facilityName AND d.status = :status"),
        @NamedQuery(name = "Download.deleteById", query = "DELETE FROM Download d WHERE d.id = :id")

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

    @Column(name = "TRANSPORT", nullable = false)
    private String transport;

    @Column(name = "FILE_NAME", nullable = false)
    private String fileName;

    @Column(name = "PREPARED_ID", nullable = false)
    private String preparedId;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "STATUS")
    private String status;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "download")
    private List<DownloadItem> downloadItems;


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

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<DownloadItem> getDownloadItems() {
        return downloadItems;
    }

    public void setDownloadItems(List<DownloadItem> downloadItems) {
        this.downloadItems = downloadItems;
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
