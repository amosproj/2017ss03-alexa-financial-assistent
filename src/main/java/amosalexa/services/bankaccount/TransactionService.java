package amosalexa.services.bankaccount;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import api.banking.TransactionAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import model.banking.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TransactionService extends AbstractSpeechService implements SpeechService {

    /**
     * Default value for cards
     */
    private static final String TRANSACTION = "Überweisung";

    //TODO When we use log in system this should be replaced by the account the user has connected with the Alexa skill
    private static final String SOURCE_ACCOUNT_NUMBER = "0000000001";
    private static final String SOURCE_ACCOUNT_IBAN = "DE50100000000000000001";

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                BANK_TRANSFER_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                BANK_TRANSFER_INTENT,
                YES_INTENT,
                NO_INTENT,
                STOP_INTENT,
                PLAIN_NUMBER_INTENT
        );
    }

    public TransactionService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
    private static final String BANK_TRANSFER_INTENT = "BankTransferIntent";
    private static final String NUMBER_KEY = "Number";
    private static final String AMOUNT_KEY = "Amount";
    private static final String NAME_KEY = "Name";
    private static final String IBAN_KEY = "iban";

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
        LOGGER.info("Intent Name: " + intentName);
        Session session = requestEnvelope.getSession();
        String context = (String) session.getAttribute(CONTEXT);

        if (BANK_TRANSFER_INTENT.equals(intentName)) {
            session.setAttribute(CONTEXT, BANK_TRANSFER_INTENT);
            return saveSlotValuesAndAskForConfirmation(intent, session);
        } else if (context != null && context.equals(BANK_TRANSFER_INTENT) && YES_INTENT.equals(intentName)) {
            return proceedBankTransfer(session);
        } else if (context != null && context.equals(BANK_TRANSFER_INTENT) && NO_INTENT.equals(intentName)) {
            return askForBankTransferCorrection(intent, session);
        } else if (context != null && context.equals(BANK_TRANSFER_INTENT) && STOP_INTENT.equals(intentName)) {
            return getResponse("Stop", null);
        } else if (context != null && context.equals(BANK_TRANSFER_INTENT) && PLAIN_NUMBER_INTENT.equals(intentName)) {
            Map<String, Slot> slots = intent.getSlots();
            String newAmount = slots.get(NUMBER_KEY) != null ? slots.get(NUMBER_KEY).getValue() : null;
            if (newAmount == null) {
                return getAskResponse(TRANSACTION, "Das habe ich nicht ganz verstanden. Bitte wiederhole deine Eingabe.");
            }
            session.setAttribute(AMOUNT_KEY, newAmount);
            String name = (String) session.getAttribute(NAME_KEY);
            return askForBankTransferConfirmation(newAmount, name);
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    /**
     * Saves the slot values "name" and "amount" in the session storage and asks for confirmation.
     * Does NOT transfer money yet. If contact not found or not enough in account in declines the transaction.
     *
     * @return SpeechletResponse, that asks for confirmation.
     */
    private SpeechletResponse saveSlotValuesAndAskForConfirmation(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();

        // get amount + name from slots.
        String amount = slots.get(AMOUNT_KEY) != null ? slots.get(AMOUNT_KEY).getValue() : null;
        String name = slots.get(NAME_KEY) != null ? slots.get(NAME_KEY).getValue() : null;
        LOGGER.info("Amount: " + amount);
        LOGGER.info("Name: " + name);

        // TODO: as soon as API also contains name this should be deleted / adjusted
        // create bank accounts
        BankAccount anneBankAccount = new BankAccount("anne", "0000000001", "DE50100000000000000001");
        BankAccount christianBankAccount = new BankAccount("christian", "0000000000", "DE60643995205405578292");
        BankAccount[] allBankAccounts = {anneBankAccount, christianBankAccount};
        String iban = "";

        // check if person is in the contact list
        for (int i = 0; i < allBankAccounts.length; i++) {
            if (allBankAccounts[i].getNamePerson().equals(name)) {
                iban = allBankAccounts[i].getIban();
            }
        }

        // the name was not found in list of contacts
        if (iban.equals("")) {
            String speechText = "Ich habe " + name + " leider nicht in der Liste deiner Kontakte finden können.";
            return getResponse("Kontakt nicht gefunden", speechText);
        }

        // put amount + name + iban in the session.
        session.setAttribute(AMOUNT_KEY, amount);
        session.setAttribute(NAME_KEY, name);
        session.setAttribute(IBAN_KEY, iban);

        return askForBankTransferConfirmation(amount, name);
    }

    private SpeechletResponse askForBankTransferConfirmation(String amount, String name) {
        // there is not enough money on the account
        if (!enoughMoneyForTransaction(SOURCE_ACCOUNT_NUMBER, Double.valueOf(amount))) {

            String speechText = "Dein Kontostand reicht leider nicht aus, um " + amount + " Euro zu ueberweisen." +
                    " Ich habe die Transaktion daher nicht durchgefuehrt.";
            return getResponse("Überweisung nicht möglich", speechText);
        }

        // get account balance
        Account account = AccountAPI.getAccount(SOURCE_ACCOUNT_NUMBER);
        String balanceBeforeTransation = String.valueOf(account.getBalance());
        LOGGER.info("Der aktuelle Kontostand beträgt " + balanceBeforeTransation);

        String speechText = "Aktuell betraegt dein Kontostand " + balanceBeforeTransation + " Euro. " +
                "Bist du sicher, dass du " + amount + " Euro an " + name + " ueberweisen willst?";

        return getAskResponse(TRANSACTION, speechText);
    }

    private SpeechletResponse askForBankTransferCorrection(Intent intent, Session session) {
        return getAskResponse(TRANSACTION, "Nenne den Betrag, den du stattdessen überweisen willst oder breche die Ueberweisung ab," +
                " indem du \"STOP\" sagst.");
    }

    /**
     * Transfers money and returns response with
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse proceedBankTransfer(Session session) {

        // get name + amount
        String amount = (String) session.getAttribute(AMOUNT_KEY);
        String name = (String) session.getAttribute(NAME_KEY);
        String iban = (String) session.getAttribute(IBAN_KEY);
        LOGGER.info("Die IBAN, an die überwiesen wird, lautet: " + iban);

        // FIXME: Hardcoded strings
        // TODO replace payee with the name from account holder
        Number amountNum = Integer.parseInt(amount);
        TransactionAPI.createTransaction(amountNum, SOURCE_ACCOUNT_IBAN, iban, "2017-05-16",
                "Beschreibung", "Hans", null);

        // get account balance
        Account account = AccountAPI.getAccount(SOURCE_ACCOUNT_NUMBER);
        String balanceAfterTransation = String.valueOf(account.getBalance());

        LOGGER.info("Der aktuelle Kontostand betraegt " + balanceAfterTransation);

        //reply message
        String speechText = "Ok, " + amount + " Euro wurden an " + name + " ueberwiesen." +
                " Dein neuer Kontostand betraegt " + balanceAfterTransation + " Euro.";

        return getResponse(TRANSACTION, speechText);
    }

    /**
     * Checks if there is enough money in the account to do the transaction.
     *
     * @return boolean equals false if there is not enough money in the account.
     */
    private boolean enoughMoneyForTransaction(String accountNumber, double amountToTransfer) {
        // TODO: get the specific money limit for the account (User Story 36)
        int limitForTransaction = 0;
        double accountBalance = (double) AccountAPI.getAccount(accountNumber).getBalance();
        return accountBalance - amountToTransfer >= limitForTransaction;
    }
}