package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BooleanValue {
    private Boolean value;

    public BooleanValue() {
    }

    public BooleanValue(Boolean value) {
        this.value = value;
    }

    public Boolean getState() {
        return value;
    }

    public void setState(Boolean value) {
        this.value = value;
    }

}
