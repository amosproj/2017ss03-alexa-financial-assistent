package amosalexa.services.financing;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.aws.DynamoDbClient;
import api.aws.DynamoDbMapper;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import model.banking.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PeriodicTransactionService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                PERIODIC_TRANSACTION_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                PERIODIC_TRANSACTION_INTENT,
                YES_INTENT,
                NO_INTENT,
                STOP_INTENT
        );
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodicTransactionService.class);

    /**
     * Default value for cards
     */
    private static final String PERIODIC_TRANSACTION = "Periodische Transaktion";

    //Intent names
    private static final String PERIODIC_TRANSACTION_INTENT = "PeriodicTransactionIntent";

    // FIXME: Hardcoded Strings for account
    private static final String ACCOUNT_NUMBER = "8888888888";

    // Key for the transaction number (used as slot key as well as session key!)
    private static final String TRANSACTION_NUMBER_KEY = "TransactionNumber";

    private DynamoDbMapper dynamoDbMapper = new DynamoDbMapper(DynamoDbClient.getAmazonDynamoDBClient());

    public PeriodicTransactionService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

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
        LOGGER.info("Intent Name: " + intentName);
        String context = (String) session.getAttribute(DIALOG_CONTEXT);
        String withinDialogContext = (String) session.getAttribute(WITHIN_DIALOG_CONTEXT);
        LOGGER.info("Within context: " + withinDialogContext);

        if (PERIODIC_TRANSACTION_INTENT.equals(intentName)) {
            session.setAttribute(DIALOG_CONTEXT, intentName);
            return listPeriodicTransactionSuggestions(session);
        } else if (context != null && context.equals(PERIODIC_TRANSACTION_INTENT)) {
            return null;
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse listPeriodicTransactionSuggestions(Session session) {

        List<Transaction> transactions = new ArrayList(AccountAPI.getTransactionsForAccount(ACCOUNT_NUMBER));
        LOGGER.info("Size: " + transactions.size());

        Map<Number, Set<Transaction>> candidates = new HashMap<>();

        for (Transaction t : transactions) {
            LOGGER.info("Transaction: " + t);
            if (!candidates.containsKey(t.getAmount())) {
                LOGGER.info("Not contains");
                Set<Transaction> tList = new HashSet<>();
                tList.add(t);
                candidates.put(t.getAmount(), tList);
            } else {
                candidates.get(t.getAmount()).add(t);
            }
        }

        Iterator<Number> iter = candidates.keySet().iterator();
        while (iter.hasNext()) {
            Number key = iter.next();
            if (candidates.get(key).size() <= 1) {
                iter.remove();
            }
        }

        LOGGER.info("Candidates: " + candidates);

        return getResponse(PERIODIC_TRANSACTION, "Es wurden " + candidates.size() + " moegliche periodische Transaktionen gefunden.");
    }
}