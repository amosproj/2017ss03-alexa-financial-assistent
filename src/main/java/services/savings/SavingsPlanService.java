package services.savings;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.SpeechService;

import java.util.Map;

public class SavingsPlanService implements SpeechService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SavingsPlanService.class);

    /**
     * Singleton
     */
    private static SavingsPlanService savingsPlanService = new SavingsPlanService();

    public static SavingsPlanService getInstance() {
        return savingsPlanService;
    }

    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        Map<String, Slot> slots = request.getIntent().getSlots();
        Slot grundbetragSlot = slots.get("Grundbetrag");
        String speechText, repromptText;

        LOGGER.info("onIntent...");

        if (grundbetragSlot.getValue() == null) {
            LOGGER.info("if...");
            speechText = "Wie ist denn ueberhaupt der Grundbetrag?";
            repromptText = "Wie ist der Grundbetrag?";

            return getSpeechletResponse(speechText, repromptText, true);
        } else {
            LOGGER.info("GrundbetragSlot: " + grundbetragSlot.getValue());
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("CreditLimit");
        card.setContent("TEST");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("HALLO");

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
                                                   boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }
}
