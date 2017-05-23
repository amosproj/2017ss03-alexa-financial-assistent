/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 * <p>
 * http://aws.amazon.com/apache2.0/
 * <p>
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package amosalexa;

import amosalexa.depot.DummyDepot;
import amosalexa.dialogsystem.DialogResponseManager;
import amosalexa.services.bankaccount.BankAccountService;
import amosalexa.services.bankcontact.BankContactService;
import amosalexa.services.blockcard.BlockCardService;
import amosalexa.services.pricequery.PriceQueryService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.banking.account.StandingOrder;
import model.banking.account.StandingOrderResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class AmosAlexaSpeechlet implements SpeechletSubject {

    private static final Logger logger = LoggerFactory.getLogger(AmosAlexaSpeechlet.class);

    private Map<String, List<SpeechletObserver>> speechServiceObservers = new HashMap<>();

    private static AmosAlexaSpeechlet amosAlexaSpeechlet = new AmosAlexaSpeechlet();

    public static AmosAlexaSpeechlet getInstance(){

        new BankAccountService(amosAlexaSpeechlet);
        new PriceQueryService(amosAlexaSpeechlet);
        new BankContactService(amosAlexaSpeechlet);
        new BlockCardService(amosAlexaSpeechlet);

        return amosAlexaSpeechlet;
    }

    /**
     * attach a speechlet observer - observer will be notified if the intent name matches the key
     * @param speechletObserver
     * @param intentName
     */
    @Override
    public void attachSpeechletObserver(SpeechletObserver speechletObserver, String intentName){
        List<SpeechletObserver> list = speechServiceObservers.get(intentName);

        if (list == null) {
            list = new LinkedList<>();
            speechServiceObservers.put(intentName, list);
        }

        list.add(speechletObserver);
    }

    /**
     * notifies the speechlet observer by the requested intent name - invokes method of observer
     * @param requestEnvelope request from amazon
     * @return SpeechletResponse
     */
    @Override
    public SpeechletResponse notifyOnIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope){
        List<SpeechletObserver> list = speechServiceObservers.get(requestEnvelope.getRequest().getIntent().getName());

        if (list == null) {
            return null;
        }

        for (SpeechletObserver speechService : list) {
            SpeechletResponse response = speechService.onIntent(requestEnvelope);

            if (response != null) {
                return response;
            }
        }

        return null;
    }


    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        logger.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        logger.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();


        logger.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : "";

        SessionStorage.Storage sessionStorage = SessionStorage.getInstance().getStorage(session.getSessionId());

        if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else if ("GetAccountBalance".equals(intentName)) {
            return getAccountBalanceResponse();
        } else if ("checkCreditLimit".equals(intentName)) {
            return getCreditLimitResponse();
        } else if ("StandingOrdersInfoIntent".equals(intentName)) {
            return getStandingOrdersInfoResponse(intent.getSlots());
        } else if ("StandingOrdersDeleteIntent".equals(intentName)) {
            return getStandingOrdersDeleteResponse(intent.getSlots());
        } else if ("StandingOrdersModifyIntent".equals(intentName)) {
            return getStandingOrdersModifyResponse(intent.getSlots());
        } else if ("TestListIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "TestList"); // Set CURRENTDIALOG to start the TestList dialog
            try {
                return DialogResponseManager.getInstance().handle(intentName, sessionStorage); // Let the DialogHandler handle this intent
            } catch (SpeechletException e) {
                e.printStackTrace();
            }
        } else if ("BlockBankCardIntent".equals(intentName)) {

        } else if ("MicrosoftStockIntent".equals(intentName)) {
            return DummyDepot.getMicrosoftStock(intent, session);
        } else if ("AppleStockIntent".equals(intentName)) {
            return DummyDepot.getAppleStock(intent, session);
        } else if ("TeslaStockIntent".equals(intentName)) {
            return DummyDepot.getTeslaStock(intent, session);
        } else if ("DepotRequestIntent".equals(intentName)) {
            return DummyDepot.getDepotInformation(intent, session);
        } else if ("DepotCompositionIntent".equals(intentName)) {
            return DummyDepot.getDepotComposition(intent, session);
        } else if ("AMAZON.YesIntent".equals(intentName)) {
            try {
                return DialogResponseManager.getInstance().handle(intentName, sessionStorage); // Let the DialogHandler handle this intent
            } catch (SpeechletException e) {
                e.printStackTrace();
            }
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            try {
                return DialogResponseManager.getInstance().handle(intentName, sessionStorage); // Let the DialogHandler handle this intent
            } catch (SpeechletException e) {
                e.printStackTrace();
            }
        }

        return notifyOnIntent(requestEnvelope);
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        logger.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }


    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to the Alexa Skills Kit, you can say hello";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse() {
        String speechText = "You can say hello to me!";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with the current account balance.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getAccountBalanceResponse() {

        // This is just a dummy account balance. Will be replaced by an API.
        // TODO: Implement GetAccountBalance with real data.

        double accountBalance = 47.11;
        String speechText = "Your account balance is " + Double.toString(accountBalance);

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("AccountBalance");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with the current account balance.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getCreditLimitResponse() {
        double creditLimit = 2000.91;

        String speechText = "Your credit limit is " + Double.toString(creditLimit);

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("CreditLimit");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the standing orders intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getStandingOrdersInfoResponse(Map<String, Slot> slots) {
        logger.info("StandingOrdersResponse called.");

        ObjectMapper mapper = new ObjectMapper();
        ApiHelper helper = new ApiHelper();
        String test = null;
        try {
            test = helper.sendGet("http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/accounts/9999999999/standingorders");
        } catch (Exception e) {
            //TODO
        }

        StandingOrderResponse standingOrderResponse = null;
        try {
            standingOrderResponse = mapper.readValue(test, StandingOrderResponse.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Daueraufträge");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        if (standingOrderResponse == null || standingOrderResponse.get_embedded() == null) {
            card.setContent("Keine Daueraufträge vorhanden.");
            speech.setText("Keine Dauerauftraege vorhanden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        StandingOrder[] standingOrders = standingOrderResponse.get_embedded().getStandingOrders();

        // Check if user requested to have their stranding orders sent to their email address
        Slot channelSlot = slots.get("Channel");
        boolean sendPerEmail = channelSlot != null &&
                channelSlot.getValue() != null &&
                channelSlot.getValue().equals("email");

        StringBuilder textBuilder = new StringBuilder();

        if (sendPerEmail) {
            // TODO: Send standing orders to user's email address

            textBuilder.append("Ich habe")
                    .append(standingOrders.length)
                    .append(" an deine E-Mail-Adresse gesendet.");
        } else {
            // We want to directly return standing orders here

            Slot payeeSlot = slots.get("Payee");
            String payee = payeeSlot.getValue();

            if (payee != null) {
                // User specified a recipient

                List<StandingOrder> orders = new LinkedList<>();

                // Find closest standing orders that could match the request.
                for (int i = 0; i < standingOrders.length; i++) {
                    if (StringUtils.getLevenshteinDistance(payee, standingOrders[i].getPayee()) <=
                            standingOrders[i].getPayee().length() / 3) {
                        orders.add(standingOrders[i]);
                    }
                }

                textBuilder.append("Es wurden ")
                        .append(orders.size())
                        .append(" Daueraufträge gefunden.");

                int i = 1;
                for (StandingOrder order : orders) {
                    textBuilder.append(' ');

                    textBuilder.append("Dauerauftrag Nummer ")
                            .append(i)
                            .append(": ");

                    textBuilder.append("Überweise ").append(order.getExecutionRateString())
                            .append(order.getAmount())
                            .append(" Euro an ")
                            .append(order.getPayee())
                            .append(".");

                    i++;
                }
            } else {
                // Just return all standing orders

                textBuilder.append("Sie haben momentan ")
                        .append(standingOrders.length)
                        .append(" Daueraufträge.");

                for (int i = 0; i < standingOrders.length; i++) {
                    textBuilder.append(' ');

                    textBuilder.append("Dauerauftrag Nummer ")
                            .append(i + 1)
                            .append(": ");

                    textBuilder.append("Überweise ").append(standingOrders[i].getExecutionRateString())
                            .append(standingOrders[i].getAmount())
                            .append(" Euro an ")
                            .append(standingOrders[i].getPayee())
                            .append(".");
                }
            }
        }

        String text = textBuilder.toString();

        card.setContent(text);
        speech.setText(text);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getStandingOrdersDeleteResponse(Map<String, Slot> slots) {
        logger.info("StandingOrdersDeleteResponse called.");

        Slot numberSlot = slots.get("Number");
        logger.info("NumberSlot: " + numberSlot.getValue());

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Lösche Dauerauftrag");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        ApiHelper helper = new ApiHelper();
        try {
            helper.sendDelete("http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/accounts/9999999999/standingorders/" + numberSlot.getValue());
        } catch (Exception e) {
            logger.error(e.getMessage());
            card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde gelöscht.");
        speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geloescht.");
        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getStandingOrdersModifyResponse(Map<String, Slot> slots) {
        logger.info("StandingOrdersModifyResponse called.");

        Slot numberSlot = slots.get("Number");
        logger.info("NumberSlot: " + numberSlot.getValue());

        Slot amountSlot = slots.get("Amount");
        logger.info("AmountSlot: " + amountSlot.getValue());

        Slot executionRateSlot = slots.get("ExecutionRate");
        logger.info("ExecutionRateSlot: " + executionRateSlot.getValue());

        Slot firstExecutionSlot = slots.get("FirstExecution");
        logger.info("FirstExecutionSlot: " + firstExecutionSlot.getValue());

        ObjectMapper mapper = new ObjectMapper();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Ändere Dauerauftrag");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        ApiHelper helper = new ApiHelper();
        try {
            String response = helper.sendGet("http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/accounts/9999999999/standingorders/" +
                    numberSlot.getValue());
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            StandingOrder standingOrder = mapper.readValue(response, StandingOrder.class);
            String urlParameters = "payee=" + standingOrder.getPayee() + "&amount=" + Double.valueOf(amountSlot.getValue()) +
                    "&destinationAccount=" + standingOrder.getDestinationAccount() + "&firstExecution=2017-05-16"
                    + "&executionRate=" + standingOrder.getExecutionRate() + "&description=" + standingOrder.getDescription() + "&status=" + standingOrder.getStatus();
            helper.sendPut("http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/accounts/9999999999/standingorders/" +
                    numberSlot.getValue(), urlParameters);
        } catch (Exception e) {
            logger.error(e.getMessage());
            card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geändert.");
        speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geaendert.");
        return SpeechletResponse.newTellResponse(speech, card);
    }


}