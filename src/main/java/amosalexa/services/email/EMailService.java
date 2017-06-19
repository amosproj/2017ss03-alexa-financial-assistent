package amosalexa.services.email;

import amosalexa.AmosAlexaSpeechlet;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.aws.EMailClient;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.util.Arrays;
import java.util.List;

public class EMailService extends AbstractSpeechService implements SpeechService {

	@Override
	public String getDialogName() {
		return this.getClass().getName();
	}

	@Override
	public List<String> getStartIntents() {
		return Arrays.asList(
				TEST_EMAIL_INTENT
		);
	}

	@Override
	public List<String> getHandledIntents() {
		return Arrays.asList(
				TEST_EMAIL_INTENT
		);
	}

	@Override
	public void subscribe(SpeechletSubject speechletSubject) {
		for(String intent : getHandledIntents()) {
			speechletSubject.attachSpeechletObserver(this, intent);
		}
	}

	public EMailService(SpeechletSubject speechletSubject) {
		subscribe(speechletSubject);
	}

	private static final String TEST_EMAIL_INTENT = "TestEMailIntent";

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
		IntentRequest request = requestEnvelope.getRequest();

		if (request.getIntent().getName().equals(TEST_EMAIL_INTENT)) {
			String answer = "Okay, ich habe dir eine E-Mail gesendet.";
			if(!EMailClient.SendEMail("Test-Mail", "Hello, World!")) {
				answer = "Leider konnte die E-Mail nicht gesendet werden.";
			}
			return AmosAlexaSpeechlet.getSpeechletResponse(answer, "", false);
		}

		return null;
	}

}
