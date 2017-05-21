package amosalexa.services.bankcontact;


import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.*;

public class BankContactService implements SpeechService{

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        return null;
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {

    }

}
