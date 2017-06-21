package amosalexa.services.cards;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import model.banking.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ReplacementCardService extends AbstractSpeechService implements SpeechService {

    private static final String REPLACEMENT_CARD = "Ersatzkarte";

    private enum ReplacementReason {
        BLOCKED,
        DAMAGED
    }

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                REPLACEMENT_CARD_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                REPLACEMENT_CARD_INTENT,
                FOUR_DIGIT_NUMBER_INTENT,
                REPLACEMENT_CARD_REASON_INTENT,
                YES_INTENT,
                NO_INTENT
        );
    }

    public ReplacementCardService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ReplacementCardService.class);

    public static final String REPLACEMENT_CARD_INTENT = "ReplacementCardIntent";
    public static final String FOUR_DIGIT_NUMBER_INTENT = "FourDigitNumberIntent";
    public static final String REPLACEMENT_CARD_REASON_INTENT = "ReplacementCardReasonIntent";

    private static final String STORAGE_VALID_CARDS = "REPLACEMENT_VALID_CARDS";
    private static final String STORAGE_SELECTED_CARD = "REPLACEMENT_SELECTED_CARD";
    private static final String STORAGE_REASON = "REPLACEMENT_REASON";

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = intent.getName();
        Session session = requestEnvelope.getSession();
        LOGGER.info("Intent Name: " + intentName);
        String context = (String) session.getAttribute(CONTEXT);
        LOGGER.info("Context: " + context);

        if (REPLACEMENT_CARD_INTENT.equals(intentName)) {
            session.setAttribute(CONTEXT, REPLACEMENT_CARD_INTENT);
            return askForCardNumber(session, false);
        } else if (FOUR_DIGIT_NUMBER_INTENT.equals(intentName) && context != null && context.equals(REPLACEMENT_CARD)) {
            return askIfBlockedOrDamaged(intent, session);
        } else if (REPLACEMENT_CARD_REASON_INTENT.equals(intentName) && context != null && context.equals(REPLACEMENT_CARD)) {
            return askForConfirmation(intent, session);
        } else if (YES_INTENT.equals(intentName) && context != null && context.equals(REPLACEMENT_CARD)) {
            return orderReplacement(intent, session);
        } else if (NO_INTENT.equals(intentName) && context != null && context.equals(REPLACEMENT_CARD)) {
            return cancelDialog();
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse askForCardNumber(Session session, boolean errored) {
        Collection<Card> cards = AccountAPI.getCardsForAccount("0000000000"); // TODO: Load account from session

        if (cards.size() == 0) {
            // This user does not have any cards, ordering a replacement card is impossible.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Es wurden keine Kredit- oder EC-Karten gefunden.");
            return SpeechletResponse.newTellResponse(speech);
        } else {
            SsmlOutputSpeech speech = new SsmlOutputSpeech();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("<speak>");

            if (errored) {
                stringBuilder.append("Entschuldigung, das habe ich nicht verstanden. ");
            }

            stringBuilder.append("Bestellung einer Ersatzkarte. Es wurden folgende Karten gefunden: ");

            List<String> userCards = new ArrayList<>();

            for (Card card : cards) {
                // Check if this card is active
                if (card.getStatus() != Card.Status.ACTIVE) {
                    continue;
                }

                userCards.add(card.getCardNumber());

                String prefix = (card.getCardType() == Card.CardType.CREDIT ? "Kredit" : "EC-");
                stringBuilder.append(prefix + "karte mit den Endziffern <say-as interpret-as=\"digits\">" +
                        card.getCardNumber().substring(card.getCardNumber().length() - 4) + "</say-as>. ");
            }

            // Store all card numbers in the session
            session.setAttribute(STORAGE_VALID_CARDS, userCards);

            stringBuilder.append("Bitte gib die Endziffern der Karte an, für die du Ersatz benötigst.");
            stringBuilder.append("</speak>");
            speech.setSsml(stringBuilder.toString());

            // Create reprompt
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newAskResponse(speech, reprompt);
        }
    }

    private SpeechletResponse askIfBlockedOrDamaged(Intent intent, Session session) {
        String fourDigits = intent.getSlot("FourDigits").getValue();

        LOGGER.info("Digits: " + fourDigits);
        boolean validDigits = false;

        // Check if these digits are valid
        List<String> userCards = (List<String>) session.getAttribute(STORAGE_VALID_CARDS);
        for (String cardNumber : userCards) {
            if (cardNumber.substring(cardNumber.length() - 4).equals(fourDigits)) {
                // Digits are valid
                session.setAttribute(STORAGE_SELECTED_CARD, cardNumber);
                validDigits = true;
                break;
            }
        }

        // If these are invalid digits, ask again
        if (!validDigits) {
            return askForCardNumber(session, true);
        }

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Wurde die Karte gesperrt oder wurde sie beschädigt?");

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse askForConfirmation(Intent intent, Session session) {
        if (!session.getAttributes().containsKey(STORAGE_SELECTED_CARD)) {
            return askForCardNumber(session, true);
        }

        String replacementReason = intent.getSlot("ReplacementReason").getValue();
        LOGGER.info("Replacement reason: " + replacementReason);

        if (replacementReason.equals("beschaedigt")) {
            LOGGER.info("Beschädigt");
            session.setAttribute(STORAGE_REASON, ReplacementReason.DAMAGED);
        } else if (replacementReason.equals("gesperrt")) {
            LOGGER.info("Gesperrt");
            session.setAttribute(STORAGE_REASON, ReplacementReason.BLOCKED);
        } else {
            return askIfBlockedOrDamaged(intent, session);
        }

        SsmlOutputSpeech speech = new SsmlOutputSpeech();

        String reason = session.getAttribute(STORAGE_REASON) == ReplacementReason.DAMAGED ? "beschädigte" : "gesperrte";
        String lastDigits = (String) session.getAttribute(STORAGE_SELECTED_CARD);
        lastDigits = lastDigits.substring(lastDigits.length() - 4);
        speech.setSsml("<speak>Soll ein Ersatz für die " + reason + " Karte mit den Endziffern <say-as interpret-as=\"digits\">" +
                lastDigits + "</say-as> bestellt werden?</speak>");

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse orderReplacement(Intent intent, Session session) {
        if (!session.getAttributes().containsKey(STORAGE_SELECTED_CARD)) {
            return askForCardNumber(session, true);
        }
        if (!session.getAttributes().containsKey(STORAGE_REASON)) {
            return askIfBlockedOrDamaged(intent, session);
        }

        // TODO: Actually order a replacement card

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Okay, eine Ersatzkarte wurde bestellt.");

        return SpeechletResponse.newTellResponse(speech);
    }

    private SpeechletResponse cancelDialog() {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("");

        return SpeechletResponse.newTellResponse(speech);
    }
}
