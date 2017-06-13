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


import java.util.*;


public class BankAccountService extends AbstractSpeechService implements SpeechService {

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
     * account
     */
    private static Account account;

    /**
     * Name for custom slot types
     */
    private static final String SLOT_NAME = "AccountInformationSlots";

    /**
     * session id
     */
    private String sessionID;

    /**
     *
     */
    private String CONTEXT_FURTHER_TRANSACTION_INDEX = "transaction_dialog";

    /**
     * speech texts
     */
    private static String speechText = "Was möchtest du über dein Konto erfahren?";
    private static final String repromptText = "Was möchtest du über dein Konto erfahren? Frage mich etwas!";
    private static final String EMPTY_TRANSACTIONS = "Du hast keine Transaktionen in deinem Konto";


    /**
     * Slots for transactions
     */
    private final List<String> transactionSlots = new ArrayList<String>(){{
        add("transaktionen");
        add("überweisungen");
        add("umsätze");
    }};


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
        speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
    }


    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {

        Intent intent = requestEnvelope.getRequest().getIntent();
        sessionID = requestEnvelope.getSession().getSessionId();

        // get dialog context index
        SessionStorage sessionStorage = SessionStorage.getInstance();
        Integer furtherTransactionDialogIndex = (Integer) sessionStorage.getObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX);
        String currentDialog = (String) sessionStorage.getObject(sessionID, SessionStorage.CURRENTDIALOG);

        // check for dialog
        if(furtherTransactionDialogIndex != null && currentDialog != null){
            if(intent.getName().equals("AMAZON.YesIntent")){
                log.info("Account Information Intent - AMAZON.YesIntent");
                return getNextTransaction(furtherTransactionDialogIndex);
            }

            if(intent.getName().equals("AMAZON.NoIntent")){
                log.info("Account Information Intent - AMAZON.NoIntent");
                return getResponse(CARD_NAME, "Verstanden!");
            }
        }

        // check slot values
        String slotValue = intent.getSlot(SLOT_NAME) != null ? intent.getSlot(SLOT_NAME).getValue().toLowerCase() : null;
        if (slotValue != null) {
            log.info("Account Information Intent - Slot: " + slotValue);
            setAccount();

            if (transactionSlots.contains(slotValue)) {
                return handleTransactionSpeech();
            }

            speechText = account.getSpeechTexts().get(slotValue);

            return getSSMLResponse(CARD_NAME, speechText);

        }

        return null;
    }

    public void setAccount(){
        account = AccountAPI.getAccount(number);
        account.setSpeechTexts();
    }

    /**
     * responses each transaction from a account
     * @return SpeechletResponse to alexa
     */
    private SpeechletResponse handleTransactionSpeech() {
        List<Transaction> transactions = getTransactions();

        if (transactions == null || transactions.isEmpty())
            return getResponse(CARD_NAME, EMPTY_TRANSACTIONS);

        // transaction list intro
        StringBuilder stringBuilder = new StringBuilder("Du hast " + transactions.size() + " Transaktionen. ");
        int i;
        for ( i = 0; i < 3; i++) {
            stringBuilder.append(getTransactionText(transactions.get(i)));
        }

        if(i - 1 < transactions.size()){
            stringBuilder.append(getAskMoreTransactionText());
            SessionStorage sessionStorage = SessionStorage.getInstance();
            sessionStorage.putObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX, i);
            sessionStorage.putObject(sessionID, SessionStorage.CURRENTDIALOG, "transactionsDialog");
        }

        return getSSMLAskResponse(CARD_NAME, stringBuilder.toString());
    }

    private String getTransactionIdText(Transaction transaction){
        return "<break time=\"1s\"/>Nummer " + transaction.getTransactionId() + " ";
    }

    private String getAskMoreTransactionText(){
        return "<break time=\"1s\"/> Möchtest du weitere Transaktionen hören";
    }

    private String getTransactionText(Transaction transaction){

        String transactionText = "";
        if (transaction.getSourceAccount() != null) {
            transactionText = getTransactionToAccountText(transaction);
        }
        if (transaction.getDestinationAccount() != null) {
            transactionText = getTransactionFromAccountText(transaction);
        }
        return transactionText;
    }

    private String getTransactionFromAccountText(Transaction transaction) {
        return  getTransactionIdText(transaction) + "Von deinem Konto auf das Konto " + transaction.getDestinationAccount() +
                " in Höhe von <say-as interpret-as=\"unit\">€"
                + Math.abs(transaction.getAmount().doubleValue()) + "</say-as>\n";
    }

    private String getTransactionToAccountText(Transaction transaction) {
        return "Von " + transaction.getSourceAccount() + " auf dein Konto in Höhe von <say-as interpret-as=\"unit\">€"
                + Math.abs(transaction.getAmount().doubleValue()) + "</say-as>\n";
    }

    private List<Transaction> getTransactions() {
        Collection<Transaction> transactions = AccountAPI.getTransactionsForAccount(account.getNumber());
        List<Transaction> txs = new ArrayList<>(transactions);
        Collections.reverse(txs);
        return txs;
    }

    private SpeechletResponse getNextTransaction(int i){
        List<Transaction> transactions = getTransactions();
        String transactionText = getTransactionText(transactions.get(i));
        if(i - 1 < transactions.size()){
            transactionText = transactionText + getAskMoreTransactionText();
            SessionStorage sessionStorage = SessionStorage.getInstance();
            sessionStorage.putObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX, i + 1);
            sessionStorage.putObject(sessionID, SessionStorage.CURRENTDIALOG, "transactionsDialog");
        }
        return getSSMLAskResponse(CARD_NAME, transactionText);
    }
}
