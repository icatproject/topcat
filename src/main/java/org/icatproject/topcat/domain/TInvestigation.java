package org.icatproject.topcat.domain;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name="investigation")
public class TInvestigation {
    private Long id;
    private String name;
    private String title;
    private String visitId;
    private String summary;
    private String doi;
    private Date startDate;
    private Date endDate;
    private Date releaseDate;

    public TInvestigation() {
    }

    public TInvestigation(Long id, String name, String title, String visitId,
            String summary, String doi, Date startDate, Date endDate,
            Date releaseDate) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.visitId = visitId;
        this.summary = summary;
        this.doi = doi;
        this.startDate = startDate;
        this.endDate = endDate;
        this.releaseDate = releaseDate;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }



}
