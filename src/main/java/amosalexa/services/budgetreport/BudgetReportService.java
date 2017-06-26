package amosalexa.services.budgetreport;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.aws.EMailClient;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.apache.commons.io.IOUtils;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BudgetReportService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                TEST_EMAIL_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                TEST_EMAIL_INTENT
        );
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    public BudgetReportService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    private static final String TEST_EMAIL_INTENT = "TestEMailIntent";

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        IntentRequest request = requestEnvelope.getRequest();

        if (request.getIntent().getName().equals(TEST_EMAIL_INTENT)) {
            // Load the mail template from resources
            JtwigTemplate template = JtwigTemplate.classpathTemplate("html-templates/budget-report.twig");

            JtwigModel model = JtwigModel.newModel().with("var", "World");

            // Render mail template
            String body = template.render(model);

            String answer = "Okay, ich habe dir eine Test-E-Mail gesendet.";
            if (!EMailClient.SendHTMLEMail("Test-Mail", body)) {
                answer = "Leider konnte die E-Mail nicht gesendet werden.";
            }
            return getResponse("E-Mail gesendet", answer);
        }

        return null;
    }

}
