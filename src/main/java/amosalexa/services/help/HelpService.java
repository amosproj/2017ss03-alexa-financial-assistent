package amosalexa.services.help;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
        onlineBanking.addFeature(new Category.Feature(
                "Überweisen",
                "",
                ""));
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        return null;
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {

    }

    @Override
    public String getDialogName() {
        return HelpService.class.getName();
    }

    @Override
    public List<String> getStartIntents() {
        return null;
    }

    @Override
    public List<String> getHandledIntents() {
        return null;
    }

}
