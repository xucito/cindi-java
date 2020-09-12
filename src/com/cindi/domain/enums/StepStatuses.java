package com.cindi.domain.enums;

public enum StepStatuses {

    Suspended("suspended"),
    Unassigned("unassigned"),
    Assigned("assigned"),
    Successful("successful"),
    Warning("warning"),
    Error("error"),
    Unknown("unknown");

    private String status;

    StepStatuses(String value) {
        this.status = value;
    }

    @Override
    public java.lang.String toString() {
        return status;
    }
}
