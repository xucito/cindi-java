package com.cindi.domain;

import java.util.*;

public class Step {
    public UUID Id;
    public String Name;
    public String Description;
    public UUID WorkflowId;
    public String StepTemplateId;
    public HashMap<String, Object> Inputs;
    public Date CompletedOn;
    public String Status;
    public HashMap<String, Object> Outputs;
    public int StatusCode;
    public List<StepLog> Logs;
    public Date SuspendedUntil;
}
