import com.cindi.CindiBotFactory;
import com.cindi.domain.Step;
import com.cindi.domain.StepTemplate;
import com.cindi.requests.UpdateStepRequest;
import com.fasterxml.jackson.databind.MapperFeature;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class Main extends CindiBotFactory {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        System.out.println("Hello World");
        Integer threads = 10;

        StepLibrary.mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

        for(Integer i = 0; i < threads; i++ )
        {
        StartBot(SampleBotThread.class,"BOT", "https://localhost:5021", new StepTemplate[] {
                StepLibrary.StepTemplate()
        }, 1000);}
        System.out.println("All ran");

    }

    public UpdateStepRequest HandleStep(Step step) {
        return null;
    }
}
