package com.cindi.domain.enums;

public enum InputDataTypes {
    Int("int"),
    String("string"),
    Bool("bool"),
    Object("object"),
    ErrorMessage("errorMessage"),
    Decimal("decimal"),
    DateTime("dateTime"),
    Secret("secret");

    private String dataType;

    InputDataTypes(String value) {
        this.dataType = value;
    }

    @Override
    public java.lang.String toString() {
        return dataType;
    }
}
