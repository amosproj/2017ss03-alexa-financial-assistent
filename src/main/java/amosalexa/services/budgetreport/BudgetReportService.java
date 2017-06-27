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
import java.util.ArrayList;
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

            List<BudgetCategory> categories = new ArrayList<>();
            categories.add(new BudgetCategory("Gesundheit", "black", 130., 10.));
            categories.add(new BudgetCategory("Bildung", "blue", 100., 0.));
            categories.add(new BudgetCategory("Lebensmittel", "green", 350., 280.));
            categories.add(new BudgetCategory("Kleidung", "lightblue", 75., 25.));
            categories.add(new BudgetCategory("Auto", "orange", 200., 62.));
            categories.add(new BudgetCategory("Wohltätigkeit", "pink", 100., 5.));
            categories.add(new BudgetCategory("Haushalt", "red", 85., 44.));
            categories.add(new BudgetCategory("Urlaub", "yellow", 100., 0.));


            JtwigModel model = JtwigModel.newModel().with("var", "World")
                                                    .with("categories", categories);

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
