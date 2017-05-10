/**
 Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

 http://aws.amazon.com/apache2.0/

 or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package amosalexa;

import com.amazon.speech.slu.Slot;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This sample shows how to create a simple speechlet for handling speechlet requests.
 */
public class AmosAlexaSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(AmosAlexaSpeechlet.class);

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : "";

         
        if ("HelloWorldIntent".equals(intentName)) {
            return getHelloResponse();
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return getHelpResponse();
        } else if ("GetAccountBalance".equals(intentName)) {
            return getAccountBalanceResponse();
        } else if ("checkCreditLimit".equals(intentName)) {
            return getCreditLimitResponse();
        } else if ("StandingOrdersIntent".equals(intentName)) {
            return getStandingOrdersResponse(intent.getSlots());
        }  else {
            throw new SpeechletException("Invalid Intent");
                
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
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
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelloResponse() {
        String speechText = "Hello world";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
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
    private SpeechletResponse getStandingOrdersResponse(Map<String,Slot> slots) {
        /*
          This class represents a standing order. It currently only serves testing purposes.
          Later, it should be replaced by a structure corresponding to the API provided by the bank.
         */
        class StandingOrder {
            String recipient;
            double amount;

            StandingOrder(String recipient, double amount) {
                this.recipient = recipient;
                this.amount = amount;
            }
        }

        // This array contains some sample standing orders. Later, it should be filled through the API.
        StandingOrder[] dummyStandingOrders = new StandingOrder[]{
                new StandingOrder("Alice", 50),
                new StandingOrder("Bob", 30),
        };

        // Check if user requested to have their stranding orders sent to their email address
        Slot channelSlot = slots.get("Channel");
        boolean sendPerEmail = channelSlot != null &&
                channelSlot.getValue() != null &&
                channelSlot.getValue().equals("email");

        StringBuilder textBuilder = new StringBuilder();

        if (sendPerEmail) {
            // TODO: Send standing orders to user's email address

            textBuilder.append("I have sent ")
                    .append(dummyStandingOrders.length)
                    .append(" standing orders to your email address.");
        } else {
            // We want to directly return standing orders here

            Slot recipientSlot = slots.get("Recipient");
            String recipient = recipientSlot.getValue();

            if (recipient != null) {
                // User specified a recipient

                List<StandingOrder> orders = new LinkedList<>();

                // Find closest standing orders that could match the request.
                for (int i = 0; i < dummyStandingOrders.length; i++) {
                    if (StringUtils.getLevenshteinDistance(recipient, dummyStandingOrders[i].recipient) <=
                            dummyStandingOrders[i].recipient.length() / 3) {
                        orders.add(dummyStandingOrders[i]);
                    }
                }

                textBuilder.append("I have found ")
                        .append(orders.size())
                        .append(" standing orders.");

                int i = 1;
                for (StandingOrder order : orders) {
                    textBuilder.append(' ');

                    textBuilder.append("Standing order number ")
                            .append(i)
                            .append(": ");

                    textBuilder.append("Transfer ")
                            .append(order.amount)
                            .append(" Euros to ")
                            .append(order.recipient)
                            .append(".");

                    i++;
                }
            } else {
                // Just return all standing orders

                textBuilder.append("There are ")
                        .append(dummyStandingOrders.length)
                        .append(" standing orders.");

                for (int i = 0; i < dummyStandingOrders.length; i++) {
                    textBuilder.append(' ');

                    textBuilder.append("Standing order number ")
                            .append(i + 1)
                            .append(": ");

                    textBuilder.append("Transfer ")
                            .append(dummyStandingOrders[i].amount)
                            .append(" Euros to ")
                            .append(dummyStandingOrders[i].recipient)
                            .append(".");
                }
            }
        }

        String text = textBuilder.toString();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("StandingOrders");
        card.setContent(text);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(text);

        return SpeechletResponse.newTellResponse(speech, card);
    }
}
