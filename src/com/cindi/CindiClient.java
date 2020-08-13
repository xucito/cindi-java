package com.cindi;

public interface CindiClient {
    String AddStepLog(Guid stepId, string log, string idToken);
    void BackOff(int attempt, string command, Exception e, int maxAttempts);
    String CompleteStep(UpdateStepRequest request, string idToken);
    Task<Step> GetNextStep(StepRequest request, string idToken);
    String PostNewStep(StepInput stepInput, string idToken);
    String PostNewWorkflow(WorkflowInput input, string idToken);
    String PostNewWorkflowTemplate(WorkflowTemplate WorkflowTemplate, string idToken);
    String PostStepTemplate(NewStepTemplateRequest stepTemplate, string idToken);
    Task<NewBotKeyResult> RegisterBot(string name, string rsaPublicKey);
    void SetIdToken(string botId);
}
