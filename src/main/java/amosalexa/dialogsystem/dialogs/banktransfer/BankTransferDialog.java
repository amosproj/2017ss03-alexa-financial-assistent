package amosalexa.dialogsystem.dialogs.banktransfer;

import amosalexa.SessionStorage;
import amosalexa.dialogsystem.DialogHandler;
import amosalexa.services.AbstractSpeechService;
import api.banking.AccountAPI;
import api.banking.TransactionAPI;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import model.banking.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BankTransferDialog extends AbstractSpeechService implements DialogHandler {

    private static final String AMOUNT_KEY = "amount";
    private static final String NAME_KEY = "name";
    private static final String IBAN_KEY = "iban";
    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransferDialog.class);

    @Override
    public String getDialogName() {
        return "BankTransfer";
    }

    @Override
    public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException {
        String intentName = intent.getName();
        LOGGER.info("Intent Name: " + intentName);

        if ("BankTransferIntent".equals(intentName)) {
            LOGGER.info("askForBankTransferConfirmation wird aufgerufen.");
            return askForBankTransferConfirmation(intent, storage);
        } else if (YES_INTENT.equals(intentName)) {
            LOGGER.info("Intent Name: " + intentName);
            return proceedBankTransfer(intent, storage);
        } else if (NO_INTENT.equals(intentName)) {
            return cancelAction();
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


    private SpeechletResponse askForBankTransferConfirmation(Intent intent, SessionStorage.Storage storage) {
        Map<String, Slot> slots = intent.getSlots();

        // get amount + name from slots.
        String amount = slots.get("amount").getValue();
        String name = slots.get("name").getValue();
        LOGGER.info("Slot name hat folgenden Wert: " + name);

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

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("Kontakt nicht gefunden");
            card.setContent(speechText);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newTellResponse(speech, card);
        }

        // there is not enough money on the account
        if (enoughMoneyforTransaction("0000000001", Double.valueOf(amount)) == false) {

            String speechText = "Dein Kontostand reicht leider nicht aus, um " + amount + " Euro zu ueberweisen." +
                    " Ich habe die Transaktion daher nicht durchgefuehrt.";

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("Überweisung nicht möglich.");
            card.setContent(speechText);

            // Create the plain text output.
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(speechText);

            return SpeechletResponse.newTellResponse(speech, card);
        }

        // put amount + name in the storage.
        storage.put(AMOUNT_KEY, amount);
        storage.put(NAME_KEY, name);
        storage.put(IBAN_KEY, iban);

        // get account balance
        Account account = AccountAPI.getAccount("0000000001");
        String balanceBeforeTransation = String.valueOf(account.getBalance());
        LOGGER.info("Der aktuelle Kontostand beträgt " + balanceBeforeTransation);

        String speechText = "Aktuell betraegt dein Kontostand " + balanceBeforeTransation + " Euro. " +
                "Bist du sicher, dass du " + amount + " Euro an " + name + " ueberweisen willst?";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("CreditLimit");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Transfers money and returns response with
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse proceedBankTransfer(Intent intent, SessionStorage.Storage storage) {

        // get name + amount
        String amount = (String) storage.get(AMOUNT_KEY);
        String name = (String) storage.get(NAME_KEY);
        String iban = (String) storage.get(IBAN_KEY);
        LOGGER.info("Die IBAN, an die überwiesen wird, lautet: " + iban);

        // FIXME: Hardcoded strings
        Number amountNum = Integer.parseInt(amount);
        TransactionAPI.createTransaction(amountNum, "DE50100000000000000001",iban, "2017-05-16", "Beschreibung", "Hans" , "Helga");

        // get account balance
        Account account = AccountAPI.getAccount("0000000001");
        String balanceAfterTransation = String.valueOf(account.getBalance());

        LOGGER.info("Der aktuelle Kontostand betraegt " + balanceAfterTransation);

        //reply message
        String speechText = "Ok, " + amount + " Euro wurden an " + name + " ueberwiesen." +
                " Dein neuer Kontostand betraegt " + balanceAfterTransation + " Euro." ;

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("CreditLimit");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Checks if there is a enough money in the account to do the transaction.
     *
     * @return boolean equals false if there is not enough money in the account.
     */
    private boolean enoughMoneyforTransaction(String accountNumber, double amountToTransfer) {

        // TODO: get the specific money limit for the account (User Story 36)
        int limitForTransaction = 0;
        double accountBalance = (double) AccountAPI.getAccount(accountNumber).getBalance();

        return accountBalance - amountToTransfer >= limitForTransaction;
    }

    private SpeechletResponse cancelAction() {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("OK, ich fuehre die Ueberweisung nicht aus.");
        return SpeechletResponse.newTellResponse(speech);
    }

}
