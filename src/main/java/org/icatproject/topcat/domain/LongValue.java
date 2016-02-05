package org.icatproject.topcat.domain;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LongValue {
    private Long value;

    public LongValue() {
    }

    public LongValue(Long value) {
        this.setValue(value);
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
