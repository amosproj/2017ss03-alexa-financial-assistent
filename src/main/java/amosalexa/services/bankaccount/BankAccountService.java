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
    private static final String SLOT_NAME = "AccountInformationSlots";

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


        String slot = "Kontostand";
        if(slot.equals(slotValue)){
            speechText = "Dein "  + slot + " beträgt " + account.getBalance();
        }

        slot = "Kontonummer";
        if(slot.equals(slotValue)){
            speechText = "Deine "  + slot + " lautet " + account.getNumber();
        }

        slot = "IBAN";
        if(slot.equals(slotValue)){
            speechText = "Deine "  + slot + " lautet " + account.getIban();
        }

        slot = "Eröffnungsdatum";
        if(slot.equals(slotValue)){
            speechText = "Dein "  + slot + " war " + account.getOpeningDate();
        }

        slot = "Abhebegebühr";
        if(slot.equals(slotValue)){
            speechText = "Deine "  + slot + " beträgt " + account.getWithdrawalFee();
        }

        slot = "Zinssatz";
        if(slot.equals(slotValue)){
            speechText = "Dein "  + slot + " ist aktuell " + account.getInterestRate();
        }

        slot = "Kreditlimit";
        if(slot.equals(slotValue)){
            speechText = "Dein "  + slot + " ist " + account.getCreditLimit();
        }

        slot = "Kreditkartenlimit";
        if(slot.equals(slotValue)){
            speechText = "Dein "  + slot + " beträgt " + account.getCreditcardLimit();
        }

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
