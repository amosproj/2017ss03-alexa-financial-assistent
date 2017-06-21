package amosalexa.services.financing;

import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
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
import com.amazon.speech.ui.SsmlOutputSpeech;
import model.banking.StandingOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SavingsPlanService extends AbstractSpeechService implements SpeechService {

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                SAVINGS_PLAN_INTRO_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                SAVINGS_PLAN_INTRO_INTENT,
                SAVINGS_PLAN_AMOUNT_INTENT,
                SAVINGS_PLAN_NUMBER_OF_YEARS_INTENT,
                SAVINGS_PLAN_CHANGE_PARAMETER_INTENT,
                SAVINGS_PLAN_NEW_INTENT,
                YES_INTENT,
                NO_INTENT,
                STOP_INTENT
        );
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SavingsPlanService.class);

    /**
     * Default value for cards
     */
    private static final String SAVINGS_PLAN = "Sparplan";

    private static final String CONTEXT = "DIALOG_CONTEXT";

    //Intent names
    private static final String SAVINGS_PLAN_INTRO_INTENT = "SavingsPlanIntroIntent";
    private static final String SAVINGS_PLAN_AMOUNT_INTENT = "SavingsPlanAmountIntent";
    private static final String SAVINGS_PLAN_NUMBER_OF_YEARS_INTENT = "SavingsPlanNumberOfYearsIntent";
    private static final String SAVINGS_PLAN_CHANGE_PARAMETER_INTENT = "SavingsPlanChangeParameterIntent";
    private static final String SAVINGS_PLAN_NEW_INTENT = "SavingsPlanNewIntent";

    // FIXME: Hardcoded Strings for account
    private static final String SOURCE_ACCOUNT = "DE42100000009999999999";
    private static final String SAVINGS_ACCOUNT = "DE39100000007777777777";
    private static final String STANDING_ORDER_ACCOUNT = "9999999999";
    private static final String PAYEE = "Max Mustermann";
    private static final String DESCRIPTION_SAVINGS_PLAN = "Sparplan regelm. Einzahlung";
    private static final String DESCRIPTION_ONE_OFF_PAYMENT = "Sparplan Einmalzahlung";

    //Static keys for the three savings plan parameters (used as slot keys as well as session keys!)
    private static final String AMOUNT_KEY = "Betrag";
    private static final String NUMBER_OF_YEARS_KEY = "AnzahlJahre";
    private static final String MONTHLY_PAYMENT_KEY = "MonthlyPayment";

    //Slot key for SavingsPlanChangeParameterIntent
    private static final String SAVINGS_PLAN_PARAMETER_KEY = "SavingsPlanParameter";

    public SavingsPlanService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
        Intent intent = requestEnvelope.getRequest().getIntent();
        String intentName = intent.getName();
        Session session = requestEnvelope.getSession();
        LOGGER.info("Intent Name: " + intentName);
        String context = (String) session.getAttribute(CONTEXT);

        if (SAVINGS_PLAN_INTRO_INTENT.equals(intentName)) {
            session.setAttribute(CONTEXT, "SavingsPlan");
            return askForBasicAmount();
        } else if (SAVINGS_PLAN_AMOUNT_INTENT.equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return saveAmountAndContinue(intent, session);
        } else if (SAVINGS_PLAN_NUMBER_OF_YEARS_INTENT.equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return saveDurationAndContinue(intent, session);
        } else if (YES_INTENT.equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return createSavingsPlan(intent, session);
        } else if (NO_INTENT.equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return getAskResponse(SAVINGS_PLAN, "Nenne einen der Parameter, die du aendern willst " +
                    "oder beginne neu, indem du \"Neu\" sagst.");
        } else if (SAVINGS_PLAN_CHANGE_PARAMETER_INTENT.equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return askAgainForParameter(intent, session);
        } else if (SAVINGS_PLAN_NEW_INTENT.equals(intentName) && context != null && context.equals("SavingsPlan")) {
            session.getAttributes().clear();
            session.setAttribute(CONTEXT, "SavingsPlan");
            return askForBasicAmount();
        } else if (STOP_INTENT.equals(intentName) && context != null && context.equals("SavingsPlan")) {
            return getResponse("Stop", "");
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse askForBasicAmount() {
        return getAskResponse(SAVINGS_PLAN,
                "Was moechtest du als Grundbetrag anlegen?");
    }

    private SpeechletResponse askForDuration() {
        //TODO better use duration?
        return getAskResponse(SAVINGS_PLAN, "Wie viele Jahre moechtest du das Geld anlegen?");
    }

    private SpeechletResponse askForMonthlyPaymentAmount() {
        return getAskResponse(SAVINGS_PLAN, "Welchen Geldbetrag moechtest du monatlich investieren?");
    }

    private SpeechletResponse saveAmountAndContinue(Intent intent, Session session) {
        //Amount may either be basic amount or monthly payment amount, so we
        //have to check first, if basic amount has already been set in the session.
        //(Whole process must be sequential)
        Map<String, Slot> slots = intent.getSlots();
        Slot amountSlot = slots.get(AMOUNT_KEY);
        if (!session.getAttributes().containsKey(AMOUNT_KEY)) {
            //In this case amount slot contains basic amount
            String basicAmount = amountSlot.getValue();
            LOGGER.info("BasicAmount: " + basicAmount);
            session.setAttribute(AMOUNT_KEY, basicAmount);
            return askForDuration();
        } else {
            //In this case amount slot contains monthly payment amount
            String monthlyPayment = amountSlot.getValue();
            LOGGER.info("MonthlyPayment: " + monthlyPayment);
            session.setAttribute(MONTHLY_PAYMENT_KEY, monthlyPayment);
            return getCalculatedSavingsPlanInfo(session);
        }
    }

    private SpeechletResponse saveDurationAndContinue(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();
        String numberOfYears = slots.get(NUMBER_OF_YEARS_KEY).getValue();
        LOGGER.info("NumberOfYears: " + numberOfYears);
        session.setAttribute(NUMBER_OF_YEARS_KEY, numberOfYears);
        if (session.getAttributes().containsKey(MONTHLY_PAYMENT_KEY)) {
            return getCalculatedSavingsPlanInfo(session);
        }
        return askForMonthlyPaymentAmount();
    }

    private SpeechletResponse askAgainForParameter(Intent intent, Session session) {
        Map<String, Slot> slots = intent.getSlots();
        String parameter = slots.get(SAVINGS_PLAN_PARAMETER_KEY).getValue();
        LOGGER.info("Parameter to change: " + parameter);
        session.setAttribute(SAVINGS_PLAN_PARAMETER_KEY, parameter);
        if (parameter.equals("grundbetrag")) {
            session.removeAttribute(AMOUNT_KEY);
            return askForBasicAmount();
        }
        if (parameter.equals("laufzeit")) {
            session.removeAttribute(NUMBER_OF_YEARS_KEY);
            return askForDuration();
        }
        if (parameter.equals("monatliche Einzahlung")) {
            session.removeAttribute(MONTHLY_PAYMENT_KEY);
            return askForMonthlyPaymentAmount();
        } else return getAskResponse(SAVINGS_PLAN,
                "Das habe ich leider nicht verstanden. Wiederhole deine Eingabe.");
    }

    private SpeechletResponse getCalculatedSavingsPlanInfo(Session session) {
        //Assume we have all parameters now. Get them from the session attributes.

        String grundbetragString = (String) session.getAttribute(AMOUNT_KEY);
        String einzahlungMonatString = (String) session.getAttribute(MONTHLY_PAYMENT_KEY);
        String anzahlJahreString = (String) session.getAttribute(NUMBER_OF_YEARS_KEY);

        String calculationString = calculateSavings(grundbetragString, einzahlungMonatString, anzahlJahreString);

        SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml("<speak>Bei einem Zinssatz von zwei Prozent waere der Gesamtsparbetrag am Ende " +
                "des Zeitraums insgesamt <say-as interpret-as=\"number\">" + calculationString
                + "</say-as> Euro. Soll ich diesen Sparplan fuer dich anlegen?</speak>");

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse createSavingsPlan(Intent intent, Session session) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        String grundbetrag = (String) session.getAttribute(AMOUNT_KEY);
        String monatlicheZahlung = (String) session.getAttribute(MONTHLY_PAYMENT_KEY);
        createSavingsPlanOneOffPayment(grundbetrag);
        StandingOrder so = createSavingsPlanStandingOrder(monatlicheZahlung);
        speech.setText("Okay! Ich habe den Sparplan angelegt. Der Grundbetrag von " + grundbetrag + " Euro wird deinem Sparkonto " +
                "gutgeschrieben. Die erste regelmaeßige Einzahlung von " + monatlicheZahlung + " Euro erfolgt am "
                + so.getFirstExecutionSpeechString() + ".");
        return SpeechletResponse.newTellResponse(speech);
    }

    private void createSavingsPlanOneOffPayment(String betrag) {
        Number amount = Integer.parseInt(betrag);
        Date now = new Date();
        String valueDate = formatDate(now, "yyyy-MM-dd");
        TransactionAPI.createTransaction(amount, SOURCE_ACCOUNT, SAVINGS_ACCOUNT, valueDate, DESCRIPTION_ONE_OFF_PAYMENT, "Hans", "Helga");
    }

    private StandingOrder createSavingsPlanStandingOrder(String monatlicheZahlung) {
        Number amount = Integer.parseInt(monatlicheZahlung);
        String firstExecution = formatDate(getFirstExecutionDate(), "yyyy-MM-dd");
        LOGGER.debug("FirstExecution: " + firstExecution);
        return AccountAPI.createStandingOrderForAccount(STANDING_ORDER_ACCOUNT, PAYEE, amount, SAVINGS_ACCOUNT, firstExecution, StandingOrder.ExecutionRate.MONTHLY, DESCRIPTION_SAVINGS_PLAN);
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