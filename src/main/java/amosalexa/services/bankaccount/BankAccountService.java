package amosalexa.services.bankaccount;

import amosalexa.server.Launcher;
import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import model.banking.AccountFactory;
import model.banking.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;


public class BankAccountService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);

    private static final String BANK_ACCOUNT_INTENT = "AccountInformation";

    /**
     *
     */
    private static final String number = "0000000001";

    /**
     * Name for custom slot types
     */
    private static final String SLOT_NAME = "BankInformation";

    public BankAccountService(SpeechletSubject speechletSubject){
      subscribe(speechletSubject);
    }

    /**
     * ties the Speechlet Subject (Amos Alexa Speechlet) with an Speechlet Observer
     * @param speechletSubject service
     */
    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, BANK_ACCOUNT_INTENT);
    }


    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        Account account = AccountFactory.getInstance().getAccount(number);

        String speechText = "What Information do you need about your bank account?";

        String repromptText = "Say what you want do know about your bank account. For Example: What is my interest rate.";

        Map<String, Slot> slots = request.getIntent().getSlots();

        for (Map.Entry entry : slots.entrySet()) {
            Slot slot = (Slot) entry.getValue();
            log.info(entry.getKey() + ", " + slot.getValue());
        }

        String slotValue = request.getIntent().getSlot(SLOT_NAME).getValue();

        String slot = "balance";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getBalance();
        }

        slot = "number";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getNumber();
        }

        slot = "iban";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getIban();
        }

        slot = "opening date";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getOpeningDate();
        }

        slot = "withdrawal fee";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getWithdrawalFee();
        }

        slot = "interest rate";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getInterestRate();
        }

        slot = "credit limit";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getCreditLimit();
        }

        slot = "credit card limit";
        if(slot.equals(slotValue)){
            speechText = "Your "  + slot + " is " + account.getCreditcardLimit();
        }

        log.warn("I'm here from Observer pattern");

        return getSpeechletResponse(speechText, repromptText);
    }


    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText){
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Bank Information");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }


}
