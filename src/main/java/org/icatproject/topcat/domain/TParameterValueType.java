package org.icatproject.topcat.domain;

public enum TParameterValueType {
    DATE_AND_TIME,
    NUMERIC,
    STRING;

    public String value() {
        return name();
    }

    public static TParameterValueType fromValue(String v) {
        return valueOf(v);
    }
}