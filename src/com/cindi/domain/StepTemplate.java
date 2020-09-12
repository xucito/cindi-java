package com.cindi.domain;

import java.util.Dictionary;
import java.util.HashMap;

public class StepTemplate {

    public String ReferenceId;

    public String Name() { return ReferenceId.split(":")[0]; }
    public String Version() { return ReferenceId.split(":")[1]; }
    public String Description;
    public Boolean AllowDynamicInputs;
    public HashMap<String, DynamicDataDescription> InputDefinitions;
    public HashMap<String, DynamicDataDescription> OutputDefinitions;
}
