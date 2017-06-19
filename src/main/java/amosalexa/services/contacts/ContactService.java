package amosalexa.services.contacts;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.DynamoDbClient;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import model.banking.Contact;
import model.banking.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service for contact and contact list related intents.
 */
public class ContactService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    private static final String CONTACT_LIST_INFO_INTENT = "ContactListInfoIntent";
    private static final String CONTACT_ADD_INTENT = "ContactAddIntent";

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                CONTACT_LIST_INFO_INTENT,
                CONTACT_ADD_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                CONTACT_LIST_INFO_INTENT,
                CONTACT_ADD_INTENT,
                YES_INTENT,
                NO_INTENT
        );
    }

    public ContactService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * Default value for cards
     */
    private static final String CONTACTS = "Kontakte";
    private static final Logger LOGGER = LoggerFactory.getLogger(ContactService.class);

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
        Session session = requestEnvelope.getSession();
        String context = (String) session.getAttribute(CONTEXT);

        if (CONTACT_LIST_INFO_INTENT.equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            session.setAttribute(CONTEXT, intent.getName());
            return getContactListInfo(session);
        } else if (CONTACT_ADD_INTENT.equals(intentName)) {
            session.setAttribute(CONTEXT, intent.getName());
            return askForNewContactConfirmation(intent, session);
        } else if (YES_INTENT.equals(intentName) && context.equals(CONTACT_ADD_INTENT)) {
            return createNewContact(session);
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse createNewContact(Session session) {
        //TODO test
        //Acutally create and save contact
        String contactName = (String) session.getAttribute("ContactName");
        Contact contact = new Contact(contactName, " DE42100000009999999999");
        DynamoDbClient.instance.putItem(Contact.TABLE_NAME, contact);
        return getResponse(CONTACTS, "Kontakt wurde angelegt.");
    }

    private SpeechletResponse askForNewContactConfirmation(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();
        LOGGER.info("Slots: " + slots);

        Slot transactionNumberSlot = slots.get("TransactionNumber");

        if (transactionNumberSlot.getValue() == null || StringUtils.isBlank(transactionNumberSlot.getValue())) {
            String speechText = "Das habe ich nicht ganz verstanden. Bitte wiederhole deine Eingabe.";
            return getAskResponse(CONTACTS, speechText);
        }

        String speechText = "";
        List<Transaction> allTransactions = Transaction.getTransactions("9999999999");
        Number transactionNumber = Integer.valueOf(transactionNumberSlot.getValue());

        Transaction transaction = null;
        for (Transaction t : allTransactions) {
            if (transactionNumber == t.getTransactionId()) {
                transaction = t;
            }
        }

        if (transaction == null) {
            speechText = "Ich habe keine Transaktion mit dieser Nummer gefunden. Bitte wiederhole deine Eingabe.";
            getAskResponse(CONTACTS, speechText);
        }

        LOGGER.info("Payee: " + transaction.getPayee());
        LOGGER.info("Remitter: " + transaction.getRemitter());
        String payee = transaction.getPayee();
        String remitter = transaction.getRemitter();
        String ibanRegex = "^DE([0-9a-zA-Z]\\s?){20}$";
        LOGGER.info("outgoing: " + transaction.isOutgoing());
        if ((payee == null && remitter == null) || (transaction.isOutgoing() && payee.matches(ibanRegex)) ||
                (!transaction.isOutgoing() && remitter.matches(ibanRegex))) {
            speechText = "Ich kann fuer diese Transaktion keine Kontaktdaten speichern, weil der Name des";
            speechText = speechText.concat(transaction.isOutgoing() ? " Zahlungsempfaengers" : " Auftraggebers");
            speechText = speechText.concat(" nicht bekannt ist. Bitte wiederhole deine Eingabe oder breche ab, indem du \"Alexa, Stop!\" sagst.");
            getAskResponse(CONTACTS, speechText);
        } else {
            //TODO improve
            //Actually asking for confirmation
            session.setAttribute("ContactName", transaction.isOutgoing() ? payee : remitter);
            speechText = "Moechtest du ";
            speechText = speechText.concat(transaction.isOutgoing() ? payee : remitter);
            speechText = speechText.concat(" als Kontakt speichern?");
        }
        return getAskResponse(CONTACTS, speechText);
    }

    private SpeechletResponse getContactListInfo(Session session) {
        //TODO ...
        return null;
    }
}
