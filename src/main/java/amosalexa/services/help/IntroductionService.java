package amosalexa.services.help;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Introduction speech service. Gives a basic information to the user of what he can say or ask AMOS.
 */
public class IntroductionService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    private static final String AMOS_INTRODUCTION_INTENT = "IntroductionIntent";

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                AMOS_INTRODUCTION_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                AMOS_INTRODUCTION_INTENT
        );
    }

    public IntroductionService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * Default value for cards
     */
    private static final String INTRODUCTION = "AMOS Einführung";
    private static final Logger LOGGER = LoggerFactory.getLogger(IntroductionService.class);

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = intent.getName();
        LOGGER.info("Intent name: " + intentName);
        Session session = requestEnvelope.getSession();
        //String context = (String) session.getAttribute(DIALOG_CONTEXT);

        if (AMOS_INTRODUCTION_INTENT.equals(intentName)) {
            session.setAttribute(DIALOG_CONTEXT, intentName);
            return getIntroduction(intent);
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse getIntroduction(Intent intent) {
        //TODO improve speech by ssml, continue
        String introductionSsml = "Willkommen bei AMOS, der sprechenden Banking-App! Mit mir kannst du deine Bank-Geschaefte" +
                " mit Sprachbefehlen erledigen. Ich möchte dir kurz vorstellen, was ich alles kann. Du kannst mich zum Beispiel" +
                " fragen: \"Was ist mein Kontostand?\" oder \"Wie sind meine Daueraufträge?\" Cool oder?";
        return getSSMLResponse(INTRODUCTION, introductionSsml);
    }
}