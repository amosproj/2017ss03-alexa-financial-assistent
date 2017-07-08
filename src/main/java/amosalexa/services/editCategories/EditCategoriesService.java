package amosalexa.services.editCategories;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.aws.DynamoDbClient;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import model.db.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class EditCategoriesService extends AbstractSpeechService implements SpeechService {

    private static final Logger LOGGER = LoggerFactory.getLogger(amosalexa.services.bankaccount.TransactionService.class);
    private static final String SHOW_CATEGORIES_INTENT = "ShowCategoriesIntent";
    private static final String ITEM_KEY = "item";
    private static final String TRANSFER_LIMIT_KEY = "transferLimit";

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                SHOW_CATEGORIES_INTENT

        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                SHOW_CATEGORIES_INTENT,
                YES_INTENT,
                NO_INTENT,
                STOP_INTENT
        );
    }

    public EditCategoriesService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }



    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {

        return null;
    }



    private SpeechletResponse showAllCategories(Intent intent, Session session){

        String intentName = intent.getName();
        LOGGER.info("Intent Name: " + intentName);

        List<Category> items = DynamoDbClient.instance.getItems(Category.TABLE_NAME, Category::new);
        String namesOfCategories = "";

        for (Category item : items) {
            namesOfCategories += item.getName() + ", ";
        }

        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText("Aktuell hast du folgende Kategorien: " + namesOfCategories);

        SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech);

        return response;
    }

    private SpeechletResponse addNewCategory(Intent intent, Session session){

        String intentName = intent.getName();
        LOGGER.info("Intent Name: " + intentName);
        Slot categoryNameSlot = intent.getSlot("CategoryName");

        //List<Category> items = DynamoDbClient.instance.getItems(Category.TABLE_NAME, Category::new);

        Category item = new Category(categoryNameSlot.getValue());
        DynamoDbClient.instance.putItem(Category.TABLE_NAME, item);

        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText("Verstanden. die Kategorie " + categoryNameSlot.getValue() + " wurde erstellt.");

        SpeechletResponse response = SpeechletResponse.newTellResponse(outputSpeech);

        return response;
    }

}
