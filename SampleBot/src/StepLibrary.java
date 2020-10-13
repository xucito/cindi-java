import com.cindi.domain.*;
import com.cindi.domain.enums.InputDataTypes;
import com.cindi.domain.enums.OperatorStatements;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;


public class StepLibrary {
    static ObjectMapper mapper = new ObjectMapper();

    public static StepTemplate StepTemplate() throws JsonProcessingException {
        return mapper.readValue("{\n" +
                "            \"referenceId\": \"Fibonacci_stepTemplate:0\",\n" +
                "            \"description\": null,\n" +
                "            \"allowDynamicInputs\": false,\n" +
                "            \"inputDefinitions\": {\n" +
                "                \"n-2\": {\n" +
                "                    \"description\": null,\n" +
                "                    \"type\": \"int\"\n" +
                "                },\n" +
                "                \"n-1\": {\n" +
                "                    \"description\": null,\n" +
                "                    \"type\": \"int\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"outputDefinitions\": {\n" +
                "                \"n\": {\n" +
                "                    \"description\": null,\n" +
                "                    \"type\": \"int\"\n" +
                "                }\n" +
                "            }\n" +
                "        }", StepTemplate.class);
    }

    public static StepTemplate SecretStepTemplate() throws JsonProcessingException {
        return mapper.readValue("{\n" +
                "            \"referenceId\": \"Pass_Password:0\",\n" +
                "            \"description\": null,\n" +
                "            \"allowDynamicInputs\": false,\n" +
                "            \"inputDefinitions\": {\n" +
                "                \"secret\": {\n" +
                "                    \"description\": null,\n" +
                "                    \"type\": \"secret\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"outputDefinitions\": {\n" +
                "                \"secret\": {\n" +
                "                    \"description\": null,\n" +
                "                    \"type\": \"secret\"\n" +
                "                }\n" +
                "            }\n" +
                "        }", StepTemplate.class);
    }
}
