package org.icatproject.topcat.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@Table(name = "CACHEDGETSIZE")
@XmlRootElement
public class CachedGetSize implements Serializable {

	@Id
	@Column(name = "FACILITY_NAME")
    private String facilityName;

    @Id
    @Column(name = "ENTITY_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @Id
    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @Column(name = "VALUE", nullable = false)
    private Long value;

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
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

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
