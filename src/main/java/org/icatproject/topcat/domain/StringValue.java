package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StringValue {
    private String value;

    public StringValue() {
    }

    public StringValue(String value) {
        this.setValue(value);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
