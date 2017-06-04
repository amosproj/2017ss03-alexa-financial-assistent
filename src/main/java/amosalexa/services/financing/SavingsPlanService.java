package amosalexa.services.financing;

import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import api.banking.AccountAPI;
import api.banking.TransactionAPI;
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
import com.amazon.speech.ui.SsmlOutputSpeech;
import model.banking.StandingOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class SavingsPlanService implements SpeechService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SavingsPlanService.class);

    // FIXME: Hardcoded Strings
    private static final String SOURCE_ACCOUNT = "DE42100000009999999999";
    private static final String DESTINATION_ACCOUNT = "DE39100000007777777777";
    private static final String VALUE_DATE = "2017-05-24";
    private static final String DESCRIPTION = "Savings Plan";
    private static final String STANDING_ORDER_ACCOUNT = "9999999999";
    private static final String PAYEE = "Max Mustermann";
    private static final StandingOrder.ExecutionRate EXECUTION_RATE = StandingOrder.ExecutionRate.MONTHLY;

    private static final String CONTEXT = "DIALOG_CONTEXT";

    private static final String GRUNDBETRAG_KEY = "Grundbetrag";

    private static final String ANZAHL_JAHRE_KEY = "AnzahlJahre";

    private static final String EINZAHLUNG_MONAT_KEY = "EinzahlungMonat";

    public SavingsPlanService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = intent.getName();
        Session session = requestEnvelope.getSession();
        LOGGER.info("Intent Name: " + intentName);
        String context = (String) session.getAttribute(CONTEXT);

        if ("SavingsPlanIntent".equals(intentName)) {
            return askForSavingsParameter(intent, session);
        } else if ("AMAZON.YesIntent".equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return createSavingsPlan(intent, session);
        } else if ("AMAZON.NoIntent".equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return cancelAction();
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, "SavingsPlanIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
    }

    private SpeechletResponse askForSavingsParameter(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();
        session.setAttribute(CONTEXT, "SavingsPlan");

        String grundbetrag = slots.get(GRUNDBETRAG_KEY).getValue();
        String anzahlJahre = slots.get(ANZAHL_JAHRE_KEY).getValue();
        String monatlicheEinzahlung = slots.get(EINZAHLUNG_MONAT_KEY).getValue();

        String speechText, repromptText;

        LOGGER.info("Grundbetrag: " + grundbetrag);
        LOGGER.info("Jahre: " + anzahlJahre);
        LOGGER.info("monatliche Einzahlung: " + monatlicheEinzahlung);
        LOGGER.debug("Storage Before: " + session.getAttributes());

        if (grundbetrag != null && session.getAttributes().containsKey(GRUNDBETRAG_KEY)) {
            monatlicheEinzahlung = grundbetrag;
            grundbetrag = null;
        }

        if (grundbetrag != null) {
            session.setAttribute(GRUNDBETRAG_KEY, grundbetrag);
        }
        if (anzahlJahre != null) {
            session.setAttribute(ANZAHL_JAHRE_KEY, anzahlJahre);
        }
        if (monatlicheEinzahlung != null) {
            session.setAttribute(EINZAHLUNG_MONAT_KEY, monatlicheEinzahlung);
        }

        if (grundbetrag == null && !session.getAttributes().containsKey(GRUNDBETRAG_KEY)) {
            speechText = "Was moechtest du als Grundbetrag anlegen?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        if (anzahlJahre == null && !session.getAttributes().containsKey(ANZAHL_JAHRE_KEY)) {
            speechText = "Wie viele Jahre moechtest du das Geld anlegen?";
            //TODO better use duration?
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        if (monatlicheEinzahlung == null && !session.getAttributes().containsKey(EINZAHLUNG_MONAT_KEY)) {
            if (grundbetrag == null && !session.getAttributes().containsKey(GRUNDBETRAG_KEY)) {
                speechText = "Du musst zuerst einen Grundbetrag angeben.";
                repromptText = speechText;
                return getSpeechletResponse(speechText, repromptText, true);
            }
            speechText = "Wie viel Geld moechtest du monatlich investieren?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        String grundbetragString = (String) session.getAttribute(GRUNDBETRAG_KEY);
        String einzahlungMonatString = (String) session.getAttribute(EINZAHLUNG_MONAT_KEY);
        String anzahlJahreString = (String) session.getAttribute(ANZAHL_JAHRE_KEY);

        String calculationString = calculateSavings(grundbetragString, einzahlungMonatString, anzahlJahreString);

        SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml("<speak>Bei einem Zinssatz von zwei Prozent waere der Gesamtsparbetrag am Ende " +
                "des Zeitraums insgesamt <say-as interpret-as=\"number\">" + calculationString
                + "</say-as> Euro. Soll ich diesen Sparplan fuer dich anlegen?</speak>");

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        LOGGER.debug("Session afterwards: " + session.getAttributes());

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse createSavingsPlan(Intent intent, Session session) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        String grundbetrag = (String) session.getAttribute(GRUNDBETRAG_KEY);
        String monatlicheZahlung = (String) session.getAttribute(EINZAHLUNG_MONAT_KEY);
        createSavingsPlanOneOffPayment(grundbetrag);
        StandingOrder so = createSavingsPlanStandingOrder(monatlicheZahlung);
        //TODO replace date
        speech.setText("Okay! Ich habe den Sparplan angelegt. Der Grundbetrag von " + grundbetrag + " Euro wird deinem Sparkonto " +
                "gutgeschrieben. Die erste regelmaeßige Einzahlung von " + monatlicheZahlung + " Euro erfolgt am "
                + so.getFirstExecutionSpeechString() + ".");
        return SpeechletResponse.newTellResponse(speech);
    }

    private void createSavingsPlanOneOffPayment(String betrag) {
        Number amount = Integer.parseInt(betrag);
        TransactionAPI.createTransaction(amount, SOURCE_ACCOUNT, DESTINATION_ACCOUNT, VALUE_DATE, DESCRIPTION);
    }

    private StandingOrder createSavingsPlanStandingOrder(String monatlicheZahlung) {
        Number amount = Integer.parseInt(monatlicheZahlung);
        String firstExecution = formatDate(getFirstExecutionDate(), "yyyy-MM-dd");
        LOGGER.info("FirstExecution: " + firstExecution);
        return AccountAPI.createStandingOrderForAccount(STANDING_ORDER_ACCOUNT, PAYEE, amount, DESTINATION_ACCOUNT, firstExecution, EXECUTION_RATE, DESCRIPTION);
    }

    private Date getFirstExecutionDate() {
        Calendar today = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        next.clear();
        next.set(Calendar.YEAR, today.get(Calendar.YEAR));
        next.set(Calendar.MONTH, today.get(Calendar.MONTH) + 1);
        next.set(Calendar.DAY_OF_MONTH, 1);
        LOGGER.info("Next.getTime :" + next.getTime());
        return next.getTime();
    }

    //TODO helper method, should be moved
    private String formatDate(Date date, String pattern) {
        SimpleDateFormat dt = new SimpleDateFormat(pattern);
        return dt.format(date);
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
                                                   boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
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

    private SpeechletResponse cancelAction() {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText("OK tschuess!");
        return SpeechletResponse.newTellResponse(speech);
    }

    private String calculateSavings(String grundbetrag, String monatlicheEinzahlung, String jahre) {
        DecimalFormat df = new DecimalFormat("#.##");

        //The principal investment amount
        double p = Double.valueOf(grundbetrag);

        //The annual interest rate TODO hardcoded 2 percent?
        double r = 0.02;

        //The number of times that interest is compounded per year (monthly = 12)
        double n = 12;

        //The number of years the money is invested
        int t = Integer.valueOf(jahre);

        //Compound interest for principal
        double amount1 = p * Math.pow(1 + (r / n), (n * t));

        //The monthly payment
        double pmt = Double.valueOf(monatlicheEinzahlung);

        //Future value of a series
        double amount2 = pmt * (((Math.pow((1 + r / n), n * t)) - 1) / (r / n)); //* (1+ (r/n));
        //Note that last multiplication is optional! (Two ways of calculating)

        double totalAmount = amount1 + amount2;
        return df.format(totalAmount);
    }
}