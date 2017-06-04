package amosalexa.dialogsystem.dialogs.banktransfer;

import amosalexa.ApiHelper;
import amosalexa.SessionStorage;
import amosalexa.dialogsystem.DialogHandler;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import model.banking.AccountFactory;
import model.banking.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class BankTransferDialog implements DialogHandler{

    private static final String AMOUNT_KEY = "amount";
    private static final String NAME_KEY = "name";
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
        } else if ("AMAZON.YesIntent".equals(intentName)) {
            LOGGER.info("Intent Name: " + intentName);
            return proceedBankTransfer(intent, storage);
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            return cancelAction();
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }


    private SpeechletResponse askForBankTransferConfirmation(Intent intent, SessionStorage.Storage storage) {
        Map<String, Slot> slots = intent.getSlots();

        Slot amountSlot = slots.get("amount");
        Slot nameSlot = slots.get("name");

        String amount = amountSlot.getValue();
        String name = nameSlot.getValue();

        storage.put(AMOUNT_KEY, amount);
        storage.put(NAME_KEY, name);

        // get account balance
        Account account = AccountFactory.getInstance().getAccount("0000000001");
        String balanceBeforeTransation = String.valueOf(account.getBalance());
        LOGGER.info("Der aktuelle Kontostand beträgt " + balanceBeforeTransation);

        String speechText = "Aktuell beträgt dein Kontostand " + balanceBeforeTransation + " Euro. " +
                "Bist du sicher, dass du " + amount + " Euro an " + name + " überweisen willst?";

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

        //transferring money
        String url = "http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/transactions";
        String urlParams = "{\n" +
                "  \"amount\" : " + amount + ",\n" +
                "  \"sourceAccount\" : \"DE50100000000000000001\",\n" +
                "  \"destinationAccount\" : \"DE60643995205405578292\",\n" +
                "  \"valueDate\" : \"2017-05-16\",\n" +
                "  \"description\" : \"Beschreibung\"\n" +
                "}";
        ApiHelper.sendPost(url, urlParams);

        // get account balance
        Account account = AccountFactory.getInstance().getAccount("0000000001");
        String balanceAfterTransation = String.valueOf(account.getBalance());

        LOGGER.info("Der aktuelle Kontostand beträgt " + balanceAfterTransation);

        //reply message
        String speechText = "Ok, " + amount + " Euro wurden an " + name + " überwiesen." +
                " Dein neuer Kontostand beträgt " + balanceAfterTransation + " Euro." ;

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

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private SpeechletResponse cancelAction() {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("OK tschuess!");
        return SpeechletResponse.newTellResponse(speech);
    }

}
