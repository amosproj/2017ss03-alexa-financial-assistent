package amosalexa.services.blockcard;

import amosalexa.AmosAlexaSpeechlet;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import model.banking.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class BlockCardService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                BLOCK_CARD_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                BLOCK_CARD_INTENT,
                YES_INTENT,
                NO_INTENT
        );
    }

    private static final Logger log = LoggerFactory.getLogger(BlockCardService.class);

    /**
     *
     */
    private static final String number = "0000000001";

    private static final String BLOCK_CARD_INTENT = "BlockCardIntent";

    public BlockCardService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * ties the Speechlet Subject (Amos Alexa Speechlet) with an Speechlet Observer
     *
     * @param speechletSubject service
     */
    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for(String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        // TODO: Use account later to actually block a card
        Account account = AccountAPI.getAccount(number);

        if (request.getIntent().getName().equals(YES_INTENT)) {
            String cardNumberObj = (String) session.getAttribute("BlockCardService.CardNumber");

            if (cardNumberObj != null) {
                long cardNumber = Long.parseLong(cardNumberObj);

                // TODO: Lock card with number cardNumber

                return AmosAlexaSpeechlet.getSpeechletResponse("Karte " + cardNumberObj + " wurde gesperrt.", "", false);
            }

            return null;
        } else if (request.getIntent().getName().equals(NO_INTENT)) {
            session.setAttribute("BlockCardService.CardNumber", null);
            return AmosAlexaSpeechlet.getSpeechletResponse("Okay, tschüss.", "", false);
        } else if (request.getIntent().getName().equals(BLOCK_CARD_INTENT)) {
            String bankCardNumber = request.getIntent().getSlot("BankCardNumber").getValue();

            if (bankCardNumber == null) {
                String speechText = "Wie lautet die Nummber der Karte?";
                String repromptText = "Sagen Sie auch die Nummer der Karte. Zum Beispiel: Sperre Karte 12345.";

                return AmosAlexaSpeechlet.getSpeechletResponse(speechText, repromptText, false);
            } else {
                session.setAttribute("BlockCardService.CardNumber", bankCardNumber);

                String speechText = "Möchten Sie die Karte " + bankCardNumber + " wirklich sperren?";
                String repromptText = "Bitte bestätigen Sie, indem Sie 'ja' sagen.";

                return AmosAlexaSpeechlet.getSpeechletResponse(speechText, repromptText, true);
            }
        }

        return null;
    }
}
