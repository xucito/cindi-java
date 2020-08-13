package com.cindi.domain;

public class Step {
    public Step();
    public string Name;
    public string Description;
    public Guid? WorkflowId;
        [Required]
    public string StepTemplateId;
    public Dictionary<string, object> Inputs;
    public DateTime? CompletedOn;
    public string Status;
    public Dictionary<string, object> Outputs;
    public int StatusCode;
    public List<StepLog> Logs;
    public DateTime? SuspendedUntil;

    public bool IsComplete();
    public void RemoveDelimiters();
}
