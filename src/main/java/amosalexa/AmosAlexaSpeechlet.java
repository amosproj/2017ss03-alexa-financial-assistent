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

import amosalexa.dialogsystem.DialogResponseManager;
import amosalexa.security.AuthenticationManager;
import amosalexa.services.bankaccount.BankAccountService;
import amosalexa.services.bankcontact.BankContactService;
import amosalexa.services.blockcard.BlockCardService;
import amosalexa.services.securitiesAccount.SecuritiesAccountInformationService;
import amosalexa.services.pricequery.PriceQueryService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class AmosAlexaSpeechlet implements SpeechletSubject {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmosAlexaSpeechlet.class);

    private Map<String, List<SpeechletObserver>> speechServiceObservers = new HashMap<>();

    private static AmosAlexaSpeechlet amosAlexaSpeechlet = new AmosAlexaSpeechlet();

    public static AmosAlexaSpeechlet getInstance() {

        new BankAccountService(amosAlexaSpeechlet);
        new PriceQueryService(amosAlexaSpeechlet);
        new BankContactService(amosAlexaSpeechlet);
        new BlockCardService(amosAlexaSpeechlet);
        new SecuritiesAccountInformationService(amosAlexaSpeechlet);
        new AuthenticationManager(amosAlexaSpeechlet);

        return amosAlexaSpeechlet;
    }

    /**
     * attach a speechlet observer - observer will be notified if the intent name matches the key
     *
     * @param speechletObserver
     * @param intentName
     */
    @Override
    public void attachSpeechletObserver(SpeechletObserver speechletObserver, String intentName) {
        List<SpeechletObserver> list = speechServiceObservers.get(intentName);

        if (list == null) {
            list = new LinkedList<>();
            speechServiceObservers.put(intentName, list);
        }

        list.add(speechletObserver);
    }

    /**
     * notifies the speechlet observer by the requested intent name - invokes method of observer
     *
     * @param requestEnvelope request from amazon
     * @return SpeechletResponse
     */
    @Override
    public SpeechletResponse notifyOnIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        List<SpeechletObserver> list = speechServiceObservers.get(requestEnvelope.getRequest().getIntent().getName());

        if (list == null) {
            return null;
        }

        for (SpeechletObserver speechService : list) {
            SpeechletResponse response = null;
            try {
                response = speechService.onIntent(requestEnvelope);
            } catch (SpeechletException e) {
                LOGGER.error(e.getMessage());
            }

            if (response != null) {
                return response;
            }
        }

        return null;
    }


    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        LOGGER.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        LOGGER.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        LOGGER.info("Authenticated: " + AuthenticationManager.isAuthenticated());

        LOGGER.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
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
        } else if ("bankTransfer".equals(intentName)) {
            return bankTransfer(intent.getSlots());
        } else if ("TestListIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "TestList"); // Set CURRENTDIALOG to start the TestList dialog
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("ReplacementCardIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "ReplacementCard"); // Set CURRENTDIALOG to start the ReplacementCard dialog
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("ReplacementCardReasonIntent".equals(intentName)) {
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("SavingsPlanIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "SavingsPlan"); // Set CURRENTDIALOG to start the SavingsPlan dialog
            return DialogResponseManager.getInstance().handle(intent, sessionStorage);
        } else if ("FourDigitNumberIntent".equals(intentName)) {
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("StandingOrdersInfoIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "StandingOrders");
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("StandingOrdersDeleteIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "StandingOrders");
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("StandingOrdersModifyIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "StandingOrders");
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("AMAZON.YesIntent".equals(intentName)) {
            SpeechletResponse response = DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
            if (response != null) {
                return response;
            }
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            SpeechletResponse response = DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
            if (response != null) {
                return response;
            }
        }

        SpeechletResponse response = notifyOnIntent(requestEnvelope);

        if (response == null) {
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Ein Fehler ist aufgetreten.");

            return SpeechletResponse.newTellResponse(speech);
        }

        return response;
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        LOGGER.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
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
     * Transfers money and returns response with
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse bankTransfer(Map<String, Slot> slots) {
        Slot amountSlot = slots.get("amount");
        Slot nameSlot = slots.get("name");

        String amount = amountSlot.getValue();
        String name = nameSlot.getValue();

        //getting response regarding account balance
        this.getAccountBalanceResponse();

        //transfering money
        String url = "http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/transactions";
        String urlParams = "{\n" +
                "  \"amount\" : " + amount + ",\n" +
                "  \"sourceAccount\" : \"DE23100000001234567890\",\n" +
                "  \"destinationAccount\" : \"DE60643995205405578292\",\n" +
                "  \"valueDate\" : \"2017-05-16\",\n" +
                "  \"description\" : \"Beschreibung\"\n" +
                "}";
        ApiHelper.sendPost(url, urlParams);

        //reply message
        String speechText = "Die " + amount + " wurden zu " + name + " überwiesen";

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

}