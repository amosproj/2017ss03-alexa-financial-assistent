package amosalexa.services.editCategories;


import amosalexa.SessionStorage;
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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class EditCategoriesService extends AbstractSpeechService implements SpeechService {

    private static final Logger LOGGER = LoggerFactory.getLogger(amosalexa.services.bankaccount.TransactionService.class);
    private static final String SHOW_CATEGORIES_INTENT = "ShowCategoriesIntent";

    private static final String DELETE_CATEGORY_INTENT = "DeleteCategoryIntent";
    private static final String SERVICE_CARD_TITLE = "Kategorien verwalten";

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

        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = intent.getName();
        LOGGER.info("Intent Name: " + intentName);

        Session session = requestEnvelope.getSession();
        String context = (String) session.getAttribute(DIALOG_CONTEXT);

        switch (intentName) {
            case DELETE_CATEGORY_INTENT:
                return deleteCategory(intent, session);
            case YES_INTENT:
                if (context.equals(DELETE_CATEGORY_INTENT)) {
                    return performDeletion(intent, session);
                }
        }
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


    private SpeechletResponse deleteCategory(Intent intent, Session session) {
        String category = intent.getSlot("CategoryName").getValue();

        if (category == null || category.equals("")) {
            return getResponse(SERVICE_CARD_TITLE, "Ich konnte den Namen der Kategorie nicht verstehen. Bitte versuche es noch einmal.");
        }

        List<Category> categories = DynamoDbClient.instance.getItems(Category.TABLE_NAME, Category::new);

        int closestDist = Integer.MAX_VALUE;
        Category closestCategory = null;

        for (Category categoryIter : categories) {
            int dist = StringUtils.getLevenshteinDistance(category, categoryIter.getName());

            if (dist < closestDist) {
                closestDist = dist;
                closestCategory = categoryIter;
            }
        }

        if (closestDist > 5) {
            return getResponse(SERVICE_CARD_TITLE, "Ich konnte keine passende Kategorie finden. Vielleicht hast du sie ja schon gelöscht, du Held.");
        }

        SessionStorage.getInstance().putObject(session.getSessionId(), SERVICE_CARD_TITLE + ".categoryId", closestCategory);
        session.setAttribute(DIALOG_CONTEXT, DELETE_CATEGORY_INTENT);

        return getAskResponse(SERVICE_CARD_TITLE, "Möchtest du die Kategorie mit dem Namen '"
                + closestCategory.getName()
                + "' und dem Limit von "
                + closestCategory.getLimit()
                + " Euro wirklich löschen?");
    }

    private SpeechletResponse performDeletion(Intent intent, Session session) {
        Object closestCategoryObj = SessionStorage.getInstance().getObject(session.getSessionId(), SERVICE_CARD_TITLE + ".categoryId");

        if (closestCategoryObj == null || !(closestCategoryObj instanceof Category)) {
            return getResponse(SERVICE_CARD_TITLE,
                    "Oh, da ist etwas passiert was nicht hätte passieren dürfen. " +
                            "Vielleicht sind ein paar Elektronen in eine andere Speicherzelle getunnelt. " +
                            "Anders kann ich mir das nicht erklären.");
        }

        Category closestCategory = (Category) closestCategoryObj;

        DynamoDbClient.instance.deleteItem(Category.TABLE_NAME, closestCategory);

        return getResponse(SERVICE_CARD_TITLE, "OK, wie du willst. Ich habe die Kategorie mit dem Namen '" + closestCategory.getName() + "' gelöscht. Hoffentlich bereust du es nicht.");
    }

}
