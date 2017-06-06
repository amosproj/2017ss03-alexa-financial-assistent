package amosalexa.services.bankaccount;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
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
     * Name for custom slot types
     */
    private static final String SLOT_NAME = "AccountInformationSlots";


    /**
     * speech texts
     */
    private static String speechText = "Was möchtest du über dein Konto erfahren?";
    private static final String repromptText = "Was möchtest du über dein Konto erfahren? Frage mich etwas!";

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

        String slotValue = intent.getSlot(SLOT_NAME) != null ? intent.getSlot(SLOT_NAME).getValue() : null;

        log.info("account information intent - slot: " + slotValue);


        if(slotValue != null){
            slotValue = slotValue.toLowerCase();

            String slot = "kontostand";
            if(slot.equals(slotValue)){
                speechText = "Dein Kontostand beträgt <say-as interpret-as=\"unit\">€" + account.getBalance() + "</say-as>\n";
                return getSSMLOutputSpeech( speechText, getSimpleCard(CARD_NAME, slot));
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
                speechText = "Deine "  + slot + " beträgt <say-as interpret-as=\"unit\">€" + account.getWithdrawalFee() + "</say-as>\n" ;
                return getSSMLOutputSpeech( speechText, getSimpleCard(CARD_NAME, slot));
            }

            slot = "zinssatz";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " ist aktuell " + account.getInterestRate();
            }

            slot = "kreditlimit";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " beträgt <say-as interpret-as=\"unit\">€" + account.getCreditLimit() + "</say-as>\n" ;
                return getSSMLOutputSpeech( speechText, getSimpleCard(CARD_NAME, slot));
            }

            slot = "kreditkartenlimit";
            if(slot.equals(slotValue)){
                speechText = "Dein "  + slot + " beträgt <say-as interpret-as=\"unit\">€" + account.getCreditcardLimit() + "</say-as>\n" ;
                return getSSMLOutputSpeech( speechText, getSimpleCard(CARD_NAME, slot));
            }

            return getResponse(CARD_NAME, speechText);

        } else {

            return getResponse(CARD_NAME, repromptText);
        }
    }
}
