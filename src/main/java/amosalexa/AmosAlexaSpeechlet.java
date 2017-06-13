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
import amosalexa.services.bankaccount.BalanceLimitService;
import amosalexa.services.bankaccount.BankAccountService;
import amosalexa.services.bankaccount.StandingOrderService;
import amosalexa.services.bankcontact.BankContactService;
import amosalexa.services.blockcard.BlockCardService;
import amosalexa.services.financing.SavingsPlanService;
import amosalexa.services.pricequery.PriceQueryService;
import amosalexa.services.securitiesAccount.SecuritiesAccountInformationService;
import amosalexa.services.transfertemplates.TransferTemplateService;
import api.banking.AccountAPI;
import api.banking.TransactionAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import model.banking.Account;
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
    private static AmosAlexaSpeechlet amosAlexaSpeechlet = new AmosAlexaSpeechlet();
    private Map<String, List<SpeechletObserver>> speechServiceObservers = new HashMap<>();

    public static AmosAlexaSpeechlet getInstance() {

        new BankAccountService(amosAlexaSpeechlet);
        new StandingOrderService(amosAlexaSpeechlet);
        new PriceQueryService(amosAlexaSpeechlet);
        new BankContactService(amosAlexaSpeechlet);
        new SavingsPlanService(amosAlexaSpeechlet);
        new BlockCardService(amosAlexaSpeechlet);
        new TransferTemplateService(amosAlexaSpeechlet);
        new SecuritiesAccountInformationService(amosAlexaSpeechlet);
        new BalanceLimitService(amosAlexaSpeechlet);
        //new AuthenticationManager(amosAlexaSpeechlet);

        return amosAlexaSpeechlet;
    }

    public static SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
                                                         boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Block Bank Card");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);
        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    /**
     * attach a speechlet observer - observer will be notified if the intent name matches the key
     *
     * @param speechletObserver
     * @param intentName
     */
    @Override
    public void attachSpeechletObserver(SpeechletObserver speechletObserver, String intentName) {
        // Check for duplicate start Intents
        for(List<SpeechletObserver> observerList : speechServiceObservers.values()) {
            for(SpeechletObserver observer : observerList) {
                for(String startIntent : speechletObserver.getStartIntents()) {
                    if(observer.getStartIntents().contains(startIntent) && !observer.getDialogName().equals(speechletObserver.getDialogName())) {
                        // Oh no, duplicate start Intent!
                        throw new IllegalArgumentException("Duplicate start Intent [" + startIntent + "], defined by both [" + observer.getDialogName() + "] " +
                        "and [" + speechletObserver.getDialogName() + "]!");
                    }
                }
            }
        }

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
        String intentName = requestEnvelope.getRequest().getIntent().getName();
        String sessionId = requestEnvelope.getSession().getSessionId();
        SessionStorage.Storage sessionStorage = SessionStorage.getInstance().getStorage(sessionId);

        List<SpeechletObserver> list = speechServiceObservers.get(intentName);

        if (list == null) {
            return null;
        }

        for (SpeechletObserver speechService : list) {
            // Check if this Service should handle this Intent
            if(!speechService.getHandledIntents().contains(intentName)) {
                continue;
            }

            // Check if a dialog is active
            String currentDialogContext = (String)sessionStorage.get(SessionStorage.CURRENTDIALOG);
            if(currentDialogContext != null && !currentDialogContext.equals(speechService.getDialogName())) {
                continue;
            }

            // Check if this Intent starts this Service
            if(currentDialogContext == null && speechService.getStartIntents().contains(intentName)) {
                // Set the dialog context in the current session
                sessionStorage.put(SessionStorage.CURRENTDIALOG, speechService.getDialogName());
            }

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

        //LOGGER.info("Authenticated: " + AuthenticationManager.isAuthenticated());

        LOGGER.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());



        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : "";

        SessionStorage.Storage sessionStorage = SessionStorage.getInstance().getStorage(session.getSessionId());

        if ("BankTransferIntent".equals(intentName)) {
            LOGGER.info("intent: BankTransferIntent");
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "BankTransfer"); // Set CURRENTDIALOG to start the BankTransfer dialog
            return DialogResponseManager.getInstance().handle(intent, sessionStorage);
        } else if ("TestListIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "TestList"); // Set CURRENTDIALOG to start the TestList dialog
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("ReplacementCardIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "ReplacementCard"); // Set CURRENTDIALOG to start the ReplacementCard dialog
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("ReplacementCardReasonIntent".equals(intentName)) {
            return DialogResponseManager.getInstance().handle(intent, sessionStorage); // Let the DialogHandler handle this intent
        } else if ("FourDigitNumberIntent".equals(intentName)) {
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
     * Transfers money and returns response with
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse bankTransfer(Map<String, Slot> slots) {
        Slot amountSlot = slots.get("amount");
        Slot nameSlot = slots.get("name");

        LOGGER.info("intent: Bank Transfer");


        if (slots.get("confirmation").getValue() == "Ja" || slots.get("confirmation").getValue() != null) {

            String amount = "2";
            String name = "Paul";

            //getting response regarding account balance
            Account account = AccountAPI.getAccount("0000000001");
            String balance = String.valueOf(account.getBalance());

            // FIXME: Hardcoded IBAN and so on
            Number amountNum = Integer.parseInt(amount);
            TransactionAPI.createTransaction(amountNum, "DE23100000001234567890", "DE60643995205405578292", "2017-05-16", "Beschreibung", "Hans" , "Helga");

            // confirmation question
            String speechText = "Dein aktueller Kontostand beträgt " + balance + ". "
            + "Möchtest du " + amount + " Euro an " + name + " überweisen?";

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

        String amount = "2";
        String name = "Paul";


        // FIXME: Hardcoded strings
        Number amountNum = Integer.parseInt(amount);
        TransactionAPI.createTransaction(amountNum, "DE23100000001234567890", "DE60643995205405578292", "2017-05-16", "Beschreibung","Hans" , "Helga" );

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
}