package amosalexa.services.bankaccount;

import amosalexa.SessionStorage;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import model.banking.Account;
import model.banking.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * speech service for bank account information
 * <p>
 * registered Intent @BANK_ACCOUNT_INTENT, @YES_INTENT, @NO_INTENT
 */
public class BankAccountService extends AbstractSpeechService implements SpeechService {


    /**
     * amount of transaction responded at once
     */
    public static final int TRANSACTION_LIMIT = 3;
    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);
    /**
     * intents
     */
    private static final String BANK_ACCOUNT_INTENT = "AccountInformation";
    /**
     * cards
     */
    private static final String CARD_NAME = "Kontoinformation";
    /**
     * bank account number
     */
    private static final String number = "0000000001";
    /**
     * Name for custom slot types
     */
    private static final String SLOT_NAME = "AccountInformationSlots";
    /**
     * default speech texts
     */
    private static final String REPROMPT_TEXT = "Was möchtest du über dein Konto erfahren? Frage mich etwas!";
    private static final String EMPTY_TRANSACTIONS_TEXT = "Du hast keine Transaktionen in deinem Konto";
    private static final String LIST_END_TRANSACTIONS_TEXT = "Du hast keine weiteren Transaktionen";
    private static final String ACCEPTANCE_TEXT = "Verstanden!";
    /**
     * account
     */
    private static Account account;
    /**
     * Slots for transactions
     */
    private final List<String> transactionSlots = new ArrayList<String>() {{
        add("transaktionen");
        add("überweisungen");
        add("umsätze");
    }};
    /**
     * session id
     */
    private String sessionID;
    /**
     * session attribute for transaction list indexe
     */
    private String CONTEXT_FURTHER_TRANSACTION_INDEX = "transaction_dialog_index";
    /**
     *
     */
    private String TRANSACTION_DIALOG = "transaction_dialog";


    public BankAccountService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * ties the Speechlet Subject (Amos Alexa Speechlet) with an Speechlet Observer
     *
     * @param speechletSubject service
     */
    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, BANK_ACCOUNT_INTENT);
        speechletSubject.attachSpeechletObserver(this, YES_INTENT);
        speechletSubject.attachSpeechletObserver(this, NO_INTENT);
    }


    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {

        Intent intent = requestEnvelope.getRequest().getIntent();
        sessionID = requestEnvelope.getSession().getSessionId();

        // get dialog context index
        SessionStorage sessionStorage = SessionStorage.getInstance();
        Integer furtherTransactionDialogIndex = (Integer) sessionStorage.getObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX);
        String currentDialog = (String) sessionStorage.getObject(sessionID, SessionStorage.CURRENTDIALOG);

        if (furtherTransactionDialogIndex != null && currentDialog != null) {
            if (intent.getName().equals(YES_INTENT)) {
                return getNextTransaction(furtherTransactionDialogIndex);
            }

            if (intent.getName().equals(NO_INTENT)) {
                return getResponse(CARD_NAME, ACCEPTANCE_TEXT);
            }
        }

        String slotValue = intent.getSlot(SLOT_NAME) != null ? intent.getSlot(SLOT_NAME).getValue().toLowerCase() : null;
        if (slotValue != null) {
            setAccount();
            if (transactionSlots.contains(slotValue)) {
                return handleTransactionSpeech();
            }
            String speech = account.getSpeechTexts().get(slotValue);
            return getSSMLResponse(CARD_NAME, speech);
        }

        return null;
    }

    /**
     * set up speech texts in account
     */
    private void setAccount() {
        account = AccountAPI.getAccount(number);
        account.setSpeechTexts();
    }

    /**
     * responses each transaction from a account
     *
     * @return SpeechletResponse to alexa
     */
    private SpeechletResponse handleTransactionSpeech() {
        List<Transaction> transactions = Transaction.getTransactions(account);

        if (transactions == null || transactions.isEmpty()) {
            log.warn("Account: " + account.getNumber() + " has no transactions");
            return getResponse(CARD_NAME, EMPTY_TRANSACTIONS_TEXT);
        }

        StringBuilder stringBuilder = new StringBuilder(Transaction.getTransactionSizeText(transactions.size()));
        int i;
        for (i = 0; i < TRANSACTION_LIMIT; i++) {
            stringBuilder.append(Transaction.getTransactionText(transactions.get(i)));
        }

        if (i - 1 < transactions.size()) {
            stringBuilder.append(Transaction.getAskMoreTransactionText());
            SessionStorage sessionStorage = SessionStorage.getInstance();
            sessionStorage.putObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX, i);
            sessionStorage.putObject(sessionID, SessionStorage.CURRENTDIALOG, TRANSACTION_DIALOG);
        } else {
            return getResponse(CARD_NAME, LIST_END_TRANSACTIONS_TEXT);
        }

        return getSSMLAskResponse(CARD_NAME, stringBuilder.toString(), REPROMPT_TEXT);
    }

    /**
     * returns a response with the next transaction in the list
     * @param i index at the current postion in the transaction list
     * @return speechletResponse
     */
    private SpeechletResponse getNextTransaction(int i) {
        List<Transaction> transactions = Transaction.getTransactions(account);
        String transactionText = Transaction.getTransactionText(transactions.get(i));
        if (i - 1 < transactions.size()) {
            transactionText = transactionText + Transaction.getAskMoreTransactionText();
            SessionStorage sessionStorage = SessionStorage.getInstance();
            sessionStorage.putObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX, i + 1);
            sessionStorage.putObject(sessionID, SessionStorage.CURRENTDIALOG, TRANSACTION_DIALOG);
        }
        return getSSMLAskResponse(CARD_NAME, transactionText, REPROMPT_TEXT);
    }
}
