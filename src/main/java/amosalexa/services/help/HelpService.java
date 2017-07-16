package amosalexa.services.help;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.util.*;

/**
 * Help service which provides information on how to use different functionality.
 */
public class HelpService extends AbstractSpeechService implements SpeechService {

    /**
     * Represents a feature category.
     */
    static class Category {

        /**
         * Feature that belongs to a category.
         */
        static class Feature {
            String name;
            String description;
            String example;

            Feature(String name, String description, String example) {
                this.name = name;
                this.description = description;
                this.example = example;
            }

            @Override
            public String toString() {
                return name;
            }
        }

        String name;
        List<Feature> features = new LinkedList<>();

        Category(String name) {
            this.name = name;
        }

        void addFeature(Feature feature) {
            features.add(feature);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    List<Category> categories = new LinkedList<>();

    static {
        Category onlineBanking = new Category("Online-Banking");
        Category smartFinancing = new Category("Online-Banking");

        onlineBanking.addFeature(new Category.Feature(
                "Überweisen",
                "",
                ""));
    }

    /**
     * Default value for cards
     */
    private static final String INTRODUCTION = "AMOS Einführung";

    private static final String INTRODUCTION_INTENT = "IntroductionIntent";

    public HelpService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        IntentRequest request = requestEnvelope.getRequest();
        Intent intent = request.getIntent();
        String intentName = intent.getName();

        switch (intentName) {
            case INTRODUCTION_INTENT:
                return getIntroduction(intent);
        }

        return null;
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public String getDialogName() {
        return HelpService.class.getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                INTRODUCTION_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                INTRODUCTION_INTENT
        );
    }

    private SpeechletResponse getIntroduction(Intent intent) {
        //TODO improve speech by ssml, continue
        String introductionSsml = "Willkommen bei AMOS, der sprechenden Banking-App! Mit mir kannst du deine Bank-Geschäfte" +
                " mit Sprachbefehlen erledigen. Ich möchte dir kurz vorstellen, was ich alles kann. Um eine Übersicht über die" +
                " verfügbaren Kategorien von Funktionen zu erhalten, sage \"Übersicht über Kategorien\". Um mehr über die Funktionen einer " +
                " Kategorie zu erfahren, sage \"Mehr über Kategorie\" und dann den Namen der Kategorie, zum Beispiel \"Mehr über Kategorie Smart-Financing\".";
        return getSSMLResponse(INTRODUCTION, introductionSsml);
    }

    private SpeechletResponse getCategories(Intent intent) {
        StringBuilder sb = new StringBuilder();

        sb.append("Die Funktionen sind in folgende Kategorien gruppiert: ");
        boolean first = true;

        for (Iterator<Category> iter = categories.iterator(); iter.hasNext();) {
            if (first) {
                first = false;
            } else {
                if (iter.hasNext()) {
                    sb.append(", ");
                } else {
                    sb.append(" und ");
                }
            }

            Category category = iter.next();
            sb.append(category.name);
        }

        return getSSMLResponse(INTRODUCTION, sb.toString());
    }

}
