package amosalexa.services.bankaccount;

import amosalexa.SessionStorage;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
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
     * your account
     */
    private final Account account = AccountAPI.getAccount(number);

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
    private static final String EMPTY_TRANSACTIONS = "Du hast keine Überweisungen in deinem Konto";

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
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        Intent intent = requestEnvelope.getRequest().getIntent();

        sessionID = requestEnvelope.getSession().getSessionId();
        String slotValue = intent.getSlot(SLOT_NAME) != null ? intent.getSlot(SLOT_NAME).getValue() : null;


        SessionStorage sessionStorage = SessionStorage.getInstance();
        Integer furtherTransactionDialogIndex = (Integer) sessionStorage.getObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX);

        if(furtherTransactionDialogIndex != null){
            if(intent.getName().equals("AMAZON.YesIntent")){
                return getNextTransaction(furtherTransactionDialogIndex);
            }

            if(intent.getName().equals("AMAZON.NoIntent")){
                return getResponse(CARD_NAME, "Verstanden!");
            }
        }


        log.info("account information intent - slot: " + slotValue);

        if (slotValue != null) {
            slotValue = slotValue.toLowerCase();

            String slot = "kontostand";
            if (slot.equals(slotValue)) {
                speechText = "Dein Kontostand beträgt <say-as interpret-as=\"unit\">€" + account.getBalance() + "</say-as>\n";
                return getSSMLResponse(CARD_NAME, speechText);
            }

            slot = "kontonummer";
            if (slot.equals(slotValue)) {
                speechText = "Deine " + slot + " lautet " + account.getNumber();
            }

            slot = "iban";
            if (slot.equals(slotValue)) {
                speechText = "Deine " + slot + " lautet " + account.getIban();
            }

            slot = "eröffnungsdatum";
            if (slot.equals(slotValue)) {
                speechText = "Dein " + slot + " war " + account.getOpeningDate();
            }

            slot = "abhebegebühr";
            if (slot.equals(slotValue)) {
                speechText = "Deine " + slot + " beträgt <say-as interpret-as=\"unit\">€" + account.getWithdrawalFee() + "</say-as>\n";
                return getSSMLResponse(CARD_NAME, speechText);
            }

            slot = "zinssatz";
            if (slot.equals(slotValue)) {
                speechText = "Dein " + slot + " ist aktuell " + account.getInterestRate();
            }

            slot = "kreditlimit";
            if (slot.equals(slotValue)) {
                speechText = "Dein " + slot + " beträgt <say-as interpret-as=\"unit\">€" + account.getCreditLimit() + "</say-as>\n";
                return getSSMLResponse(CARD_NAME, speechText);
            }

            slot = "kreditkartenlimit";
            if (slot.equals(slotValue)) {
                speechText = "Dein " + slot + " beträgt <say-as interpret-as=\"unit\">€" + account.getCreditcardLimit() + "</say-as>\n";
                return getSSMLResponse(CARD_NAME, speechText);
            }

            if ("überweisungen".equals(slotValue) || "transaktionen".equals(slotValue)) {
                return handleTransactionSpeech();
            }

            return getResponse(CARD_NAME, speechText);
        } else {
            return getResponse(CARD_NAME, repromptText);
        }
    }

    /**
     * responses each transaction from a account
     * @return SpeechletResponse to alexa
     */
    private SpeechletResponse handleTransactionSpeech() {
        List<Transaction> transactions = getTransactions();

        if (transactions.isEmpty())
            return getResponse(CARD_NAME, EMPTY_TRANSACTIONS);

        // transaction list intro
        StringBuilder stringBuilder = new StringBuilder("Du hast " + transactions.size() + " Transaktionen. ");
        int i;
        for ( i = 0; i < 3; i++) {
            stringBuilder.append("<break time=\"1s\"/> Transaktionsnummer " + (i + 1) + " ");
            stringBuilder.append(getTransactionText(transactions.get(i)));
        }

        if(i - 1 < transactions.size()){
            stringBuilder.append("<break time=\"1s\"/> Möchtest du weitere Transaktionen hören?");
            SessionStorage sessionStorage = SessionStorage.getInstance();
            sessionStorage.putObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX, i);
        }

        return getSSMLAskResponse(CARD_NAME, stringBuilder.toString());
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
        return "Von deinem Konto auf das Konto " + transaction.getDestinationAccount() + " in Höhe von <say-as interpret-as=\"unit\">€" + transaction.getAmount() + "</say-as>\n";
    }

    private String getTransactionToAccountText(Transaction transaction) {
        return "Von " + transaction.getSourceAccount() + " auf dein Konto in Höhe von <say-as interpret-as=\"unit\">€" + transaction.getAmount() + "</say-as>\n";
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
            transactionText = transactionText + "<break time=\"1s\"/> Möchtest du weitere Transaktionen hören?";
            SessionStorage sessionStorage = SessionStorage.getInstance();
            sessionStorage.putObject(sessionID, CONTEXT_FURTHER_TRANSACTION_INDEX, i + 1);
        }

        return getSSMLAskResponse(CARD_NAME, transactionText);
    }

}
