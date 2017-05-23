package amosalexa.dialogsystem;

import amosalexa.SessionStorage;
import amosalexa.dialogsystem.dialogs.ReplacementCardDialog;
import amosalexa.dialogsystem.dialogs.TestListDialog;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.util.HashMap;

/**
 * This singleton class handles
 *
 */
public class DialogResponseManager {

	private static DialogResponseManager instance;

	private DialogResponseManager() {
		// TODO: Registering new DialogHandlers should happen automatically, not in this class
		registerDialogHandler(new TestListDialog());
		registerDialogHandler(new ReplacementCardDialog());
	}

	public static DialogResponseManager getInstance() {
		if(DialogResponseManager.instance == null) {
			DialogResponseManager.instance = new DialogResponseManager();
		}
		return DialogResponseManager.instance;
	}

	private HashMap<String, DialogHandler> dialogHandlers = new HashMap<>();

	private void registerDialogHandler(DialogHandler dialogHandler) {
		dialogHandlers.put(dialogHandler.getDialogName(), dialogHandler);
	}

	public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException {
		String dialog = (String)storage.get(SessionStorage.CURRENTDIALOG);

		if(dialog == null) {
			throw new SpeechletException("CURRENTDIALOG not set in Session.");
		}

		DialogHandler handler = dialogHandlers.get(dialog);

		if(handler == null) {
			throw new SpeechletException("No handler for dialogsystem " + dialog + " registered in DialogResponseManager.");
		}

		return handler.handle(intent, storage);
	}
}
