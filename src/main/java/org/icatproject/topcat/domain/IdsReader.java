package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class IdsReader {

    private String facilityName;
    private String authenticatorType;
    private String userNameKey;
    private String userName;
    private String passwordKey;
    private String password;

    public String getFacilityName() {
        return facilityName;
    }
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
    public String getAuthenticatorType() {
        return authenticatorType;
    }
    public void setAuthenticatorType(String authenticatorType) {
        this.authenticatorType = authenticatorType;
    }
    public String getUserNameKey() {
        return userNameKey;
    }
    public void setUserNameKey(String userNameKey) {
        this.userNameKey = userNameKey;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPasswordKey() {
        return passwordKey;
    }
    public void setPasswordKey(String passwordKey) {
        this.passwordKey = passwordKey;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }



}
