package com.cindi.requests;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.UUID;

public class UpdateStepRequest {
    public UUID Id;
    public HashMap<String, Object> Outputs;
    public String Status;
    public Integer StatusCode;
    public String Log;
}
