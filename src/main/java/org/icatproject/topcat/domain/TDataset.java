package org.icatproject.topcat.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TDataset {
    private Long id;
    private String name;
    private String description;
    private String doi;
    private String location;
    private boolean complete;
    private Date startDate;
    private Date enddate;
    private TDatasetType type;

    public TDataset() {
    }

    public TDataset(Long id, String name, String description, String doi,
            String location, boolean complete, Date startDate, Date enddate,
            TDatasetType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.doi = doi;
        this.location = location;
        this.complete = complete;
        this.startDate = startDate;
        this.enddate = enddate;
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

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public TDatasetType getType() {
        return type;
    }

    public void setType(TDatasetType type) {
        this.type = type;
    }

}
