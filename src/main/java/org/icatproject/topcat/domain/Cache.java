package org.icatproject.topcat.domain;

import java.io.*;
import java.util.*;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "CACHE")
@XmlRootElement
public class Cache implements Serializable {

	@Id
	@Column(name = "KEY_")
    private String key;

    @Lob
    @Column(name = "SERIALIZED_VALUE")
    private byte[] serializedValue;

    @Column(name = "LAST_ACCESS_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastAccessTime;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue(){
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(this.serializedValue));
            Object object  = objectInputStream.readObject();
            objectInputStream.close();
            return object;
        } catch(Exception e){
            return null;
        }
    }

    public void setValue(Serializable object){
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.close();
            this.serializedValue = byteArrayOutputStream.toByteArray();
        } catch(Exception e){}
    }

    public Date getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(Date lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    @PrePersist
    private void init() {
        this.lastAccessTime = new Date();
    }
}
