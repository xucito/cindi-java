package com.cindi.domain;

import java.util.Dictionary;
import java.util.HashMap;

public class ConditionGroup {
    public String Operator;
    public HashMap<String, Condition> Conditions;
    public HashMap<String, ConditionGroup> ConditionGroups;
}
