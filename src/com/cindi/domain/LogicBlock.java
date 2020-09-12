package com.cindi.domain;

import java.util.Dictionary;
import java.util.HashMap;

public class LogicBlock {
    public ConditionGroup Dependencies;
    public HashMap<String, SubsequentStep> SubsequentSteps;
}
