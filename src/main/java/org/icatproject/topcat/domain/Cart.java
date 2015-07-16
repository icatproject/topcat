package org.icatproject.topcat.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.Table;
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
@NamedQueries({
        @NamedQuery(name = "Cart.findAll", query = "SELECT c FROM Cart c"),
        @NamedQuery(name = "Cart.findById", query = "SELECT c FROM Cart c WHERE c.id = :id"),
        @NamedQuery(name = "Cart.findByFacilityNameAndUserName", query = "SELECT c FROM Cart c WHERE c.facilityName = :facilityName AND c.userName = :userName"),
        @NamedQuery(name = "Cart.deleteByFacilitNameAndUserName", query = "DELETE FROM Cart c WHERE c.facilityName = :facilityName AND c.userName = :userName")

})
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

    @Column(name = "SIZE")
    private Long size ;

    @Column(name = "AVAILABILITY")
    @Enumerated(EnumType.STRING)
    private Availability availability;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cart", orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<CartItem>();


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

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

}
