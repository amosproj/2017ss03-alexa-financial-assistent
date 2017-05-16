package services;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

public interface SpeechService {

    SpeechletResponse onIntent(final IntentRequest request, final Session session) throws SpeechletException;
}
