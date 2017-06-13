package amosalexa.services.securitiesAccount;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import api.banking.SecuritiesAccountAPI;
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
import model.banking.SecuritiesAccount;
import model.banking.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SecuritiesAccountInformationService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                SECURITIES_ACCOUNT_INFORMATION_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                SECURITIES_ACCOUNT_INFORMATION_INTENT,
                YES_INTENT,
                NO_INTENT
        );
    }

    // FIXME: Hardcoded SecuritiesAccount Id
    private static final Number SEC_ACCOUNT_ID = 1;


    private static final Logger LOGGER = LoggerFactory.getLogger(SecuritiesAccountInformationService.class);

    private static final String CONTEXT = "DIALOG_CONTEXT";

    private static final String SECURITIES_ACCOUNT_INFORMATION_INTENT = "SecuritiesAccountInformationIntent";

    private List<Security> securities;

    public SecuritiesAccountInformationService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    /**
     * ties the Speechlet Subject (Amos Alexa Speechlet) with an Speechlet Observer
     *
     * @param speechletSubject service
     */
    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for(String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        String intentName = request.getIntent().getName();
        LOGGER.info("Intent Name: " + intentName);

        if (SECURITIES_ACCOUNT_INFORMATION_INTENT.equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            session.setAttribute(CONTEXT, "SecuritiesAccountInformation");
            return getSecuritiesAccountInformation(request.getIntent(), session);
        }

        if(session.getAttribute(CONTEXT) == null || !session.getAttribute(CONTEXT).equals("SecuritiesAccountInformation")) {
            return null;
        }

        if (YES_INTENT.equals(intentName)) {
            return getNextSecuritiesAccountInformation(request.getIntent(), session);
        } else if (NO_INTENT.equals(intentName)) {
            return getSpeechletResponse("Okay, tschuess!", "", false);
        } else {
            return null;
        }
    }

    private SpeechletResponse getSecuritiesAccountInformation(Intent intent, Session session) {
        LOGGER.info("SecuritiesAccountInformation called.");

        Map<String, Slot> slots = intent.getSlots();
        SecuritiesAccount securitiesAccount = SecuritiesAccountAPI.getSecuritiesAccount(SEC_ACCOUNT_ID);

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Depot-Information");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        Collection<Security> securitiesCollection = SecuritiesAccountAPI.getSecuritiesForAccount(SEC_ACCOUNT_ID);

        if (securitiesAccount == null || securitiesCollection == null || securitiesCollection.size() == 0) {
            card.setContent("Keine Info vorhanden.");
            speech.setText("Keine Info vorhanden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        securities = new LinkedList<>(securitiesCollection);

        // Check if user requested to have their stranding orders sent to their email address
        Slot channelSlot = slots.get("Channel");
        boolean sendPerEmail = channelSlot != null &&
                channelSlot.getValue() != null &&
                channelSlot.getValue().equals("email");

        StringBuilder textBuilder = new StringBuilder();

        if (sendPerEmail) {
            // TODO: Send info to user's email address
            textBuilder.append("Ich habe die Informationen ueber dein Depot an deine E-Mail-Adresse gesendet.");
        } else {

            Slot payee = null;
            //Slot payeeSlot = slots.get("Payee");
            //String payee = payeeSlot.getValue();

            if (payee != null) {
                //TODO
//                // User specified a recipient
//
//                List<StandingOrder> orders = new LinkedList<>();
//
//                // Find closest standing orders that could match the request.
//                for (int i = 0; i < standingOrders.length; i++) {
//                    if (StringUtils.getLevenshteinDistance(payee, standingOrders[i].getPayee()) <=
//                            standingOrders[i].getPayee().length() / 3) {
//                        orders.add(standingOrders[i]);
//                    }
//                }
//
//                textBuilder.append(orders.size() == 1 ? "Es wurde ein Dauerauftrag gefunden." :
//                        "Es wurden " + orders.size() +
//                                " Dauerauftraege gefunden.");
//
//                int i = 1;
//                for (StandingOrder order : orders) {
//                    textBuilder.append(' ');
//
//                    textBuilder.append("Dauerauftrag ")
//                            .append(order.getStandingOrderId())
//                            .append(": ");
//
//                    textBuilder.append("Ueberweise ").append(order.getExecutionRateString())
//                            .append(order.getAmount())
//                            .append(" Euro an ")
//                            .append(order.getPayee())
//                            .append(".");
//
//                    i++;
//                }
            } else {
                // Just return all standing orders

                textBuilder.append("Du hast momentan ")
                        .append(securities.size() == 1 ? "ein Wertpapier in deinem Depot. " : securities.size()
                                + " Wertpapiere in deinem Depot. ");

                for (int i = 0; i <= 1; i++) {
                    String stockPrice = FinanceApi.getStockPrice(securities.get(i));
                    LOGGER.info("Stock Price: " + stockPrice);
                    textBuilder.append(' ');

                    textBuilder.append("Wertpapier Nummer")
                            .append(securities.get(i).getSecurityId())
                            .append(": ").append(securities.get(i).getDescription()).
                            append(" mit einem momentanen Wert von ").append(stockPrice).append(" Euro. ");
                }

                if (securities.size() > 2) {
                    textBuilder.append(" Moechtest du einen weiteren Eintrag hoeren?");

                    String text = textBuilder.toString();

                    card.setContent(text);
                    speech.setText(text);

                    // Create reprompt
                    Reprompt reprompt = new Reprompt();
                    reprompt.setOutputSpeech(speech);

                    // Save current list offset in this session
                    session.setAttribute("NextSecurity", 2);

                    return SpeechletResponse.newAskResponse(speech, reprompt);
                }
            }
        }

        String text = textBuilder.toString();

        card.setContent(text);
        speech.setText(text);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse getNextSecuritiesAccountInformation(Intent intent, Session session) {
        int nextEntry = (int) session.getAttribute("NextSecurity");
        Security nextSecurity;
        StringBuilder textBuilder = new StringBuilder();

        if (nextEntry < securities.size()) {
            String stockPrice = FinanceApi.getStockPrice(securities.get(nextEntry));
            nextSecurity = securities.get(nextEntry);
            textBuilder.append("Wertpapier Nummer ")
                    .append(nextSecurity.getSecurityId())
                    .append(": ").append(securities.get(nextEntry).getDescription()).
                    append(" mit einem momentanen Wert von ").append(stockPrice).append(" Euro. ");

            if (nextEntry == (securities.size() - 1)) {
                textBuilder.append(" Das waren alle Wertpapiere in deinem Depot.");
                PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
                speech.setText(textBuilder.toString());
                return SpeechletResponse.newTellResponse(speech);
            } else {
                textBuilder.append(" Moechtest du einen weiteren Eintrag hoeren?");
            }

            // Save current list offset in this session
            session.setAttribute("NextSecurity", nextEntry + 1);

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
            speech.setText("Das waren alle Wertpapiere in deinem Depot.");
            return SpeechletResponse.newTellResponse(speech);
        }
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
