package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CartSubmitDTO {
    private String facilityName;
    private String userName;
    private String sessionId;
    private String icatUrl;
    private String fileName;
    private Status status;
    private String transport;
    private String transportUrl;
    private String email;
    private String zipType;


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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIcatUrl() {
        return icatUrl;
    }

    public void setIcatUrl(String icatUrl) {
        this.icatUrl = icatUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public String getTransportUrl() {
        return transportUrl;
    }

    public void setTransportUrl(String transportUrl) {
        this.transportUrl = transportUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZipType() {
        return zipType;
    }

    public void setZipType(String zipType) {
        this.zipType = zipType;
    }


}
