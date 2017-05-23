package amosalexa.services.bankaccount;

import amosalexa.ApiHelper;
import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.banking.account.StandingOrder;
import model.banking.account.StandingOrderResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StandingOrderService implements SpeechService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandingOrderService.class);

    private static final String STANDING_ORDERS_INFO_INTENT = "StandingOrdersInfoIntent";

    public StandingOrderService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, STANDING_ORDERS_INFO_INTENT);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        LOGGER.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        LOGGER.info(getClass().toString() + " Intent started: " + intentName);

        if ("StandingOrdersInfoIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            return getStandingOrdersInfoResponse(intent);
        } else if ("StandingOrdersDeleteIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            return getStandingOrdersDeleteResponse(intent);
        } else if ("StandingOrdersModifyIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            return getStandingOrdersModifyResponse(intent);
        }
        return null;
    }

    /**
     * Creates a {@code SpeechletResponse} for the standing orders intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getStandingOrdersInfoResponse(Intent intent) {
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

                textBuilder.append(orders.size() == 1 ? "Es wurde ein Dauerauftrag gefunden." :
                        "Es wurden " + orders.size() +
                                " Dauerauftraege gefunden.");

                int i = 1;
                for (StandingOrder order : orders) {
                    textBuilder.append(' ');

                    textBuilder.append("Dauerauftrag Nummer ")
                            .append(i)
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
                for (int i = 0; i < standingOrders.length; i++) {
                    textBuilder.append(' ');

                    textBuilder.append("Dauerauftrag Nummer ")
                            .append(i + 1)
                            .append(": ");

                    textBuilder.append("Ueberweise ").append(standingOrders[i].getExecutionRateString())
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

    private SpeechletResponse getStandingOrdersDeleteResponse(Intent intent) {
        LOGGER.info("StandingOrdersDeleteResponse called.");

        Map<String, Slot> slots = intent.getSlots();

        Slot numberSlot = slots.get("Number");
        LOGGER.info("NumberSlot: " + numberSlot.getValue());

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Lösche Dauerauftrag");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        ApiHelper helper = new ApiHelper();
        try {
            helper.sendDelete("http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com/api/v1_0/accounts/9999999999/standingorders/" + numberSlot.getValue());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde gelöscht.");
        speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geloescht.");
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
            LOGGER.error(e.getMessage());
            card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde nicht gefunden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        card.setContent("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geändert.");
        speech.setText("Dauerauftrag Nummer " + numberSlot.getValue() + " wurde geaendert.");
        return SpeechletResponse.newTellResponse(speech, card);
    }

}