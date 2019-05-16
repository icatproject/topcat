package org.icatproject.topcat.domain;

import java.io.Serializable;
import java.lang.Boolean;
import java.lang.Long;
import java.lang.String;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.annotations.CascadeOnDelete;

/**
 * Entity implementation class for Entity: DownloadType
 *
 */

@Entity
@Table(name = "DOWNLOADTYPE")
@CascadeOnDelete
@NamedQueries({
        @NamedQuery(name = "DownloadType.findAll", query = "SELECT d FROM DownloadType d"),
        @NamedQuery(name = "DownloadType.findById", query = "SELECT d FROM DownloadType d WHERE d.id = :id"),
        @NamedQuery(name = "DownloadType.find", query = "SELECT d FROM DownloadType d WHERE d.facilityName = :facilityName AND d.downloadType = :downloadType"),
})
@XmlRootElement
public class DownloadType implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
	private Long id;
	
    @Column(name = "FACILITY_NAME", nullable = false)
	private String facilityName;

    @Column(name = "DOWNLOAD_TYPE", nullable = false)
    private String downloadType;
    
    @Column(name = "DISABLED")
	private Boolean disabled;
    
    @Column(name="MESSAGE")
	private String message;   

	public DownloadType() {
		super();
	}   
	public String getFacilityName() {
		return this.facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}   
	public String getDownloadType() {
		return this.downloadType;
	}

	public void setDownloadType(String downloadType) {
		this.downloadType = downloadType;
	}   
	public Boolean getDisabled() {
		return this.disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}   
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}   
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
   
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id:" + id);
		sb.append(" ");
        sb.append("facilityName:" + facilityName);
        sb.append(" ");
        sb.append("downloadType:" + downloadType);
        sb.append(" ");
        sb.append("disabled:" + disabled);
        sb.append(" ");
        sb.append("message:" + message);
        
		return sb.toString();
	}
}
