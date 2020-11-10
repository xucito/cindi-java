package com.cindi;

import com.cindi.domain.DynamicDataDescription;
import com.cindi.domain.Step;
import com.cindi.domain.StepTemplate;
import com.cindi.domain.enums.StepStatuses;
import com.cindi.requests.NewStepTemplateRequest;
import com.cindi.requests.StepRequest;
import com.cindi.requests.UpdateStepRequest;
import com.cindi.utilities.SecurityUtility;
import com.cindi.valueobjects.NextStep;
import org.bouncycastle.crypto.InvalidCipherTextException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class BotThread {

    public void Start(String botName, String botUrl, StepTemplate[] stepTemplates, Integer sleepTime) throws Exception {
        CindiBotClient client = new CindiBotClient(botName, botUrl);
        List<String> registeredIds = new ArrayList<>();
        HashMap<String, StepTemplate> templateMap = new HashMap<>();

        for (StepTemplate template : stepTemplates) {
            System.out.println("Registering " + template.ReferenceId);
            client.PostStepTemplate(new NewStepTemplateRequest() {
                String Name = template.Name();
                String Version = template.Version();
                String Description = template.Description;
                Boolean AllowDynamicInputs = template.AllowDynamicInputs;
                HashMap<String, DynamicDataDescription> InputDefinitions = template.InputDefinitions;
                HashMap<String, DynamicDataDescription> OutputDefinitions = template.OutputDefinitions;
            });
            registeredIds.add(template.ReferenceId);
            System.out.println("Successfully registered " + template.ReferenceId);
            templateMap.put(template.ReferenceId, template);
        }

        while (true) {
            try {
                runOnce(client, registeredIds, templateMap);
            } catch (Exception e) {
                System.out.println("Encountered error " + e.getMessage() + "\n" + e.getStackTrace());
            }
            Thread.sleep(sleepTime);
        }
    }

    public void runOnce(CindiBotClient client, List<String> registeredIds, HashMap<String, StepTemplate> templateMap) throws Exception {
        System.out.println("Getting next step...");
        Step nextStep = null;
        NextStep result = null;
        try {
            result = client.GetNextStep(new StepRequest() {
                String[] StepTemplateIds = registeredIds.stream().toArray(String[]::new);
            });

            if (result.Step != null) {
                nextStep = result.Step;
                HashMap<String, Object> inputs = new HashMap<>();
                StepTemplate template = templateMap.get(nextStep.StepTemplateId);
                String encryptionKey = result.EncryptionKey;

                try {
                    String finalKey = SecurityUtility.decryptRSA(encryptionKey, client.privateKeyRaw);
                    nextStep.Inputs.forEach((k, v) -> {
                        if (template.InputDefinitions.get(k).Type.toLowerCase().equals("secret")) {
                            try {
                                inputs.put(k, SecurityUtility.decryptAES(finalKey, (String) v));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            inputs.put(k, v);
                        }
                    });
                } catch (Exception e) {
                    throw e;
                }
                nextStep.Inputs = inputs;
                System.out.println("Processing step " + nextStep.Id + "...");
                long startTime = System.currentTimeMillis();
                UpdateStepRequest update = HandleStep(nextStep);
                update.Id = nextStep.Id;
                String finalKey = SecurityUtility.GenerateRandomString(32);

                HashMap<String, Object> outputs = new HashMap<>();
                System.out.println(update.Status);
                if (update.Outputs != null && update.Outputs.size() > 0) {
                    update.Outputs.forEach((k, v) -> {
                        if (template.OutputDefinitions.get(k).Type.toLowerCase().equals("secret")) {
                            try {
                                outputs.put(k, SecurityUtility.encryptAES(finalKey, (String) v));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            outputs.put(k, v);
                        }
                    });
                }
                update.EncryptionKey = SecurityUtility.encryptRSA(finalKey, client.privateKeyRaw);
                update.Outputs = outputs;
                client.CompleteStep(update);
                System.out.println("Successfully processed step " + nextStep.Id + " took " + (System.currentTimeMillis() - startTime) + "ms.");
            } else {
                System.out.println("No step found...");
            }
        } catch (Exception e) {
            throw e;
        }

    }

    public abstract UpdateStepRequest HandleStep(Step step) throws Exception;
}
