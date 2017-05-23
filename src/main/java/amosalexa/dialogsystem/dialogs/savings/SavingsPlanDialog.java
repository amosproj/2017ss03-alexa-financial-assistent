package amosalexa.dialogsystem.dialogs.savings;

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
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import model.banking.account.StandingOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Map;

public class SavingsPlanDialog implements DialogHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SavingsPlanDialog.class);

    private static final String GRUNDBETRAG_KEY = "Grundbetrag";

    private static final String ANZAHL_JAHRE_KEY = "AnzahlJahre";

    private static final String EINZAHLUNG_MONAT_KEY = "EinzahlungMonat";

    @Override
    public String getDialogName() {
        return "SavingsPlan";
    }

    @Override
    public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException {
        String intentName = intent.getName();
        LOGGER.info("Intent Name: " + intentName);

        if ("SavingsPlanIntent".equals(intentName)) {
            return askForSavingsParameter(intent, storage);
        } else if ("AMAZON.YesIntent".equals(intentName)) {
            return createSavingsPlanStandingOrder(intent, storage);
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            return cancelAction();
        } else {
            throw new SpeechletException("Unhandled intent: " + intentName);
        }
    }

    private SpeechletResponse askForSavingsParameter(Intent intent, SessionStorage.Storage storage) {
        Map<String, Slot> slots = intent.getSlots();
        Slot grundbetragSlot = slots.get(GRUNDBETRAG_KEY);
        Slot anzahlJahreSlot = slots.get(ANZAHL_JAHRE_KEY);
        Slot monatlicheEinzahlungSlot = slots.get(EINZAHLUNG_MONAT_KEY);

        String speechText, repromptText;

        LOGGER.info("Grundbetrag: " + grundbetragSlot.getValue());
        LOGGER.info("Jahre: " + anzahlJahreSlot.getValue());
        LOGGER.info("monatliche Einzahlung: " + monatlicheEinzahlungSlot.getValue());
        //LOGGER.info("Session Before: " + storage.getAttributes());

        if (grundbetragSlot.getValue() != null) {
            storage.put(GRUNDBETRAG_KEY, grundbetragSlot.getValue());
        }
        if (anzahlJahreSlot.getValue() != null) {
            storage.put(ANZAHL_JAHRE_KEY, anzahlJahreSlot.getValue());
        }
        if (monatlicheEinzahlungSlot.getValue() != null) {
            storage.put(EINZAHLUNG_MONAT_KEY, monatlicheEinzahlungSlot.getValue());
        }

        if (grundbetragSlot.getValue() == null && !storage.containsKey(GRUNDBETRAG_KEY)) {
            speechText = "Was moechtest du als Grundbetrag anlegen?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        if (anzahlJahreSlot.getValue() == null && !storage.containsKey(ANZAHL_JAHRE_KEY)) {
            speechText = "Wie viele Jahre moechtest du das Geld anlegen?";
            //TODO better use duration?
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        if (monatlicheEinzahlungSlot.getValue() == null && !storage.containsKey(EINZAHLUNG_MONAT_KEY)) {
            if (grundbetragSlot.getValue() == null && !storage.containsKey(GRUNDBETRAG_KEY)) {
                speechText = "Du musst zuerst einen Grundbetrag angeben.";
                repromptText = speechText;
                return getSpeechletResponse(speechText, repromptText, true);
            }
            speechText = "Wie viel Geld moechtest du monatlich investieren?";
            repromptText = speechText;
            return getSpeechletResponse(speechText, repromptText, true);
        }

        String grundbetragString = (String) storage.get(GRUNDBETRAG_KEY);
        String einzahlungMonatString = (String) storage.get(EINZAHLUNG_MONAT_KEY);
        String anzahlJahreString = (String) storage.get(ANZAHL_JAHRE_KEY);

        String calculationString = calculateSavings(grundbetragString, einzahlungMonatString, anzahlJahreString);

        SsmlOutputSpeech speech = new SsmlOutputSpeech();
        speech.setSsml("<speak>Bei einem Zinssatz von zwei Prozent waere der Gesamtsparbetrag am Ende " +
                "des Zeitraums insgesamt <say-as interpret-as=\"number\">" + calculateSavings(grundbetragString, einzahlungMonatString, anzahlJahreString)
                + "</say-as> Euro. Soll ich diesen Sparplan fuer dich anlegen?</speak>");

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        //LOGGER.info("Session Afterwards: " + session.getAttributes());
        LOGGER.info("Storage afterwards: " + storage);

        return SpeechletResponse.newAskResponse(speech, reprompt);
    }

    private SpeechletResponse createSavingsPlanStandingOrder(Intent intent, SessionStorage.Storage storage) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        createSavingsPlanStandingOrder((String) storage.get(EINZAHLUNG_MONAT_KEY));
        //TODO replace date
        speech.setText("Okay! Ich habe den Sparplan angelegt. Die erste Zahlung auf dein Sparkonto erfolgt am 01.06.2017");
        return SpeechletResponse.newTellResponse(speech);
    }

    private void createSavingsPlanStandingOrder(String monatlicheZahlung) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("payee", "Max Mustermann");
            jsonObject.put("amount", monatlicheZahlung);
            //TODO Hard coded savings account?
            jsonObject.put("destinationAccount", "DE39100000007777777777");
            jsonObject.put("firstExecution", "2017-06-01");
            jsonObject.put("executionRate", "MONTHLY");
            jsonObject.put("description", "Savings Plan");
        } catch (JSONException e) {
            LOGGER.error(e.getMessage());
        }
        BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();
        bankingRESTClient.postBankingModelObject("/api/v1_0/accounts/9999999999/standingorders", jsonObject.toString(), StandingOrder.class);
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
        speech.setText("OK tschüss!");
        return SpeechletResponse.newTellResponse(speech);
    }

    private String calculateSavings(String grundbetrag, String monatlicheEinzahlung, String jahre) {
        DecimalFormat df = new DecimalFormat("#.##");
        double gb = Double.valueOf(grundbetrag);
        //TODO m never used?
        double m = Double.valueOf(monatlicheEinzahlung);
        double j = Double.valueOf(jahre);
        double zins = 2;
        double result;
        double klammer;
        klammer = 1 + zins / 100;
        result = gb * Math.pow(klammer, j);
        String strResult = String.valueOf(df.format(result));
        return strResult;
    }
}
