package amosalexa.services.budgetreport;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.aws.EMailClient;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

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
                BUDGET_REPORT_EMAIL_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                BUDGET_REPORT_EMAIL_INTENT
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

    private static final String BUDGET_REPORT_EMAIL_INTENT = "BudgetReportEMailIntent";

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        IntentRequest request = requestEnvelope.getRequest();

        if (request.getIntent().getName().equals(BUDGET_REPORT_EMAIL_INTENT)) {
            // Load the mail template from resources
            JtwigTemplate template = JtwigTemplate.classpathTemplate("html-templates/budget-report.twig");

            // TODO: Dummy data
            List<BudgetReportCategory> categories = new ArrayList<>();
            categories.add(new BudgetReportCategory("Gesundheit", 130., 10.));
            categories.add(new BudgetReportCategory("Bildung", 100., 0.));
            categories.add(new BudgetReportCategory("Lebensmittel", 350., 280.));
            categories.add(new BudgetReportCategory("Kleidung", 75., 90.));
            categories.add(new BudgetReportCategory("Auto",  200., 62.));
            categories.add(new BudgetReportCategory("Wohltätigkeit", 100., 120.));
            categories.add(new BudgetReportCategory("Haushalt", 85., 44.));
            categories.add(new BudgetReportCategory("Urlaub",  100., 40.));
            
            JtwigModel model = JtwigModel.newModel().with("var", "World")
                                                    .with("categories", categories);

            // Render mail template
            String body = template.render(model);

            String answer = "Okay, ich habe dir deinen Ausgabenreport per E-Mail gesendet.";
            if (!EMailClient.SendHTMLEMail("Test-Mail", body)) {
                answer = "Leider konnte der Ausgabenreport nicht gesendet werden.";
            }
            return getResponse("E-Mail gesendet", answer);
        }

        return null;
    }

}
