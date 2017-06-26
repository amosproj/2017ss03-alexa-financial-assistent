package amosalexa.services.contactTransfer;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import amosalexa.services.bankcontact.BankContactService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This dialog allows users to perform a transfer to a {@link model.banking.Contact contact} from the contact book.
 */
public class ContactTransferService extends AbstractSpeechService implements SpeechService {

    /**
     * Logger for debugging purposes.
     */
    private static final Logger log = LoggerFactory.getLogger(BankContactService.class);

    /**
     * This is the default title that this skill will be using for cards.
     */
    private static final String CONTACT_TRANSFER_CARD = "Überweisung an Kontakt";

    //region Intent names

    /**
     * Entry intent for this service.
     */
    private static final String CONTACT_TRANSFER_INTENT = "ContactTransferIntent";

    /**
     * Intent to specify which contact to transfer money to.
     */
    private static final String CONTACT_CHOICE_INTENT = "ContactChoiceIntent";

    //endregion

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = intent.getName();

        switch (intentName) {
            case CONTACT_TRANSFER_INTENT:
                return contactTransfer(intent);
            case CONTACT_CHOICE_INTENT:
                return contactChoice(intent);
            case YES_INTENT:
                return performTransfer(intent);
            case NO_INTENT:
                return getResponse(CONTACT_TRANSFER_CARD, "Okay, verstanden. Dann bis zum nächsten Mal.");
            default:
                return null;
        }
    }

    /**
     * Lists possible contact or directly asks for confirmation.
     */
    private SpeechletResponse contactTransfer(Intent intent) {
        return null;
    }

    /**
     * Asks for confirmation.
     */
    private SpeechletResponse contactChoice(Intent intent) {
        return null;
    }

    /**
     * Performs the transfer.
     */
    private SpeechletResponse performTransfer(Intent intent) {
        return null;
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Collections.singletonList(
                CONTACT_TRANSFER_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                CONTACT_TRANSFER_INTENT,
                CONTACT_CHOICE_INTENT,
                YES_INTENT,
                NO_INTENT
        );
    }

}
