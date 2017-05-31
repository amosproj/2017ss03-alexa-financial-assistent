package amosalexa.services.securitiesAccount;

import amosalexa.SpeechletSubject;
import amosalexa.depot.FinanceApi;
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
import model.banking.account.SecuritiesAccount;
import model.banking.account.Security;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Map;

public class SecuritiesAccountInformationService implements SpeechService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecuritiesAccountInformationService.class);

    private static final String CONTEXT = "CURRENT_CONTEXT";

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
        speechletSubject.attachSpeechletObserver(this, "SecuritiesAccountInformationIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        String intentName = request.getIntent().getName();
        LOGGER.info("Intent Name: " + intentName);

        if ("SecuritiesAccountInformationIntent".equals(intentName)) {
            LOGGER.info(getClass().toString() + " Intent started: " + intentName);
            session.setAttribute(CONTEXT, "SecuritiesAccountInformation");
            return getSecuritiesAccountInformation(request.getIntent(), session);
        }

        if ("AMAZON.YesIntent".equals(intentName)) {
            return getNextSecuritiesAccountInformation(request.getIntent(), session);
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            session.setAttribute("BlockCardService.CardNumber", null);
            return getSpeechletResponse("Okay, tschuess!", "", false);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            return null;
        }
        throw new SpeechletException("Unhandled intent: " + intentName);
    }

    private SpeechletResponse getSecuritiesAccountInformation(Intent intent, Session session) {
        LOGGER.info("SecuritiesAccountInformation called.");

        Map<String, Slot> slots = intent.getSlots();
        SecuritiesAccount securitiesAccount = getSecuritiesAccountForAccount("1");

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Depot-Information");

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();

        if (securitiesAccount == null || securitiesAccount.getSecurities() == null) {
            card.setContent("Keine Info vorhanden.");
            speech.setText("Keine Info vorhanden.");
            return SpeechletResponse.newTellResponse(speech, card);
        }

        securities = securitiesAccount.getSecurities();

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

    private SecuritiesAccount getSecuritiesAccountForAccount(String number) throws HttpClientErrorException {
        BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();
        SecuritiesAccount secAccount = (SecuritiesAccount) bankingRESTClient.getBankingModelObject("/api/v1_0/securitiesAccounts/" + number, SecuritiesAccount.class);
        return secAccount;
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
