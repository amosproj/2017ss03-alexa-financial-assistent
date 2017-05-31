/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 * <p>
 * http://aws.amazon.com/apache2.0/
 * <p>
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package amosalexa.services.pricequery;

import amosalexa.SpeechletSubject;
import amosalexa.depot.FinanceApi;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import amosalexa.services.SpeechService;
import amosalexa.services.pricequery.aws.model.Item;
import amosalexa.services.pricequery.aws.model.Offer;
import amosalexa.services.pricequery.aws.request.AWSLookup;
import amosalexa.services.pricequery.aws.util.AWSUtil;

import java.util.List;

/**
 * This feature lets alexa request product information from amazon
 */
public class PriceQueryService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(PriceQueryService.class);

    private static final String PRICE_QUERY_INTENT = "ProductRequestIntent";

    private String speechTextWelcome = "Willkommen! Der Preisanfrage-Skill zeigt, was ein Produkt auf Amazon kostet.";

    private String repromptTextWelcome = "Willkommen! Der Preisanfrage-Skill zeigt, was ein Produkt auf Amazon kostet. " +
            "Sag einfach wonach Du suchen willst. Zum Beispiel: Was kostet ein iPhone?";


    public PriceQueryService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }


    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, PRICE_QUERY_INTENT);
    }


    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        log.warn(getClass().toString() + " Intent started: " + intentName);

        if ("ProductRequestIntent".equals(intentName)) {
            log.warn(getClass().toString() + " Intent started: " + intentName);
            return getProductInformation(intent, session);
        }

        return null;
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

            if (!items.isEmpty()) {
                speechTextStart = "Bingo, Ich habe etwas gefunden!";
            }
            String specheTextItems = "";

            for (int i = 0; i < 3; i++) {
                Offer offer = AWSLookup.offerLookup(items.get(i).getASIN());
                specheTextItems = specheTextItems + AWSUtil.shortTitle(items.get(i).getTitle()) + "fuer " + offer.getLowestNewPrice() / 100 + " Euro";
            }

            speechText = speechTextStart + specheTextItems;
        } else {
            speechText = speechTextWelcome;
        }


        return getSpeechletResponse(speechText, repromptTextWelcome);
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText) {
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
