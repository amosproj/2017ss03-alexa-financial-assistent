package amosalexa.services.bankaccount;

import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import model.banking.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Map;


public class BankAccountService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);

    private static final String BANK_ACCOUNT_INTENT = "AccountInformation";

    private static final String CARD_NAME = "Konto Information";

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

        Intent intent = requestEnvelope.getRequest().getIntent();

        Account account = AccountAPI.getAccount(number);

        String speechText = "Was möchtest du über dein Konto erfahren?";

        String repromptText = "Was möchtest du über dein Konto erfahren? Frage mich etwas!";


        String slotValue = intent.getSlot(SLOT_NAME) != null ? intent.getSlot(SLOT_NAME).getValue() : null;

        log.info("account information intent - slot: " + slotValue);

        if(slotValue != null){
            slotValue = slotValue.toLowerCase();

            String slot = "kontostand";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " beträgt " + account.getBalance();
            }

            slot = "kontonummer";
            if(slot.equals(slotValue)){
                speechText = "Deine "  + slot + " lautet " + account.getNumber();
            }

            slot = "iban";
            if(slot.equals(slotValue)){
                speechText = "Deine "  + slot + " lautet " + account.getIban();
            }

            slot = "eröffnungsdatum";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " war " + account.getOpeningDate();
            }

            slot = "abhebegebühr";
            if(slot.equals(slotValue)){
                speechText = "Deine "  + slot + " beträgt " + account.getWithdrawalFee();
            }

            slot = "zinssatz";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " ist aktuell " + account.getInterestRate();
            }

            slot = "kreditlimit";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " ist " + account.getCreditLimit();
            }

            slot = "kreditkartenlimit";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " beträgt " + account.getCreditcardLimit();
            }

            return getSpeechletResponse(speechText);

        } else {
            return getSpeechletResponse(repromptText);
        }
    }


    private SpeechletResponse getSpeechletResponse(String speechText){

        SimpleCard card = new SimpleCard();
        card.setTitle(CARD_NAME);
        card.setContent(speechText);

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }
}
