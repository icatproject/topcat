package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TDatafileFormat {
    private Long id;
    private String name;
    private String description;
    private String version;
    private String type;

    public TDatafileFormat(){
    }

    public TDatafileFormat(Long id, String name, String description,
            String version, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }




}
