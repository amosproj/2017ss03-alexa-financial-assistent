package amosalexa.dialogsystem;

import amosalexa.AmosAlexaSpeechlet;
import amosalexa.SessionStorage;
import amosalexa.dialogsystem.dialogs.ReplacementCardDialog;
import amosalexa.dialogsystem.dialogs.TestListDialog;
import amosalexa.services.bankaccount.banktransfer.BankTransferDialog;
import amosalexa.services.pricequery.PriceQueryService;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * This singleton class handles
 */
public class DialogResponseManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmosAlexaSpeechlet.class);


    private static DialogResponseManager instance;

    private DialogResponseManager() {
        // TODO: Registering new DialogHandlers should happen automatically, not in this class
        registerDialogHandler(new TestListDialog());
        registerDialogHandler(new ReplacementCardDialog());
        registerDialogHandler(new BankTransferDialog());

        // TODO: Story16: Register PriceQuery Service
        registerDialogHandler(new PriceQueryService(amosAlexaSpeechlet));

    }

    public static DialogResponseManager getInstance() {
        if (DialogResponseManager.instance == null) {
            DialogResponseManager.instance = new DialogResponseManager();
        }
        return DialogResponseManager.instance;
    }

    private HashMap<String, DialogHandler> dialogHandlers = new HashMap<>();

    private void registerDialogHandler(DialogHandler dialogHandler) {
        dialogHandlers.put(dialogHandler.getDialogName(), dialogHandler);
    }

    public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) {
        try {
            String dialog = (String) storage.get(SessionStorage.CURRENTDIALOG);

            if (dialog == null) {
                throw new SpeechletException("CURRENTDIALOG not set in Session.");
            }

            DialogHandler handler = dialogHandlers.get(dialog);

            if (handler == null) {
                throw new SpeechletException("No handler for dialogsystem " + dialog + " registered in DialogResponseManager.");
            }

            return handler.handle(intent, storage);
        } catch (SpeechletException e) {
            LOGGER.error(e.getMessage());

            /*PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Ein Fehler ist aufgetreten.");

            return SpeechletResponse.newTellResponse(speech);*/

            // I had to modify this because now more than one Speechlet can subscribe to the YesIntent
            // So we must not abort here.

            return null;
        }
    }
}
