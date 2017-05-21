package services.savings;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import services.SpeechService;

public class SavingsPlanService implements SpeechService {

    /**
     * Singleton
     */
    private static SavingsPlanService savingsPlanService = new SavingsPlanService();

    public static SavingsPlanService getInstance() {
        return savingsPlanService;
    }

    @Override
    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("CreditLimit");
        card.setContent("TEST");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("HALLO");

        return SpeechletResponse.newTellResponse(speech, card);
    }


}
