package amosalexa.services.transfertemplates;

import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

public class TransferTemplateService implements SpeechService {

    public TransferTemplateService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        String intentName = request.getIntent().getName();

        if ("AMAZON.YesIntent".equals(intentName)) {

        } else if ("AMAZON.NoIntent".equals(intentName)) {

        } else if  ("ListTransferTemplatesIntent".equals(intentName)) {

        } else if ("DeleteTransferTemplatesIntent".equals(intentName)) {

        } else if ("EditTransferTemplateIntent".equals(intentName)) {

        } else if ("DeleteTransferTemplateIntent".equals(intentName)) {

        }

        return null;
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
        speechletSubject.attachSpeechletObserver(this, "ListTransferTemplatesIntent");
        speechletSubject.attachSpeechletObserver(this, "DeleteTransferTemplatesIntent");
        speechletSubject.attachSpeechletObserver(this, "EditTransferTemplateIntent");
        speechletSubject.attachSpeechletObserver(this, "DeleteTransferTemplateIntent");
    }
}
