package com.cindi.domain;

import java.util.Dictionary;
import java.util.HashMap;

public class SubsequentStep {

    public String Description;
    public String StepTemplateId;
    /// <summary>
    /// the dictionary key is the field to map to
    /// </summary>
    public HashMap<String, Mapping> Mappings;
    public Boolean IsPriority;
}
