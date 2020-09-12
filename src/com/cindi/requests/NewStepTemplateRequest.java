package com.cindi.requests;

import com.cindi.domain.DynamicDataDescription;

import java.util.Dictionary;
import java.util.HashMap;

public class NewStepTemplateRequest {
    public String Name;
    public String Version;
    public String Description;
    public Boolean AllowDynamicInputs;
    public HashMap<String, DynamicDataDescription> InputDefinitions;
    public HashMap<String, DynamicDataDescription> OutputDefinitions;
}
