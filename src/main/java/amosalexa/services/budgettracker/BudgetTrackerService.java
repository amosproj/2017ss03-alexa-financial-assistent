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

    private static final String CATEGORY_LIMIT_SET_INTENT = "CategoryLimitSetIntent";

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                CATEGORY_LIMIT_SET_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                CATEGORY_LIMIT_SET_INTENT,
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
        Session session = requestEnvelope.getSession();
        String context = (String) session.getAttribute(CONTEXT);

        if (CATEGORY_LIMIT_SET_INTENT.equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            session.setAttribute(CONTEXT, CATEGORY_LIMIT_SET_INTENT);
            return askForSetCategoryLimitConfirmation(intent, session);
        } else if (YES_INTENT.equals(intentName) && context.equals(CATEGORY_LIMIT_SET_INTENT)) {
            return setCategoryLimit(intent, session);
        } else if (NO_INTENT.equals(intentName)) {
            //TODO handle No intent!
            return getResponse(BUDGET_TRACKER, "OK, dann nicht. Auf Wiedersehen!");
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse askForSetCategoryLimitConfirmation(Intent intent, Session session) {
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
            return getAskResponse(BUDGET_TRACKER, "Moechtest du das Ausgabelimit von Kategorie " + categorySlot.getValue()
                    + " wirklich auf " + categoryLimitSlot.getValue() + " Euro aendern?");
            //TODO
        }
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
            //TODO
        } else {
            LOGGER.info("Category does not exist yet. Create category...");
            LOGGER.info("Set limit for category " + categoryName);
            category = new Category(categoryName, Double.valueOf(categoryLimit));
            DynamoDbClient.instance.putItem(Category.TABLE_NAME, category);
        }
        return getResponse(BUDGET_TRACKER, "Limit fuer " + categoryName + " wurde gesetzt.");
    }
}