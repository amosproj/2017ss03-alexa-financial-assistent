package amosalexa.dialogsystem;


import amosalexa.SessionStorage;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

public interface DialogHandler {
	String getDialogName();
	SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException;
}
