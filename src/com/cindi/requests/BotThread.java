package com.cindi.requests;

import com.cindi.CindiBotClient;
import com.cindi.domain.DynamicDataDescription;
import com.cindi.domain.Step;
import com.cindi.domain.StepTemplate;
import com.cindi.valueobjects.NextStep;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BotThread {
    public void Start(String botName, String botUrl, StepTemplate[] stepTemplates) throws NoSuchAlgorithmException, IOException, KeyManagementException {
        CindiBotClient client = new CindiBotClient(botName, botUrl);

        List<String> registeredIds = new ArrayList<>();

        for (StepTemplate template: stepTemplates) {
            System.out.println("Registering " + template.ReferenceId);
            client.PostStepTemplate(new NewStepTemplateRequest(){
                String Name = template.Name();
                String Version = template.Version();
                String Description = template.Description;
                Boolean AllowDynamicInputs = template.AllowDynamicInputs;
                HashMap<String, DynamicDataDescription> InputDefinitions = template.InputDefinitions;
                HashMap<String, DynamicDataDescription> OutputDefinitions = template.OutputDefinitions;
            });
            registeredIds.add(template.ReferenceId);
            System.out.println("Successfully registered " + template.ReferenceId);
        }

        while(true)
        {
            NextStep nextStep = client.GetNextStep(new StepRequest(){
                String[] StepTemplateIds = registeredIds.stream().toArray(String[]::new);
            });

            HandleStep(nextStep);
        }

    }

    public UpdateStepRequest HandleStep(NextStep step){ throw new NotImplementedException();
    }
}
