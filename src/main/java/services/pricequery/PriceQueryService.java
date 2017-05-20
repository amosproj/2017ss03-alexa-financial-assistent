/**
 Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

 http://aws.amazon.com/apache2.0/

 or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package services.pricequery;

import amosalexa.depot.FinanceApi;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.SpeechService;
import services.pricequery.aws.model.Item;
import services.pricequery.aws.model.Offer;
import services.pricequery.aws.request.AWSLookup;
import services.pricequery.aws.util.AWSUtil;

import java.util.List;

/**
 * This feature lets alexa request product information from amazon
 */
public class PriceQueryService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(PriceQueryService.class);

    private String speechTextWelcome = "Willkommen! Der Preisanfrage-Skill zeigt, was ein Produkt auf Amazon kostet.";

    private String repromptTextWelcome = "Willkommen! Der Preisanfrage-Skill zeigt, was ein Produkt auf Amazon kostet. " +
            "Sag einfach wonach Du suchen willst. Zum Beispiel: Was kostet ein iPhone?";

    private static PriceQueryService priceQueryService = new PriceQueryService();

    public static PriceQueryService getInstance(){
        return priceQueryService;
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
        }

        else if ("MicrosoftStockIntent".equals(intentName)) {
            log.warn(getClass().toString() + " Intent started: " + intentName);
            return getMicrosoftStock(intent, session);
        }

        else if ("AppleStockIntent".equals(intentName)) {
            log.warn(getClass().toString() + " Intent started: " + intentName);
            return getAppleStock(intent, session);
        }

        else if ("TeslaStockIntent".equals(intentName)) {
            log.warn(getClass().toString() + " Intent started: " + intentName);
            return getTeslaStock(intent, session);
        }

        else if ("DepotRequestIntent".equals(intentName)) {
            log.warn(getClass().toString() + " Intent started: " + intentName);
            return getDepotInformation(intent, session);
        }

        else if ("DepotCompositionIntent".equals(intentName)) {
            log.warn(getClass().toString() + " Intent started: " + intentName);
            return getDepotComposition(intent, session);
        }

        else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse getDepotComposition(Intent intent, Session session) {

        String[] stock1 = new String[] {"Apple", "5", "AAPL"};
        String[] stock2 = new String[] {"Tesla", "10", "TSLA"};
        String[] stock3 = new String[] {"Microsoft", "5", "MSFT"};


        return getSpeechletResponse("Du hast folgende Aktien im Depot: "
                + stock1[1] + " Aktien von " + stock1[0] + ", "
                + stock2[1] + " Aktien von " + stock2[0] + " und "
                + stock3[1] + " Aktien von " + stock3[0]
                , repromptTextWelcome);
    }



    private SpeechletResponse getDepotInformation(Intent intent, Session session) {

        String StockTesla = FinanceApi.getStockPrice("TSLA");
        String StockApple = FinanceApi.getStockPrice("AAPL");
        String StockMicrosoft = FinanceApi.getStockPrice("MSFT");

        double numStocksApple = 5;
        double numStocksTesla = 10;
        double numStocksMicrosoft = 10;


        double DoubleDepotWert = Double.parseDouble(StockTesla) * numStocksTesla
                + Double.parseDouble(StockApple) * numStocksApple
                + Double.parseDouble(StockMicrosoft) * numStocksMicrosoft;


        String wertDepot = String.valueOf(DoubleDepotWert);

        return getSpeechletResponse("Der Gesamtwert deines Depots liegt aktuell bei"
                    + wertDepot + "Dollar.", repromptTextWelcome);
    }

    private SpeechletResponse getMicrosoftStock(Intent intent, Session session) {
        return getSpeechletResponse("Der Aktienkurs von Microsoft liegt aktuell bei "
                + FinanceApi.getStockPrice("MSFT") + " Dollor.", repromptTextWelcome);
    }

    private SpeechletResponse getAppleStock(Intent intent, Session session) {
        return getSpeechletResponse("Der Aktienkurs von Apple liegt aktuell bei "
                + FinanceApi.getStockPrice("AAPL") + " Dollar.", repromptTextWelcome);
    }

    private SpeechletResponse getTeslaStock(Intent intent, Session session) {
        return getSpeechletResponse("Der Aktienkurs von Tesla liegt aktuell bei "
                + FinanceApi.getStockPrice("TSLA") + " Dollar.", repromptTextWelcome);
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
                speechTextStart = "Bingo, Ich habe etwas gefunden!";
            }
            String specheTextItems = "";

            for(int i = 0; i < 3; i++){
                Offer offer = AWSLookup.offerLookup(items.get(i).getASIN());
                specheTextItems = specheTextItems + AWSUtil.shortTitle(items.get(i).getTitle()) + "fuer " + offer.getLowestNewPrice() / 100 + " Euro";
            }

            speechText = speechTextStart + specheTextItems;
        } else {
            speechText = speechTextWelcome;
        }


        return getSpeechletResponse(speechText, repromptTextWelcome);
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
