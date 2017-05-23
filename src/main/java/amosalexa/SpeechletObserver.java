package amosalexa;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletResponse;

public interface SpeechletObserver {
    SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope);

    void subscribe(SpeechletSubject speechletSubject);
}
