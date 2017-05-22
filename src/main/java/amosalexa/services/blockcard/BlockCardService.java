package amosalexa.services.blockcard;

import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import model.banking.AccountFactory;
import model.banking.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BlockCardService  implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(BlockCardService.class);

    private static final String BLOCK_CARD_INTENT = "BlockCardIntent";

    /**
     *
     */
    private static final String number = "0000000001";

    /**
     * Name for custom slot types
     */
    private static final String BANK_CARD_NUMBER_SLOT = "BankCardNumber";

    public BlockCardService(SpeechletSubject speechletSubject){
        subscribe(speechletSubject);
    }

    /**
     * ties the Speechlet Subject (Amos Alexa Speechlet) with an Speechlet Observer
     * @param speechletSubject service
     */
    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, BLOCK_CARD_INTENT);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        // TODO: Use account later to actually block a card
        Account account = AccountFactory.getInstance().getAccount(number);

        String bankCardNumber = request.getIntent().getSlot(BANK_CARD_NUMBER_SLOT).getValue();

        if (request.getIntent().equals("AMAZON.YesIntent")) {
            // TODO: Block card
            return null;
        } else if (request.getIntent().equals("AMAZON.NoIntent")) {
            // TODO: Cancel
            return null;
        } else {
            if (bankCardNumber == null) {
                String speechText = "Wie lautet die Nummber der Karte?";
                String repromptText = "Sagen Sie auch die Nummber der Karte. Zum Beispiel: Sperre Karte 12345.";

                return getSpeechletResponse(speechText, repromptText, false);
            } else {
                session.setAttribute("BlockCardService.CardNumber", Long.parseLong(bankCardNumber));

                String speechText = "Anfrage zur Sperrung entgegengenommen.";
                String repromptText = String.format("Möchten Sie die Karte %d wirklich sperren?", bankCardNumber);

                return getSpeechletResponse(speechText, repromptText, true);
            }
        }
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
                                                   boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Block Bank Card");
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
