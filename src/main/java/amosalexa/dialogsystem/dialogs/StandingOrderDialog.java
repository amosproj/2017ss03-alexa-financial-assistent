package amosalexa.dialogsystem.dialogs;

import amosalexa.ApiHelper;
import amosalexa.SessionStorage;
import amosalexa.dialogsystem.DialogHandler;
import api.BankingRESTClient;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
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

public class StandingOrderDialog implements DialogHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandingOrderDialog.class);

    private static final String CONTEXT = "CURRENT_CONTEXT";

    private static final String STANDING_ORDER_DIALOG = "StandingOrders";

    StandingOrder[] standingOrders;

    @Override
    public String getDialogName() {
        return STANDING_ORDER_DIALOG;
    }

    @Override
    public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException {
        String intentName = intent.getName();
        LOGGER.info("Intent Name: " + intentName);

        if ("StandingOrdersInfoIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            storage.put(CONTEXT, "StandingOrderInfo");
            return getStandingOrdersInfoResponse(intent, storage);
        } else if ("StandingOrdersDeleteIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            storage.put(CONTEXT, "StandingOrderDeletion");
            return askForDDeletionConfirmation(intent, storage);
        } else if ("StandingOrdersModifyIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            storage.put(CONTEXT, "StandingOrderModification");
            return getStandingOrdersModifyResponse(intent);
        } else if ("AMAZON.YesIntent".equals(intentName) && storage.get(CONTEXT).equals("StandingOrderInfo")) {
            return getNextStandingOrderInfo(storage);
        } else if ("AMAZON.YesIntent".equals(intentName) && storage.get(CONTEXT).equals("StandingOrderDeletion")) {
            return getStandingOrdersDeleteResponse(intent, storage);
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the standing orders intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getStandingOrdersInfoResponse(Intent intent, SessionStorage.Storage storage) {
        LOGGER.info("StandingOrdersResponse called.");

        Map<String, Slot> slots = intent.getSlots();

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

        standingOrders = standingOrderResponse.get_embedded().getStandingOrders();

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
                        .append(standingOrders.length == 1 ? "einen Dauerauftrag. " : standingOrders.length + " Dauerauftraege. ");
                for (int i = 0; i <= 1; i++) {
                    textBuilder.append(' ');

                    textBuilder.append("Dauerauftrag ")
                            .append(standingOrders[i].getStandingOrderId())
                            .append(": ");

                    textBuilder.append("Ueberweise ").append(standingOrders[i].getExecutionRateString())
                            .append(standingOrders[i].getAmount())
                            .append(" Euro an ")
                            .append(standingOrders[i].getPayee())
                            .append(".");
                }

                if (standingOrders.length > 2) {
                    textBuilder.append(" Moechtest du einen weiteren Eintrag hoeren?");

                    String text = textBuilder.toString();

                    card.setContent(text);
                    speech.setText(text);

                    // Create reprompt
                    Reprompt reprompt = new Reprompt();
                    reprompt.setOutputSpeech(speech);

                    // Save current list offset in this session
                    storage.put("NextStandingOrder", 2);

                    return SpeechletResponse.newAskResponse(speech, reprompt);
                }
            }
        }

        String text = textBuilder.toString();

        card.setContent(text);
        speech.setText(text);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getNextStandingOrderInfo(SessionStorage.Storage storage) {
        int nextEntry = (int) storage.get("NextStandingOrder");
        StandingOrder nextSO;
        StringBuilder textBuilder = new StringBuilder();

        if (nextEntry < standingOrders.length) {
            nextSO = standingOrders[nextEntry];
            textBuilder.append("Dauerauftrag Nummer ")
                    .append(nextSO.getStandingOrderId())
                    .append(": ");

            textBuilder.append("Ueberweise ").append(nextSO.getExecutionRateString())
                    .append(nextSO.getAmount())
                    .append(" Euro an ")
                    .append(nextSO.getPayee())
                    .append(".");


            // Save current list offset in this session
            storage.put("NextStandingOrder", nextEntry + 1);

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

    private SpeechletResponse askForDDeletionConfirmation(Intent intent, SessionStorage.Storage storage) {
        Map<String, Slot> slots = intent.getSlots();

        Slot numberSlot = slots.get("Number");
        LOGGER.info("NumberSlot: " + numberSlot.getValue());

        storage.put("StandingOrderToDelete", numberSlot.getValue());

        // Create the plain text output
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("Moechtest du den Dauerauftrag mit der Nummer " + numberSlot.getValue()
                + " wirklich loeschen?");

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse getStandingOrdersDeleteResponse(Intent intent, SessionStorage.Storage storage) {
        LOGGER.info("StandingOrdersDeleteResponse called.");

        String standingOrderToDelete = (String) storage.get("StandingOrderToDelete");

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Lösche Dauerauftrag");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

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

    private SpeechletResponse getStandingOrdersModifyResponse(Intent intent) {
        Map<String, Slot> slots = intent.getSlots();

        LOGGER.info("StandingOrdersModifyResponse called.");

        Slot numberSlot = slots.get("Number");
        LOGGER.info("NumberSlot: " + numberSlot.getValue());

        Slot amountSlot = slots.get("Amount");
        LOGGER.info("AmountSlot: " + amountSlot.getValue());

        Slot executionRateSlot = slots.get("ExecutionRate");
        LOGGER.info("ExecutionRateSlot: " + executionRateSlot.getValue());

        Slot firstExecutionSlot = slots.get("FirstExecution");
        LOGGER.info("FirstExecutionSlot: " + firstExecutionSlot.getValue());

        ObjectMapper mapper = new ObjectMapper();

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Ändere Dauerauftrag");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        JSONObject jsonObject = new JSONObject();
        try {
            //TODO
            jsonObject.put("payee", "Max Mustermann");
            jsonObject.put("amount", amountSlot.getValue());
            jsonObject.put("destinationAccount", "DE39100000007777777777");
            jsonObject.put("firstExecution", "2017-06-01");
            jsonObject.put("executionRate", "MONTHLY");
            jsonObject.put("description", "Updated standing Order");
        } catch (JSONException e) {
            LOGGER.error(e.getMessage());
        }
        BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();
        bankingRESTClient.putBankingModelObject("/api/v1_0/accounts/9999999999/standingorders/" + numberSlot.getValue(), jsonObject.toString(), StandingOrder.class);

        card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geändert.");
        speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geaendert.");
        return SpeechletResponse.newTellResponse(speech, card);
    }
}