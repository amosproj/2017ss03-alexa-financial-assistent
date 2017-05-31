package amosalexa.services.bankaccount;

import amosalexa.ApiHelper;
import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import api.BankingRESTClient;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.banking.account.StandingOrder;
import model.banking.account.StandingOrderResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//TODO write test
public class StandingOrderService implements SpeechService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandingOrderService.class);

    private static final String CONTEXT = "DIALOG_CONTEXT";

    private List<StandingOrder> standingOrders;

    public StandingOrderService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * Ties the Speechlet Subject (Amos Alexa Speechlet) with an Speechlet Observer
     *
     * @param speechletSubject service
     */
    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, "StandingOrdersInfoIntent");
        speechletSubject.attachSpeechletObserver(this, "StandingOrdersDeleteIntent");
        speechletSubject.attachSpeechletObserver(this, "StandingOrdersModifyIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.StopIntent");
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = intent.getName();
        Session session = requestEnvelope.getSession();
        String dialogContext = (String) session.getAttribute(CONTEXT);

        LOGGER.info("Intent Name: " + intentName);
        LOGGER.info("Context: " + dialogContext);

        if ("StandingOrdersInfoIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            session.setAttribute(CONTEXT, "StandingOrderInfo");
            return getStandingOrdersInfoResponse(intent, session);
        } else if ("StandingOrdersDeleteIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            session.setAttribute(CONTEXT, "StandingOrderDeletion");
            return askForDDeletionConfirmation(intent, session);
        } else if ("StandingOrdersModifyIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            session.setAttribute(CONTEXT, "StandingOrderModification");
            return askForModificationConfirmation(intent, session);
        } else if ("AMAZON.YesIntent".equals(intentName) && dialogContext != null && dialogContext.equals("StandingOrderInfo")) {
            return getNextStandingOrderInfo(session);
        } else if ("AMAZON.YesIntent".equals(intentName) && dialogContext != null && dialogContext.equals("StandingOrderDeletion")) {
            return getStandingOrdersDeleteResponse(intent, session);
        } else if ("AMAZON.YesIntent".equals(intentName) && dialogContext != null && dialogContext.equals("StandingOrderModification")) {
            return getStandingOrdersModifyResponse(intent, session);
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            return getSpeechletResponse("Okay, tschuess!", "", false);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            //TODO StopIntent not working? Test
            return null;
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the standing orders intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getStandingOrdersInfoResponse(Intent intent, Session session) {
        LOGGER.info("StandingOrdersResponse called.");

        Map<String, Slot> slots = intent.getSlots();

        ObjectMapper mapper = new ObjectMapper();
        //TODO do not use ApiHelper
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
            LOGGER.error(e.getMessage());
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

        standingOrders = standingOrderResponse.getStandingOrders();

        // Check if user requested to have their stranding orders sent to their email address
        Slot channelSlot = slots.get("Channel");
        boolean sendPerEmail = channelSlot != null &&
                channelSlot.getValue() != null &&
                channelSlot.getValue().equals("email");

        StringBuilder textBuilder = new StringBuilder();

        if (sendPerEmail) {
            // TODO: Send standing orders to user's email address
            textBuilder.append("Ich habe")
                    .append(standingOrders.size())
                    .append(" an deine E-Mail-Adresse gesendet.");
        } else {
            // We want to directly return standing orders here

            Slot payeeSlot = slots.get("Payee");
            String payee = payeeSlot.getValue();

            if (payee != null) {
                // User specified a recipient

                List<StandingOrder> orders = new LinkedList<>();

                // Find closest standing orders that could match the request.
                for (int i = 0; i < standingOrders.size(); i++) {
                    if (StringUtils.getLevenshteinDistance(payee, standingOrders.get(i).getPayee()) <=
                            standingOrders.get(i).getPayee().length() / 3) {
                        orders.add(standingOrders.get(i));
                    }
                }

                textBuilder.append(orders.size() == 1 ? "Es wurde ein Dauerauftrag gefunden." :
                        "Es wurden " + orders.size() +
                                " Dauerauftraege gefunden.");

                int i = 1;
                for (StandingOrder order : orders) {
                    textBuilder.append(' ');

                    textBuilder.append("Dauerauftrag ")
                            .append(order.getStandingOrderId())
                            .append(": ");

                    textBuilder.append("Ueberweise ").append(order.getExecutionRateString())
                            .append(order.getAmount())
                            .append(" Euro an ")
                            .append(order.getPayee())
                            .append(".");

                    i++;
                }
            } else {
                // Just return all standing orders

                textBuilder.append("Du hast momentan ")
                        .append(standingOrders.size() == 1 ? "einen Dauerauftrag. " : standingOrders.size() + " Dauerauftraege. ");
                for (int i = 0; i <= 1; i++) {
                    textBuilder.append(' ');

                    textBuilder.append("Dauerauftrag ")
                            .append(standingOrders.get(i).getStandingOrderId())
                            .append(": ");

                    //FIXME hardcoded savings account iban?
                    boolean isSavingsPlanStandingOrder = standingOrders.get(i).getDestinationAccount().equals("DE39100000007777777777");

                    textBuilder.append("Ueberweise ").append(standingOrders.get(i).getExecutionRateString())
                            .append(standingOrders.get(i).getAmount())
                            .append(" Euro ")
                            .append(isSavingsPlanStandingOrder ? "auf dein Sparkonto " : "an " + standingOrders.get(i).getPayee())
                            .append(".");
                }

                if (standingOrders.size() > 2) {
                    textBuilder.append(" Moechtest du einen weiteren Eintrag hoeren?");

                    String text = textBuilder.toString();

                    card.setContent(text);
                    speech.setText(text);

                    // Create reprompt
                    Reprompt reprompt = new Reprompt();
                    reprompt.setOutputSpeech(speech);

                    // Save current list offset in this session
                    session.setAttribute("NextStandingOrder", 2);

                    return SpeechletResponse.newAskResponse(speech, reprompt);
                }
            }
        }

        String text = textBuilder.toString();

        card.setContent(text);
        speech.setText(text);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getNextStandingOrderInfo(Session session) {
        int nextEntry = (int) session.getAttribute("NextStandingOrder");
        StandingOrder nextSO;
        StringBuilder textBuilder = new StringBuilder();

        if (nextEntry < standingOrders.size()) {
            nextSO = standingOrders.get(nextEntry);
            textBuilder.append("Dauerauftrag Nummer ")
                    .append(nextSO.getStandingOrderId())
                    .append(": ");

            //FIXME hardcoded savings account iban?
            boolean isSavingsPlanStandingOrder = standingOrders.get(nextEntry).getDestinationAccount().equals("DE39100000007777777777");

            textBuilder.append("Ueberweise ").append(nextSO.getExecutionRateString())
                    .append(nextSO.getAmount())
                    .append(" Euro ")
                    .append(isSavingsPlanStandingOrder ? "auf dein Sparkonto " : "an " + nextSO.getPayee())
                    .append(".");

            if (nextEntry == (standingOrders.size() - 1)) {
                textBuilder.append(" Das waren alle vorhandenen Dauerauftraege.");
                PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
                speech.setText(textBuilder.toString());
                return SpeechletResponse.newTellResponse(speech);
            } else {
                textBuilder.append(" Moechtest du einen weiteren Eintrag hoeren?");
            }

            // Save current list offset in this session
            session.setAttribute("NextStandingOrder", nextEntry + 1);

            String text = textBuilder.toString();

            // Create the plain text output
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText(text);

            // Create reprompt
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(speech);

            return SpeechletResponse.newAskResponse(speech, reprompt);
        } else {
            // Create the plain text output
            PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
            speech.setText("Das waren alle vorhandenen Dauerauftraege.");

            return SpeechletResponse.newTellResponse(speech);
        }
    }

    private SpeechletResponse askForDDeletionConfirmation(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();
        Slot numberSlot = slots.get("Number");
        LOGGER.info("NumberSlot: " + numberSlot.getValue());

        session.setAttribute("StandingOrderToDelete", numberSlot.getValue());

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Moechtest du den Dauerauftrag mit der Nummer " + numberSlot.getValue()
                + " wirklich loeschen?");

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse askForModificationConfirmation(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();
        Slot numberSlot = slots.get("Number");
        Slot amountSlot = slots.get("Amount");
        Slot executionRateSlot = slots.get("ExecutionRate");
        Slot firstExecutionSlot = slots.get("FirstExecution");

        if (numberSlot.getValue() == null || (amountSlot.getValue() == null && executionRateSlot.getValue() == null && firstExecutionSlot.getValue() == null)) {
            String text = "Das habe ich nicht ganz verstanden. Bitte wiederhole deine Eingabe.";
            return getSpeechletResponse(text, text, true);
        }

        session.setAttribute("StandingOrderToModify", numberSlot.getValue());
        session.setAttribute("NewAmount", amountSlot.getValue());
        session.setAttribute("NewExecutionRate", executionRateSlot.getValue());
        session.setAttribute("NewFirstExecution", firstExecutionSlot.getValue());

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Soll ich den Betrag von Dauerauftrag Nummer " + numberSlot.getValue() + " wirklich auf " +
                amountSlot.getValue() + " Euro aendern?");
        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse getStandingOrdersDeleteResponse(Intent intent, Session session) {
        LOGGER.info("StandingOrdersDeleteResponse called.");

        String standingOrderToDelete = (String) session.getAttribute("StandingOrderToDelete");

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Lösche Dauerauftrag");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        //TODO do not use ApiHelper anymore
        ApiHelper helper = new ApiHelper();
        try {
            helper.sendDelete("http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/accounts/9999999999/standingorders/" + standingOrderToDelete);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            card.setContent("Dauerauftrag Nummer " + standingOrderToDelete + " wurde nicht gefunden.");
            speech.setText("Dauerauftrag Nummer " + standingOrderToDelete + " wurde nicht gefunden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        card.setContent("Dauerauftrag Nummer " + standingOrderToDelete + " wurde gelöscht.");
        speech.setText("Dauerauftrag Nummer " + standingOrderToDelete + " wurde geloescht.");
        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getStandingOrdersModifyResponse(Intent intent, Session session) {
        String standingOrderToModify = (String) session.getAttribute("StandingOrderToModify");
        String newAmount = (String) session.getAttribute("NewAmount");
        //TODO never used
        String newExecutionRate = (String) session.getAttribute("NewExecutionRate");
        String newFirstExecution = (String) session.getAttribute("NewFirstExecution");

        ObjectMapper mapper = new ObjectMapper();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Ändere Dauerauftrag");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        JSONObject jsonObject = new JSONObject();
        try {
            //TODO finish
            jsonObject.put("payee", "Max Mustermann");
            jsonObject.put("amount", newAmount);
            jsonObject.put("destinationAccount", "DE39100000007777777777");
            jsonObject.put("firstExecution", "2017-06-01");
            jsonObject.put("executionRate", "MONTHLY");
            jsonObject.put("description", "Updated standing Order");
        } catch (JSONException e) {
            LOGGER.error(e.getMessage());
        }
        BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();
        bankingRESTClient.putBankingModelObject("/api/v1_0/accounts/9999999999/standingorders/" + standingOrderToModify, jsonObject.toString(), StandingOrder.class);

        card.setContent("Dauerauftrag Nummer " + standingOrderToModify + " wurde geändert.");
        speech.setText("Dauerauftrag Nummer " + standingOrderToModify + " wurde geaendert.");
        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
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
}