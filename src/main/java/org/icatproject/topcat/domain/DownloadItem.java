package org.icatproject.topcat.domain;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "DownloadItem")
@NamedQueries({
        @NamedQuery(name = "DownloadItem.findById", query = "SELECT i FROM DownloadItem i WHERE i.id = :id"),
        @NamedQuery(name = "DownloadItem.findByDownloadId", query = "SELECT i FROM DownloadItem i WHERE i.download.id = :id"),
        @NamedQuery(name = "DownloadItem.deleteById", query = "DELETE FROM DownloadItem i WHERE i.id = :id"),
        @NamedQuery(name = "DownloadItem.deleteByDownloadId", query = "DELETE FROM DownloadItem i WHERE i.download.id = :id")
})
@XmlRootElement
public class DownloadItem implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ENTITY_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name= "DOWNLOAD_ID")
    private Download download;

    public DownloadItem() {
    }

    public DownloadItem(EntityType entityType, Long entityId) {
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    @XmlTransient
    public Download getDownload() {
        return download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

}
