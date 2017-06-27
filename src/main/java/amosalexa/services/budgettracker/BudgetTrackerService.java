package amosalexa.services.budgettracker;

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
import model.db.Category;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Budget tracking service
 */
public class BudgetTrackerService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    private static final String CATEGORY_LIMIT_INFO_INTENT = "CategoryLimitInfoIntent";
    private static final String PLAIN_CATEGORY_INTENT = "PlainCategoryIntent";
    private static final String CATEGORY_LIMIT_SET_INTENT = "CategoryLimitSetIntent";

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                CATEGORY_LIMIT_INFO_INTENT,
                CATEGORY_LIMIT_SET_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                CATEGORY_LIMIT_INFO_INTENT,
                PLAIN_CATEGORY_INTENT,
                CATEGORY_LIMIT_SET_INTENT,
                PLAIN_NUMBER_INTENT,
                PLAIN_EURO_INTENT,
                YES_INTENT,
                NO_INTENT,
                STOP_INTENT
        );
    }

    public BudgetTrackerService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * Default value for cards
     */
    private static final String BUDGET_TRACKER = "Budget-Tracker";
    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetTrackerService.class);

    private static final String CATEGORY = "Category";
    private static final String CATEGORY_LIMIT = "CategoryLimit";

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
        LOGGER.info("Intent name: " + intentName);
        Session session = requestEnvelope.getSession();
        String context = (String) session.getAttribute(DIALOG_CONTEXT);

        if (CATEGORY_LIMIT_INFO_INTENT.equals(intentName)) {
            session.setAttribute(DIALOG_CONTEXT, CATEGORY_LIMIT_INFO_INTENT);
            return getCategoryLimitInfo(intent);
        } else if (PLAIN_CATEGORY_INTENT.equals(intentName)) {
            return getCategoryLimitInfo(intent);
        } else if (CATEGORY_LIMIT_SET_INTENT.equals(intentName)) {
            session.setAttribute(DIALOG_CONTEXT, CATEGORY_LIMIT_SET_INTENT);
            return saveSlotValuesAndAskForConfirmation(intent, session);
        } else if ((PLAIN_NUMBER_INTENT.equals(intentName) || PLAIN_EURO_INTENT.equals(intentName)) && context != null
                && context.equals(CATEGORY_LIMIT_SET_INTENT)) {
            Map<String, Slot> slots = intent.getSlots();
            String newLimit = slots.get(NUMBER_SLOT_KEY) != null ? slots.get(NUMBER_SLOT_KEY).getValue() : null;
            if (newLimit == null) {
                return getAskResponse(BUDGET_TRACKER, "Das habe ich nicht ganz verstanden. Bitte wiederhole deine Eingabe.");
            }
            session.setAttribute(CATEGORY_LIMIT, newLimit);
            String categoryName = (String) session.getAttribute(CATEGORY);
            return askForConfirmation(categoryName, newLimit);
        } else if (YES_INTENT.equals(intentName) && context != null && context.equals(CATEGORY_LIMIT_SET_INTENT)) {
            return setCategoryLimit(intent, session);
        } else if (NO_INTENT.equals(intentName) && context != null && context.equals(CATEGORY_LIMIT_SET_INTENT)) {
            return askForCorrection(session);
        } else if (STOP_INTENT.equals(intentName) && context != null && context.equals(CATEGORY_LIMIT_SET_INTENT)) {
            return getResponse("Stop", null);
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse getCategoryLimitInfo(Intent intent) {
        Map<String, Slot> slots = intent.getSlots();
        Slot categorySlot = slots.get(CATEGORY);
        LOGGER.info("Category: " + categorySlot.getValue());

        List<Category> categories = DynamoDbClient.instance.getItems(Category.TABLE_NAME, Category::new);
        LOGGER.info("All categories: " + categories);
        Category category = null;

        for (Category cat : categories) {
            if (cat.getName().equals(categorySlot.getValue())) {
                category = cat;
            }
        }
        if (category != null) {
            return getResponse(BUDGET_TRACKER, "Das Limit fuer die Kategorie " + categorySlot.getValue() + " liegt bei" +
                    Double.valueOf(category.getLimit()) + " Euro.");
        } else {
            return getAskResponse(BUDGET_TRACKER, "Es gibt keine Kategorie mit diesem Namen. Waehle eine andere Kategorie oder" +
                    " erhalte eine Info zu den verfuegbaren Kategorien.");
        }
    }

    private SpeechletResponse saveSlotValuesAndAskForConfirmation(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();
        Slot categorySlot = slots.get(CATEGORY);
        Slot categoryLimitSlot = slots.get(CATEGORY_LIMIT);
        LOGGER.info("Category: " + categorySlot.getValue());
        LOGGER.info("CategoryLimit: " + categoryLimitSlot.getValue());

        if (categorySlot == null || categoryLimitSlot == null ||
                StringUtils.isBlank(categorySlot.getValue()) ||
                StringUtils.isBlank(categoryLimitSlot.getValue())) {
            return getAskResponse(BUDGET_TRACKER, "Das habe ich nicht ganz verstanden, bitte wiederhole deine Eingabe.");
        } else {
            session.setAttribute(CATEGORY, categorySlot.getValue());
            session.setAttribute(CATEGORY_LIMIT, categoryLimitSlot.getValue());
            return askForConfirmation(categorySlot.getValue(), categoryLimitSlot.getValue());
        }
    }

    private SpeechletResponse askForConfirmation(String category, String categoryLimit) {
        return getAskResponse(BUDGET_TRACKER, "Moechtest du das Ausgabelimit fuer die Kategorie " + category
                + " wirklich auf " + categoryLimit + " Euro setzen?");
    }

    private SpeechletResponse askForCorrection(Session session) {
        String categoryName = (String) session.getAttribute(CATEGORY);
        String response = "<emphasis level=\"reduced\">Nenne den Betrag, auf den du das Limit fuer Kategorie " + categoryName +
                " stattdessen setzen willst oder beginne mit einer neuen Eingabe.</emphasis>";
        return getSSMLAskResponse(BUDGET_TRACKER, response);
    }

    private SpeechletResponse setCategoryLimit(Intent intent, Session session) {
        String categoryName = (String) session.getAttribute(CATEGORY);
        String categoryLimit = (String) session.getAttribute(CATEGORY_LIMIT);

        List<Category> categories = DynamoDbClient.instance.getItems(Category.TABLE_NAME, Category::new);
        LOGGER.info("Categories: " + categories);
        Category category = null;

        for (Category cat : categories) {
            if (cat.getName().equals(categoryName)) {
                category = cat;
            }
        }
        if (category != null) {
            LOGGER.info("Category limit before: " + category.getLimit());
            LOGGER.info("Set limit for category " + categoryName);
            if (category.getLimit() != Double.valueOf(categoryLimit)) {
                DynamoDbClient.instance.deleteItem(Category.TABLE_NAME, category);
                category = new Category(categoryName, Double.valueOf(categoryLimit));
                DynamoDbClient.instance.putItem(Category.TABLE_NAME, category);
            }
            LOGGER.info("Category limit afterwards: " + category.getLimit());
        } else {
            LOGGER.info("Category does not exist yet. Create category...");
            LOGGER.info("Set limit for category " + categoryName + "to value: " + categoryLimit);
            category = new Category(categoryName, Double.valueOf(categoryLimit));
            DynamoDbClient.instance.putItem(Category.TABLE_NAME, category);
        }
        return getResponse(BUDGET_TRACKER, "Limit fuer " + categoryName + " wurde gesetzt.");
    }
}