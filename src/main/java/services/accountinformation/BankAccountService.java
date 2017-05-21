package services.accountinformation;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import model.banking.AccountFactory;
import model.banking.account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.SpeechService;

import java.util.Map;


public class BankAccountService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);

    /**
     * Todo: dummy account number -- Log in is missing
     */
    private static final String number = "0000000001";

    /**
     * Name for custom slot types
     */
    private static final String SLOT_NAME = "BankInformation";

    /**
     * Singleton
     */
    private static BankAccountService bankAccountService = new BankAccountService();


    public static BankAccountService getInstance(){
        return bankAccountService;
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) {

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
