package uk.ac.stfc.topcat.ejb.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * This is the TOPCAT_MESSAGES entity.
 * <p>
 * 
 */
@Entity
@Table(name = "TOPCAT_MESSAGES")
@NamedQueries({
        @NamedQuery(name = "TopcatMessages.findAll", query = "SELECT t FROM TopcatMessages t ORDER BY t.startTime DESC"),
        @NamedQuery(name = "TopcatMessages.findById", query = "SELECT t FROM TopcatMessages t WHERE t.id = :id"),
        @NamedQuery(name = "TopcatMessages.findActiveMessage", query = "SELECT t FROM TopcatMessages t WHERE CURRENT_TIMESTAMP BETWEEN t.startTime AND t.stopTime"),
        @NamedQuery(name = "TopcatMessages.findMessagesByDateRange", query = "SELECT t FROM TopcatMessages t WHERE (t.startTime <= :toDateTime) and (t.stopTime >= :fromDateTime)"),
})
@XmlRootElement
public class TopcatMessages implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Basic(optional = false)
	@Column(name = "ID")
	private Long id;
	
	@Basic(optional = false)
	@Column(name = "MESSAGE")
	private String message;
	
	@Basic(optional = false)
	@Column(name = "START_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
	
	@Basic(optional = false)
    @Column(name = "STOP_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date stopTime;
    
	public TopcatMessages(){
	}
	

    public TopcatMessages(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
    
    public Date getStartTime() {
  		return startTime;
  	}
    
    public void setStopTime(Date startTime) {
		this.stopTime = startTime;
	}
    
    public Date getStopTime() {
  		return stopTime;
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
        if (!(object instanceof TopcatMessages)) {
            return false;
        }
        TopcatMessages other = (TopcatMessages) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "uk.ac.stfc.topcat.ejb.entity.TopcatMessages[id=" + id + "]";
    }

}
