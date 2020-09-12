package com.cindi.domain.enums;

public enum OperatorStatements {

    AND("AND"),
    OR("OR");

    private String statement;

    OperatorStatements(String value) {
        this.statement = value;
    }

    @Override
    public java.lang.String toString() {
        return statement;
    }

}
