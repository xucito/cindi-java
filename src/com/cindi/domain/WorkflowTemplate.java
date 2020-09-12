package com.cindi.domain;

import java.util.Dictionary;
import java.util.HashMap;

public class WorkflowTemplate {

    public String ReferenceId;
    public String Name() { return ReferenceId.split(":")[0]; }
    public String Version() { return ReferenceId.split(":")[1]; }

    public String Description;
    public HashMap<String, LogicBlock> LogicBlocks;

    /// <summary>
    /// Input from dependency with input name is the dictionary key and the type as the Dictionary value
    /// </summary>
    public HashMap<String, DynamicDataDescription> InputDefinitions;
}
