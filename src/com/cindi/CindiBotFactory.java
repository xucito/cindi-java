package com.cindi;

import com.cindi.domain.DynamicDataDescription;
import com.cindi.domain.Step;
import com.cindi.domain.StepTemplate;
import com.cindi.requests.NewStepTemplateRequest;
import com.cindi.requests.StepRequest;
import com.cindi.requests.UpdateStepRequest;
import com.sun.org.apache.xpath.internal.operations.Bool;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

public abstract class CindiBotFactory {
    public static ArrayList<BotThread> bots =  new ArrayList<>();

    public static void StartBot(Class<? extends BotThread> botType ,String botName, String url, StepTemplate[] stepTemplates, Integer sleepTime) throws Exception {
        try {
            BotThread newBot = botType.newInstance();
            newBot.Start(botName, url, stepTemplates, sleepTime);
            bots.add(newBot);
        } catch (Exception e) {
            throw e;
        }
    }
}
