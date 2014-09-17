package org.icatproject.topcat.domain;

public enum SortOrder {
    ASC("ASC"), DESC("DESC");

    private String value;

    private SortOrder(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
