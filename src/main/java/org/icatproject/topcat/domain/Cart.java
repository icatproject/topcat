package org.icatproject.topcat.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.annotations.CascadeOnDelete;

@Entity
@Table(
        name = "CART",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {
                        "FACILITY_NAME",
                        "USER_NAME"
                }
        )
)
@CascadeOnDelete
@XmlRootElement
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FACILITY_NAME", nullable = false)
    private String facilityName;

    @Column(name = "USER_NAME", nullable = false)
    private String userName;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "cart", orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<CartItem>();

    @Column(name = "CREATED_AT", nullable=false, updatable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "UPDATED_AT", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;


    public Cart() {

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

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    private void updateAt() {
        this.updatedAt = new Date();
    }

    @PrePersist
    private void createAt() {
        this.createdAt = this.updatedAt = new Date();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + id);
        sb.append(" ");
        sb.append("facilityName:" + facilityName);
        sb.append(" ");
        sb.append("userName:" + userName);
        sb.append(" ");
        sb.append("CartItems:" + this.getCartItems().size());

        return sb.toString();
    }

}
