package amosalexa.dialogsystem.dialogs;

import amosalexa.SessionStorage;
import amosalexa.dialogsystem.DialogHandler;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;


public class TestListDialog implements DialogHandler {

	private String[] exampleList = new String[] {"Listeneintrag Eins", "Listeneintrag Zwei", "Listeneintrag Drei", "Listeneintrag Vier", "Listeneintrag Fünf"};

	@Override
	public String getDialogName() {
		return "TestList";
	}

	@Override
	public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException {
		String intentName = intent.getName();

		if("TestListIntent".equals(intentName)) {
			return initialList(storage);
		} else if("AMAZON.YesIntent".equals(intentName)) {
			return nextListItem(storage);
		} else if("AMAZON.NoIntent".equals(intentName)) {
			return exitIntent(storage);
		} else {
			throw new SpeechletException("Unhandled intent: " + intentName);
		}
	}

	private SpeechletResponse initialList(SessionStorage.Storage storage) {
		// Return the first three list entries
		String speechText = exampleList[0] + ". " + exampleList[1] + ". " + exampleList[2] + ". Möchtest du noch mehr hören?";

		// Create the plain text output
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		// Create reprompt
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(speech);

		// Save current list offset in this session
		storage.put("TestList.NextEntry", 3);

		return SpeechletResponse.newAskResponse(speech, reprompt);
	}

	private SpeechletResponse nextListItem(SessionStorage.Storage storage) {
		int nextEntry = (int)storage.get("TestList.NextEntry");
		String speechText;

		if(nextEntry < exampleList.length) {
			speechText = exampleList[nextEntry] + ". Möchtest du noch mehr hören?";

			// Save current list offset in this session
			storage.put("TestList.NextEntry", nextEntry + 1);

			// Create the plain text output
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);

			// Create reprompt
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(speech);

			return SpeechletResponse.newAskResponse(speech, reprompt);
		} else {
			speechText = "Das waren alle Einträge.";

			// Create the plain text output
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText(speechText);

			return SpeechletResponse.newTellResponse(speech);
		}
	}

	private SpeechletResponse exitIntent(SessionStorage.Storage storage) {
		String speechText = "Okay, tschüss.";

		// Create the plain text output
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(speechText);

		return SpeechletResponse.newTellResponse(speech);
	}
}
