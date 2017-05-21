package services.savings;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import services.SpeechService;

public class SavingsPlan implements SpeechService {

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        return null;
    }
}
