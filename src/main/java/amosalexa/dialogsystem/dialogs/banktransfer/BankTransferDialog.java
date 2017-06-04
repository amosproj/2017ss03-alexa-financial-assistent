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
    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransferDialog.class);

    @Override
    public String getDialogName() {
        return "bankTransfer";
    }

    @Override
    public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException {
        String intentName = intent.getName();
        LOGGER.info("Intent Name: " + intentName);

        if ("bankTransfer".equals(intentName)) {
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

        // get account balance
        Account account = AccountFactory.getInstance().getAccount("00000001");

        String speechText = "Bist du sicher, dass du " + amount + " Euro an " + name + " überweisen willst?";

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
        Map<String, Slot> slots = intent.getSlots();
        Slot amountSlot = slots.get("amount");
        Slot nameSlot = slots.get("name");

        //LOGGER.info("Confirmation slot: " + slots.get("confirmation").getValue());

        // String amount = amountSlot.getValue();
        //String name = nameSlot.getValue();
        String name = "test";
        String amount = "test";

        //getting response regarding account balance
        //this.getAccountBalanceResponse();

        //transfering money
        String url = "http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/transactions";
        String urlParams = "{\n" +
                "  \"amount\" : " + amount + ",\n" +
                "  \"sourceAccount\" : \"DE23100000001234567890\",\n" +
                "  \"destinationAccount\" : \"DE60643995205405578292\",\n" +
                "  \"valueDate\" : \"2017-05-16\",\n" +
                "  \"description\" : \"Beschreibung\"\n" +
                "}";
        ApiHelper.sendPost(url, urlParams);

        //reply message
        String speechText = "Ok, " + amount + " Euro wurden an " + name + " überwiesen.";

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
