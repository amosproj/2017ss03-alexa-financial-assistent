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
import amosalexa.services.bankaccount.TransactionService;
import amosalexa.services.bankcontact.BankContactService;
import amosalexa.services.budgettracker.BudgetTrackerService;
import amosalexa.services.cards.BlockCardService;
import amosalexa.services.cards.ReplacementCardService;
import amosalexa.services.contacts.ContactService;
import amosalexa.services.email.EMailService;
import amosalexa.services.financing.AffordabilityService;
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
        new AffordabilityService(amosAlexaSpeechlet);
        new TransactionService(amosAlexaSpeechlet);
        new PriceQueryService(amosAlexaSpeechlet);
        new BankContactService(amosAlexaSpeechlet);
        new SavingsPlanService(amosAlexaSpeechlet);
        new BlockCardService(amosAlexaSpeechlet);
        new ReplacementCardService(amosAlexaSpeechlet);
        new TransferTemplateService(amosAlexaSpeechlet);
        new SecuritiesAccountInformationService(amosAlexaSpeechlet);
        new BalanceLimitService(amosAlexaSpeechlet);
        new EMailService(amosAlexaSpeechlet);
        new ContactService(amosAlexaSpeechlet);
        new BudgetTrackerService(amosAlexaSpeechlet);
        //new AuthenticationManager(amosAlexaSpeechlet);

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
        // Check for duplicate start Intents
        for (List<SpeechletObserver> observerList : speechServiceObservers.values()) {
            for (SpeechletObserver observer : observerList) {
                for (String startIntent : speechletObserver.getStartIntents()) {
                    if (observer.getStartIntents().contains(startIntent) && !observer.getDialogName().equals(speechletObserver.getDialogName())) {
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
            boolean validIntent = false;

            // Check if this Service should handle this Intent
            if (!speechService.getHandledIntents().contains(intentName)) {
                continue;
            }

            // Check if a dialog is active
            String currentDialogContext = (String) sessionStorage.get(SessionStorage.CURRENTDIALOG);
            if (currentDialogContext != null && !currentDialogContext.equals(speechService.getDialogName())) {
                // A dialog is active, but not for this service
                continue;
            }

            // Check if this Intent starts this Service
            if (currentDialogContext == null && speechService.getStartIntents().contains(intentName)) {
                // Set the dialog context in the current session
                sessionStorage.put(SessionStorage.CURRENTDIALOG, speechService.getDialogName());
                validIntent = true;
            }

            // Check if the active dialog is intended for this Service
            if (!validIntent && currentDialogContext != null && currentDialogContext.equals(speechService.getDialogName())) {
                validIntent = true;
            }

            if (!validIntent) {
                // Invalid intent, we should not let the Service handle it
                LOGGER.error("Invalid intent [" + intentName + "]!");
                continue;
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
        String context = (String) session.getAttribute("DIALOG_CONTEXT");

        LOGGER.info("Intent: " + intentName);
        LOGGER.info("Context: " + context);

        SessionStorage.Storage sessionStorage = SessionStorage.getInstance().getStorage(session.getSessionId());

        //TODO: @all use the new dialog system to handle for intents
        if ("PriceQueryService".equals(intentName) || "AffordIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "ProductSearch");
            return DialogResponseManager.getInstance().handle(intent, sessionStorage);
        } else if ("TestListIntent".equals(intentName)) {
            sessionStorage.put(SessionStorage.CURRENTDIALOG, "TestList"); // Set CURRENTDIALOG to start the TestList dialog
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
     * TODO Still needed???
     * <p>
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
            TransactionAPI.createTransaction(amountNum, "DE23100000001234567890", "DE60643995205405578292", "2017-05-16", "Beschreibung", "Hans", "Helga");

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
        TransactionAPI.createTransaction(amountNum, "DE23100000001234567890", "DE60643995205405578292", "2017-05-16", "Beschreibung", "Hans", "Helga");

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
     * TODO Code copied from AmosAlexaSpeechletTest class
     */
    private SpeechletRequestEnvelope<IntentRequest> getEnvelope(String intent, Session session, String... slots) throws IOException, NoSuchFieldException, IllegalAccessException {
        SpeechletRequestEnvelope<IntentRequest> envelope = (SpeechletRequestEnvelope<IntentRequest>) SpeechletRequestEnvelope.fromJson(buildJson(intent, session, slots));
        // Set session via reflection
        Field f1 = envelope.getClass().getDeclaredField("session");
        f1.setAccessible(true);
        f1.set(envelope, session);
        return envelope;
    }

    /**
     * TODO Code copied from AmosAlexaSpeechletTest class
     */
    private String buildJson(String intent, Session session, String... slots) {
        Calendar cal = Calendar.getInstance();
        Date time = cal.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        StringBuilder slotsJson = new StringBuilder();

        boolean first = true;
        for (String slot : slots) {
            if (first) {
                first = false;
            } else {
                slotsJson.append(',');
            }

            String[] slotParts = slot.split(":");
            slotsJson.append("\"").append(slotParts[0]).append("\":");
            slotsJson.append("{");
            slotsJson.append("\"name\":\"").append(slotParts[0]).append("\",");
            slotsJson.append("\"value\":\"").append(slotParts[1]).append("\"");
            slotsJson.append("}");
        }

        String json = "{\n" +
                "  \"session\": {\n" +
                "    \"sessionId\": \"" + session.getSessionId() + "\",\n" +
                "    \"application\": {\n" +
                "      \"applicationId\": \"amzn1.ask.skill.38e33c69-1510-43cd-be1d-929f08a966b4\"\n" +
                "    },\n" +
                "    \"attributes\": {},\n" +
                "    \"user\": {\n" +
                "      \"userId\": \"amzn1.ask.account.AHCD37TFVGP2S3OHTPFQTU2CVLBJMIVD3IIU6OZRGBTITENQO7W76SR5TRJMS5NDYJ4HQJTX726C4KMYHYZCOV5ONNFWFGH434UF4GUZQXKX2MEK2QE2B275MDM6YITSPWB3PAAFA2JKLQAJJXRJ65F2LXGDKP524L4YVA53IAA3CA6TVZCTBCLPVHBDIC3SLZJPT7PDZN4YUQA\"\n" +
                "    },\n" +
                "    \"new\": true\n" +
                "  },\n" +
                "  \"request\": {\n" +
                "    \"type\": \"IntentRequest\",\n" +
                "    \"requestId\": \"EdwRequestId.09495460-038e-4394-9a83-12115fba09b7\",\n" +
                "    \"locale\": \"de-DE\",\n" +
                "    \"timestamp\": \"" + formatter.format(time) + "\",\n" +
                "    \"intent\": {\n" +
                "      \"name\": \"" + intent + "\",\n" +
                "      \"slots\": {\n" +
                slotsJson.toString() +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"version\": \"1.0\"\n" +
                "}";

        return json;
    }

}
