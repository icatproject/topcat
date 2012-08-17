package uk.ac.stfc.topcat.core.gwt.module;

public enum TopcatExceptionType {

    BAD_PARAMETER, INTERNAL, INSUFFICIENT_PRIVILEGES, NO_SUCH_OBJECT_FOUND, OBJECT_ALREADY_EXISTS, SESSION, VALIDATION;

    public String value() {
        return name();
    }

    public static TopcatExceptionType fromValue(String v) {
        return valueOf(v);
    }
}
