package com.cindi;

import com.cindi.domain.DynamicDataDescription;
import com.cindi.domain.Step;
import com.cindi.domain.StepTemplate;
import com.cindi.domain.enums.StepStatuses;
import com.cindi.requests.NewStepTemplateRequest;
import com.cindi.requests.StepRequest;
import com.cindi.requests.UpdateStepRequest;
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

    Thread thread;

    public void Start(String botName, String botUrl, StepTemplate[] stepTemplates, Integer sleepTime) throws NoSuchAlgorithmException, IOException, KeyManagementException {
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

        thread = new Thread() {
            public void run() {
                while (true) {
                    System.out.println("Getting next step...");
                    Step nextStep = null;
                    try {
                        nextStep = client.GetNextStep(new StepRequest() {
                            String[] StepTemplateIds = registeredIds.stream().toArray(String[]::new);
                        });


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyManagementException e) {
                        e.printStackTrace();
                    }

                    if (nextStep != null) {
                        HashMap<String, Object> inputs = new HashMap<>();
                        StepTemplate template = templateMap.get(nextStep.StepTemplateId);
                        nextStep.Inputs.forEach((k, v) -> {
                            String type = template.InputDefinitions.get(k).Type.toLowerCase();
                            if (template.InputDefinitions.get(k).Type.toLowerCase().equals("secret")) {
                                try {
                                    inputs.put(k, CindiBotClient.decrypt((String) v, client.privateKeyRaw));
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                } catch (InvalidKeyException e) {
                                    e.printStackTrace();
                                } catch (NoSuchPaddingException e) {
                                    e.printStackTrace();
                                } catch (BadPaddingException e) {
                                    e.printStackTrace();
                                } catch (IllegalBlockSizeException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                inputs.put(k, v);
                            }
                        });
                        nextStep.Inputs = inputs;
                        System.out.println("Processing step " + nextStep.Id + "...");
                        try {
                            long startTime = System.currentTimeMillis();
                            UpdateStepRequest update = HandleStep(nextStep);
                            update.Id = nextStep.Id;
                            client.CompleteStep(update);
                            System.out.println("Successfully processed step " + nextStep.Id + " took " + (System.currentTimeMillis() - startTime) + "ms.");
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                client.CompleteStep(new UpdateStepRequest() {
                                    String Status = StepStatuses.Error.toString();
                                    String Log = "Failed to complete step with error: \\n" + Arrays.toString(e.getStackTrace());
                                });
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                                noSuchAlgorithmException.printStackTrace();
                            } catch (KeyManagementException keyManagementException) {
                                keyManagementException.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("No step found...");
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                    }
                }
            }
        };

        thread.start();

    }

    public abstract UpdateStepRequest HandleStep(Step step) throws Exception;
}
