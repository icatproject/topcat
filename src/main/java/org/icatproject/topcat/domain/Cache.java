package org.icatproject.topcat.domain;


import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.*;
import java.util.*;

@Entity
@Table(name = "CACHE")
@XmlRootElement
public class Cache implements Serializable {

	@Id
	@Column(name = "KEY")
    private String key;

    @Lob
    @Column(name = "VALUE")
    private byte[] value;

    @Column(name = "LAST_ACCESS_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAccessTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getValue(){
        return value;
    }

    public void setValue(byte[] value){
        this.value = value;
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }
}
