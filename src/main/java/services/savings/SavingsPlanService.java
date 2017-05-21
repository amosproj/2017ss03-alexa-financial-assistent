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
        Slot anzahlJahreSlot = slots.get("AnzahlJahre");
        Slot monatlicheEinzahlungSlot = slots.get("EinzahlungMonat");

        String speechText, repromptText;

        String KEY_GRUNDBETRAG = "KEY_GRUNDBETRAG";
        String KEY_JAHRE = "KEY_JAHRE";
        String KEY_MONATLICH = "KEY_MONATLICH";

        LOGGER.info("Grundbetrag: " + grundbetragSlot.getValue());
        LOGGER.info("Jahre: " + anzahlJahreSlot.getValue());
        LOGGER.info("monatliche Einzahlung: " + monatlicheEinzahlungSlot.getValue());
        LOGGER.info("Session: " + session.getAttributes());

        if (anzahlJahreSlot.getValue() != null) {
            session.setAttribute(KEY_JAHRE, anzahlJahreSlot.getValue());
            LOGGER.info("DAS HIER SOLLTE NULL SEIN: "+ anzahlJahreSlot.getValue());
        }

        if (grundbetragSlot.getValue() != null) {
            session.setAttribute(KEY_GRUNDBETRAG, grundbetragSlot.getValue());
        }

        if (monatlicheEinzahlungSlot.getValue() != null) {
            session.setAttribute(KEY_MONATLICH, monatlicheEinzahlungSlot.getValue());
        }

        if (session.getAttributes().containsKey(KEY_GRUNDBETRAG)
            && session.getAttributes().containsKey(KEY_JAHRE)
                && session.getAttributes().containsKey(KEY_MONATLICH)) {
            speechText = calculateSavings(grundbetragSlot.getValue(), monatlicheEinzahlungSlot.getValue(), anzahlJahreSlot.getValue());
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, false);

        }

        if (grundbetragSlot.getValue() == null && !session.getAttributes().containsKey(KEY_GRUNDBETRAG)) {
            speechText = "Wie ist denn ueberhaupt der Grundbetrag?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        } else {
            session.setAttribute(KEY_GRUNDBETRAG, grundbetragSlot.getValue());
        }

        if (monatlicheEinzahlungSlot.getValue() == null && !session.getAttributes().containsKey(KEY_MONATLICH)) {
            speechText = "Wieviel moechtest du monatlich einzahlen?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        } else {
            session.setAttribute(KEY_MONATLICH, monatlicheEinzahlungSlot.getValue());
        }

        if (anzahlJahreSlot.getValue() == null && !session.getAttributes().containsKey(KEY_JAHRE)) {
            speechText = "Wie viele Jahre moechtest du das Geld anlegen?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        } else {
            session.setAttribute(KEY_JAHRE, anzahlJahreSlot.getValue());
        }

//        String StrGrundbetrag = (String) session.getAttribute(KEY_GRUNDBETRAG);
//        String StrMonatlicheZahlung = (String) session.getAttribute(KEY_MONATLICH);
//        String StrAnzahlJahre = (String) session.getAttribute(KEY_JAHRE);
//
//        speechText = calculateSavings(StrGrundbetrag, StrMonatlicheZahlung, StrAnzahlJahre);
        speechText = "TEST";
        repromptText = "TEST";


        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("CreditLimit");
        card.setContent("TEST");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("HALLO");

        return getSpeechletResponse(speechText, repromptText, true);
        // return SpeechletResponse.newTellResponse(speech, card);
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

    private String calculateSavings(String grundbetrag, String monatlicheEinzahlung, String jahre) {

        double gb = Double.valueOf(grundbetrag);
        double m = Double.valueOf(monatlicheEinzahlung);
        double j = Double.valueOf(jahre);
        double zins = 2;
        double result;
        double klammer;

        klammer = 1 + zins/100;
        result = gb * Math.pow(klammer, j);

        String strResult = String.valueOf(result);

        return strResult;


    }


}
