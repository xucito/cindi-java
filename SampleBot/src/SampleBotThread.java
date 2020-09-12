import com.cindi.BotThread;
import com.cindi.domain.Step;
import com.cindi.domain.enums.StepStatuses;
import com.cindi.requests.UpdateStepRequest;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.UUID;

public class SampleBotThread extends BotThread {
    @Override
    public UpdateStepRequest HandleStep(Step step) throws Exception {
        switch (step.StepTemplateId)
        {
            case "Fibonacci_stepTemplate:0":
                Integer n1 = (Integer)step.Inputs.get("n-1");
                Integer n2 = (Integer)step.Inputs.get("n-2");
                return new UpdateStepRequest(){
                    String Status = StepStatuses.Successful.toString();
                    HashMap<String, Object> Outputs = new HashMap<String, Object>(){
                        {
                            put("n", n1 + n2);
                        }
                    };
                    Integer StatusCode = 0;
                };
            default:
                throw new Exception("Steptemplate with id " + step.StepTemplateId + " is not implemented");
        }
    }
}
