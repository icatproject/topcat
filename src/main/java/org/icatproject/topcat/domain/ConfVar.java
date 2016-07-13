package org.icatproject.topcat.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "CONFVAR")
@XmlRootElement
public class ConfVar implements Serializable {

	@Id
	@Column(name = "NAME")
    private String name;

    @Column(name = "VALUE", columnDefinition = "text")
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
