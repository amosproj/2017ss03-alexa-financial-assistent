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

    private static final String GRUNDBETRAG_KEY = "Grundbetrag";

    private static final String ANZAHL_JAHRE_KEY = "AnzahlJahre";

    private static final String EINZAHLUNG_MONAT_KEY = "EinzahlungMonat";

    /**
     * Singleton
     */
    private static SavingsPlanService savingsPlanService = new SavingsPlanService();

    public static SavingsPlanService getInstance() {
        return savingsPlanService;
    }

    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {

        Map<String, Slot> slots = request.getIntent().getSlots();
        Slot grundbetragSlot = slots.get(GRUNDBETRAG_KEY);
        Slot anzahlJahreSlot = slots.get(ANZAHL_JAHRE_KEY);
        Slot monatlicheEinzahlungSlot = slots.get(EINZAHLUNG_MONAT_KEY);

        String speechText, repromptText;

        LOGGER.info("Grundbetrag: " + grundbetragSlot.getValue());
        LOGGER.info("Jahre: " + anzahlJahreSlot.getValue());
        LOGGER.info("monatliche Einzahlung: " + monatlicheEinzahlungSlot.getValue());
        LOGGER.info("Session Before: " + session.getAttributes());

        if (grundbetragSlot.getValue() != null) {
            session.setAttribute(GRUNDBETRAG_KEY, grundbetragSlot.getValue());
        }
        if (anzahlJahreSlot.getValue() != null) {
            session.setAttribute(ANZAHL_JAHRE_KEY, anzahlJahreSlot.getValue());
        }
        if (monatlicheEinzahlungSlot.getValue() != null) {
            session.setAttribute(EINZAHLUNG_MONAT_KEY, monatlicheEinzahlungSlot.getValue());
        }

        if (grundbetragSlot.getValue() == null && !session.getAttributes().containsKey(GRUNDBETRAG_KEY)) {
            speechText = "Was moechtest du als Grundbetrag anlegen?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        if (anzahlJahreSlot.getValue() == null && !session.getAttributes().containsKey(ANZAHL_JAHRE_KEY)) {
            speechText = "Wie viele Jahre moechtest du das Geld anlegen?";
            //TODO better use duration?
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        if (monatlicheEinzahlungSlot.getValue() == null && !session.getAttributes().containsKey(EINZAHLUNG_MONAT_KEY)) {
            if (grundbetragSlot.getValue() == null && !session.getAttributes().containsKey(GRUNDBETRAG_KEY)) {
                speechText = "Du musst zuerst einen Grundbetrag angeben.";
                repromptText = speechText;
                return getSpeechletResponse(speechText, repromptText, true);
            }
            speechText = "Wie viel Geld moechtest du monatlich investieren?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        String grundbetragString = (String) session.getAttribute(GRUNDBETRAG_KEY);
        String einzahlungMonatString = (String) session.getAttribute(EINZAHLUNG_MONAT_KEY);
        String anzahlJahreString = (String) session.getAttribute(ANZAHL_JAHRE_KEY);

        speechText = "Bei einem Zinssatz von zwei Prozent wäre der Gesamtsparbetrag am Ende des Zeitraums insgesamt " +
                calculateSavings(grundbetragString, einzahlungMonatString, anzahlJahreString) + "Euro. Soll ich diesen Sparplan für dich anlegen?";
        repromptText = speechText;

        LOGGER.info("Session Afterwards: " + session.getAttributes());

        return getSpeechletResponse(speechText, repromptText, true);
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
        //TODO not using zins?
        double zins = 2;
        double result;
        double klammer;

        klammer = 1 + zins / 100;
        result = gb * Math.pow(klammer, j);

        String strResult = String.valueOf(result);

        return strResult;
    }

}
