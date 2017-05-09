/**
 Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

 http://aws.amazon.com/apache2.0/

 or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package pricequery;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pricequery.aws.model.Item;
import pricequery.aws.model.Offer;
import pricequery.aws.request.AWSLookup;
import pricequery.aws.util.AWSUtil;

import java.util.List;

/**
 * This feature lets alexa request product information from amazon
 */
public class PriceQuerySpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(PriceQuerySpeechlet.class);

    private String speechTextWelcome = "Welcome, the price query skill tells you what a product costs on amazon.com.";

    private String repromptTextWelcome = "Welcome, the price query skill tells you what a product costs on amazon.com. Just " +
            "what your are looking for. Like what costs an Iphone";

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),  session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        log.warn(getClass().toString() + " Intent started: " + intentName);

        if ("ProductRequestIntent".equals(intentName)) {
            log.warn(getClass().toString() + " Intent started: " + intentName);
            return getProductInformation(intent, session);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse getProductInformation(Intent intent, Session session) {
        // get keyword for product search
        Slot slot = intent.getSlot("ProductKeyword");
        String keyword = slot.getValue();

        String speechTextStart = "";
        String speechText;

        if (keyword != null) {

            log.warn(getClass().toString() + " Keyword: " + keyword);

            List<Item> items = AWSLookup.itemSearch(keyword, 1, null);

            if(!items.isEmpty()){
                speechTextStart = "Bingo, I found something!";
            }
            String specheTextItems = "";

            for(int i = 0; i < 3; i++){
                Offer offer = AWSLookup.offerLookup(items.get(i).getASIN());
                specheTextItems = specheTextItems + "<break time=\"1.0s\" />  " + AWSUtil.shortTitle(items.get(i).getTitle()) + "for " + offer.getLowestNewPrice() / 100 + " Euro";
            }

            speechText = speechTextStart + specheTextItems;
        } else {
            speechText = speechTextWelcome;
        }


        return getSpeechletResponse(speechText, repromptTextWelcome);
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        return getSpeechletResponse(speechTextWelcome, repromptTextWelcome);
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText){
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Price Query");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }
}
