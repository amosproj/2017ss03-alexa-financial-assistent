package amosalexa;

import amosalexa.server.Launcher;
import amosalexa.services.financing.AffordabilityService;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import model.banking.StandingOrder;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Server;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class AmosAlexaSpeechletTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmosAlexaSpeechletTest.class);
    // FIXME: Get the current account AccountNumber from the session
    private static final String ACCOUNT_NUMBER = "9999999999";
    private static Integer savingsPlanTestStandingOrderId;
    private Session session;
    private String sessionId;

    /*************************************
     *          Testing section          *
     *************************************/

    // Needed to ensure that the account balance is sufficient
    @BeforeClass
    public static void setUpAccount() {
        Calendar cal = Calendar.getInstance();
        Date time = cal.getTime();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String openingDate = formatter.format(time);

        AccountAPI.createAccount("9999999999", 1250000, openingDate);
    }


    @Test
    public void bankContactTelephoneNumberTest() throws Exception {
        newSession();

        // pretend local environment
        Launcher.server = new Server();
        Launcher.server.start();

        testIntentMatches("BankTelephone","Sparkasse Nürnberg - Geldautomat hat die Telefonnummer 0911 2301000");

        Launcher.server.stop();
    }

    @Test
    public void affordabilityTest() throws Exception {

        // sometimes there is a amazon api problem - nothing we could handle -- comment out test if failing continues

        ArrayList<String> buyAskAnswers = new ArrayList<String>() {{
            add("Produkt a (.*) kostet (.*) Produkt b (.*) kostet (.*) Produkt c (.*) kostet (.*) Möchtest du ein Produkt kaufen");
            add("Ein Fehler ist aufgetreten. " + AffordabilityService.NO_RESULTS);
            add("Ein Fehler ist aufgetreten. " + AffordabilityService.TOO_FEW_RESULTS);
        }};

        ArrayList<String> productSelectionAskAnswers = new ArrayList<String>() {{
            add(AffordabilityService.SELECTION_ASK);
            add(AffordabilityService.ERROR);
        }};

        newSession();
        testIntentMatches("AffordProduct","ProductKeyword:Samsung",  StringUtils.join(buyAskAnswers, "|"));
        testIntentMatches("AMAZON.YesIntent", StringUtils.join(productSelectionAskAnswers, "|"));
        testIntentMatches("AffordProduct", "ProductSelection:a", "Produkt a (.*)  Willst du das Produkt in den Warenkorb legen");
        testIntent("AMAZON.YesIntent", AffordabilityService.CART_ACK);

        newSession();
        testIntentMatches("AffordProduct","ProductKeyword:Samsung",  StringUtils.join(buyAskAnswers, "|"));
        testIntentMatches("AMAZON.YesIntent", StringUtils.join(productSelectionAskAnswers, "|"));
        testIntentMatches("AffordProduct", "ProductSelection:a", "Produkt a (.*)  Willst du das Produkt in den Warenkorb legen");
        testIntent("AMAZON.NoIntent", AffordabilityService.BYE);

        newSession();
        testIntentMatches("AffordProduct","ProductKeyword:Samsung",  StringUtils.join(buyAskAnswers, "|"));
        testIntent("AMAZON.NoIntent", AffordabilityService.BYE);

        newSession();
        testIntentMatches("AffordProduct","ProductKeyword:Samsung",  StringUtils.join(buyAskAnswers, "|"));
        testIntentMatches("AMAZON.YesIntent", StringUtils.join(productSelectionAskAnswers, "|"));
        testIntentMatches("AffordProduct", "ProductSelection:randomtext", StringUtils.join(productSelectionAskAnswers, "|"));
    }

    @Test
    public void bankContactAddressTest() throws Exception {

        // pretend local environment
        Launcher.server = new Server();
        Launcher.server.start();

        newSession();

        testIntentMatches("BankAddress", "Sparkasse Nürnberg - Geschäftsstelle hat die Adresse: Lorenzer Pl. 12, 90402 Nürnberg, Germany");
        testIntentMatches("BankAddress", "BankNameSlots:Deutsche Bank", "Deutsche Bank Filiale hat die Adresse: Landgrabenstraße 144, 90459 Nürnberg, Germany");

        Launcher.server.stop();
    }

    @Test
    public void bankContactOpeningHoursTest() throws Exception {

        // pretend local environment
        Launcher.server = new Server();
        Launcher.server.start();

        newSession();
        testIntentMatches("BankOpeningHours", "Sparkasse Nürnberg - Geschäftsstelle hat am (.*)");
        testIntentMatches("BankOpeningHours", "OpeningHoursDate:2017-06-13", "Sparkasse Nürnberg - Geschäftsstelle Geöffnet am Dienstag von (.*) bis (.*)");

        Launcher.server.stop();
    }

    @Test
    public void bankAccountTransactionIntentTest() throws IllegalAccessException, NoSuchFieldException, IOException {
        newSession();

        ArrayList<String> possibleAnswers = new ArrayList<String>() {{
            add("Du hast keine Transaktionen in deinem Konto");
            add("Du hast (.*) Transaktionen. Nummer (.*) Von deinem Konto auf das Konto (.*) in Höhe von €(.*)\n" +
                    "Nummer (.*) Von deinem Konto auf das Konto (.*) in Höhe von €(.*)\n" +
                    "Nummer (.*) Von deinem Konto auf das Konto (.*) in Höhe von €(.*)\n" +
                    " Möchtest du weitere Transaktionen hören");
            add("Möchtest du weitere Transaktionen hören");
        }};

        testIntentMatches("AccountInformation", "AccountInformationSlots:überweisungen", StringUtils.join(possibleAnswers, "|"));

        ArrayList<String> possibleAnswersYES = new ArrayList<String>() {{
            add("Du hast keine Überweisungen in deinem Konto");
            add("Nummer (.*) Von deinem Konto auf das Konto (.*) in Höhe von €(.*)\n" +
                    " Möchtest du weitere Transaktionen hören");
        }};

        testIntentMatches(
                "AMAZON.YesIntent", StringUtils.join(possibleAnswersYES, "|"));
    }

    @Test
    public void bankAccountInformationIntentTest() throws IllegalAccessException, NoSuchFieldException, IOException {
        newSession();
        testIntentMatches("AccountInformation", "AccountInformationSlots:zinssatz", "Dein zinssatz ist aktuell (.*)");
        testIntentMatches("AccountInformation", "AccountInformationSlots:kontostand", "Dein kontostand beträgt €(.*)");
        testIntentMatches("AccountInformation", "AccountInformationSlots:eröffnungsdatum", "Dein eröffnungsdatum war (.*)");
        testIntentMatches("AccountInformation", "AccountInformationSlots:kreditlimit", "Dein kreditlimit beträgt €(.*)");
        testIntentMatches("AccountInformation", "AccountInformationSlots:kreditkartenlimit", "Dein kreditkartenlimit beträgt €(.*)");
        testIntentMatches("AccountInformation", "AccountInformationSlots:kontonummer", "Deine kontonummer lautet (.*)");
        testIntentMatches("AccountInformation", "AccountInformationSlots:abhebegebühr", "Deine abhebegebühr beträgt (.*)");
        testIntentMatches("AccountInformation", "AccountInformationSlots:iban", "Deine iban lautet (.*)");
    }

    @Test
    public void blockCardIntentTest() throws Exception {
        newSession();

        testIntent(
                "BlockCardIntent", "BankCardNumber:123",
                "Möchten Sie die Karte 123 wirklich sperren?");

        testIntent(
                "AMAZON.YesIntent",
                "Karte 123 wurde gesperrt.");
    }

    @Test
    public void bankTransferIntentTest() throws Throwable {
        newSession();

        testIntentMatches("BankTransferIntent", "name:anne", "amount:2",
                "Aktuell betraegt dein Kontostand (.*) Euro\\. Bist du sicher, dass du 2 Euro an anne ueberweisen willst\\?");

        testIntentMatches("AMAZON.YesIntent",
                "Ok, (.*) Euro wurden an anne ueberwiesen\\. Dein neuer Kontostand betraegt (.*) Euro\\.");


    }

    @Test
    public void standingOrdersInfoTest() throws IllegalAccessException, NoSuchFieldException, IOException {
        newSession();

        ArrayList<String> possibleAnswers = new ArrayList<String>() {{
            add("Keine Dauerauftraege vorhanden.");
            add("Du hast momentan einen Dauerauftrag. " +
                    "Dauerauftrag Nummer \\d+: Ueberweise monatlich \\d+\\.\\d+ Euro auf dein Sparkonto.(.*)");
            add("Du hast momentan (.*) Dauerauftraege. " +
                    "Dauerauftrag Nummer \\d+: Ueberweise monatlich \\d+\\.\\d+ Euro auf dein Sparkonto.(.*)");
        }};
        testIntentMatches(
                "StandingOrdersInfoIntent", StringUtils.join(possibleAnswers, "|"));
        testIntentMatches(
                "AMAZON.YesIntent",
                "Dauerauftrag Nummer \\d+: Ueberweise monatlich \\d+\\.\\d+ Euro auf dein Sparkonto.(.*)");
        testIntentMatches(
                "AMAZON.NoIntent", "Okay, tschuess!");
    }

    @Test
    public void savingsPlanTest() throws Exception {
        newSession();

        testIntent("SavingsPlanIntroIntent",
                "Was moechtest du als Grundbetrag anlegen?"
        );
        testIntent("SavingsPlanAmountIntent", "Betrag:1500",
                "Wie viele Jahre moechtest du das Geld anlegen?"
        );
        testIntent("SavingsPlanNumberOfYearsIntent", "AnzahlJahre:2",
                "Welchen Geldbetrag moechtest du monatlich investieren?"
        );
        testIntentMatches("SavingsPlanAmountIntent", "Betrag:150",
                "Bei einem Zinssatz von zwei Prozent waere der Gesamtsparbetrag am Ende des Zeitraums insgesamt (.*) Euro\\. Soll ich diesen Sparplan fuer dich anlegen\\?"
        );

        //Calculate what first payment date of the savings plan should be (depending on today´s date)
        Calendar calendar = Calendar.getInstance();
        String nextPayin = String.format("01.%02d.%d", calendar.get(Calendar.MONTH) + 2, calendar.get(Calendar.YEAR));

        testIntent(
                "AMAZON.YesIntent",
                "Okay! Ich habe den Sparplan angelegt. Der Grundbetrag von 1500 Euro wird deinem Sparkonto gutgeschrieben. Die erste regelmaeßige Einzahlung von 150 Euro erfolgt am " + nextPayin + ".");

        Collection<StandingOrder> allStandingOrders = AccountAPI.getStandingOrdersForAccount(ACCOUNT_NUMBER);
        final Comparator<StandingOrder> comp = Comparator.comparingInt(s -> s.getStandingOrderId().intValue());
        int latestStandingOrderId = allStandingOrders.stream().max(comp).get().getStandingOrderId().intValue();
        LOGGER.info("Latest standing order ID: " + latestStandingOrderId);
        savingsPlanTestStandingOrderId = latestStandingOrderId;

        // We need to start a new session here because the dialog ends after the YesIntent
        newSession();

        testIntent(
                "StandingOrdersDeleteIntent",
                "Number:" + latestStandingOrderId, "Moechtest du den Dauerauftrag mit der Nummer "
                        + latestStandingOrderId + " wirklich loeschen?");

        testIntent(
                "AMAZON.YesIntent",
                "Dauerauftrag Nummer " + latestStandingOrderId + " wurde geloescht.");
    }

    @Test
    public void replacementCardDialogTest() throws Exception {
        newSession();

        ArrayList<String> possibleAnswers = new ArrayList<String>() {{
            add("Bestellung einer Ersatzkarte. Es wurden folgende Karten gefunden: (.*)");
            add("Es wurden keine Kredit- oder EC-Karten gefunden.");
        }};

        String response = testIntentMatches("ReplacementCardIntent", StringUtils.join(possibleAnswers, "|"));

        if (response.equals("Es wurden keine Kredit- oder EC-Karten gefunden.")) {
            //Fallback
            return;
        }

        Pattern p = Pattern.compile("karte mit den Endziffern ([0-9]+)\\.");
        Matcher m = p.matcher(response);

        if (m.find()) {
            String endDigits = m.group(1);
            testIntent("FourDigitNumberIntent", "FourDigits:" + endDigits,
                    "Wurde die Karte gesperrt oder wurde sie beschädigt?");

            testIntent("ReplacementCardReasonIntent", "ReplacementReason:beschaedigt",
                    "Soll ein Ersatz für die beschädigte Karte mit den Endziffern " + endDigits + " bestellt werden?");

            testIntent(
                    "AMAZON.YesIntent",
                    "Okay, eine Ersatzkarte wurde bestellt.");
        } else {
            fail("Cannot find credit card.");
        }
    }

    @Test
    public void transferTemplatesTest() throws Exception {
        newSession();

        String response = performIntent("ListTransferTemplatesIntent");

        Pattern p = Pattern.compile("Vorlage ([0-9]+) vom ([0-9\\.]+): Überweise ([0-9\\.]+) Euro an ([a-zA-Z]+).");
        Matcher m = p.matcher(response);

        if (m.find()) {
            int templateId = Integer.parseInt(m.group(1));
            double amount = Double.parseDouble(m.group(3));

            testIntent("EditTransferTemplateIntent", "TemplateID:" + templateId, "NewAmount:" + (amount * 2),
                    "Möchtest du den Betrag von Vorlage " + templateId + " von " + amount + " auf " + (amount * 2) + " ändern?");

            testIntent(
                    "AMAZON.YesIntent",
                    "Vorlage wurde erfolgreich gespeichert.");

            response = performIntent("ListTransferTemplatesIntent");

            p = Pattern.compile("Vorlage ([0-9]+) vom ([0-9\\.]+): Überweise ([0-9\\.]+) Euro an ([a-zA-Z]+).");
            m = p.matcher(response);

            if (m.find()) {
                assertEquals(templateId, Integer.parseInt(m.group(1)));
                assert (Math.abs(amount * 2 - Double.parseDouble(m.group(3))) < 0.001);

                testIntent("EditTransferTemplateIntent", "TemplateID:" + templateId, "NewAmount:" + amount,
                        "Möchtest du den Betrag von Vorlage " + templateId + " von " + (amount * 2) + " auf " + amount + " ändern?");

                testIntent(
                        "AMAZON.YesIntent",
                        "Vorlage wurde erfolgreich gespeichert.");
            } else {
                fail("Cannot find transfer template.");
            }
        } else {
            fail("Cannot find transfer template.");
        }
    }

    @Test
    public void setBalanceLimitTest() throws Exception {
        newSession();

        testIntent("SetBalanceLimitIntent", "BalanceLimitAmount:100", "Möchtest du dein Kontolimit wirklich auf 100 Euro setzen?");
        testIntent("AMAZON.YesIntent", "Okay, dein Kontolimit wurde auf 100 Euro gesetzt.");

        newSession();

        testIntent("SetBalanceLimitIntent", "BalanceLimitAmount:100", "Möchtest du dein Kontolimit wirklich auf 100 Euro setzen?");
        testIntent("AMAZON.NoIntent", "");
    }

    @Test
    public void sameServiceTest() throws Exception {
        // pretend local environment
        Launcher.server = new Server();
        Launcher.server.start();

        newSession();

        testIntent("SetBalanceLimitIntent", "BalanceLimitAmount:100", "Möchtest du dein Kontolimit wirklich auf 100 Euro setzen?");

        // Switching to another Service should fail because the BalanceLimit dialog is currently active.
        testIntentMatches("BankAddress", "Ein Fehler ist aufgetreten.");

        newSession();

        // Switching to another Service works if a new session is started.
        testIntent("SetBalanceLimitIntent", "BalanceLimitAmount:100", "Möchtest du dein Kontolimit wirklich auf 100 Euro setzen?");
        newSession();
        testIntentMatches("BankAddress", "Sparkasse Nürnberg - Geschäftsstelle hat die Adresse: Lorenzer Pl. 12, 90402 Nürnberg, Germany");

        Launcher.server.stop();
    }

    /************************************
     *          Helper methods          *
     ************************************/

    private String testIntentMatches(String intent, String... params) throws IOException, NoSuchFieldException, IllegalAccessException {
        String[] slots = new String[params.length - 1];
        String expectedOutput = null;

        int i = 0;
        for (String param : params) {
            if (i == params.length - 1) {
                expectedOutput = param;
            } else {
                slots[i] = param;
                i++;
            }
        }

        String actual = performIntent(intent, slots);

        boolean condition = actual.matches(expectedOutput);

        assertTrue("[MATCHING]\nActual: " + actual + "\nExpected: " + expectedOutput, condition);

        return actual;
    }

    private void testIntent(String intent, String... params) throws IOException, NoSuchFieldException, IllegalAccessException {
        String[] slots = new String[params.length - 1];
        String expectedOutput = null;

        int i = 0;
        for (String param : params) {
            if (i == params.length - 1) {
                expectedOutput = param;
            } else {
                slots[i] = param;
                i++;
            }
        }

        //AmosAlexaSpeechlet amosAlexaSpeechlet = AmosAlexaSpeechlet.getInstance();
        //SpeechletResponse response = amosAlexaSpeechlet.onIntent(getEnvelope(intent, slots));
        assertEquals(expectedOutput, performIntent(intent, slots));
    }

    private String performIntent(String intent, String... params) throws IOException, NoSuchFieldException, IllegalAccessException {
        String[] slots = new String[params.length];

        int i = 0;
        for (String param : params) {
            slots[i] = param;
            i++;
        }

        AmosAlexaSpeechlet amosAlexaSpeechlet = AmosAlexaSpeechlet.getInstance();
        SpeechletResponse response = amosAlexaSpeechlet.onIntent(getEnvelope(intent, slots));
        return getOutputSpeechText(response.getOutputSpeech())
                .replaceAll("\\<.*?>", ""); // Remove all markup since it is not really relevant for our tests
    }

    private String getOutputSpeechText(OutputSpeech outputSpeech) {
        if (outputSpeech instanceof SsmlOutputSpeech) {
            SsmlOutputSpeech ssmlOutputSpeech = (SsmlOutputSpeech) outputSpeech;
            return ssmlOutputSpeech.getSsml();
        }
        if (outputSpeech instanceof PlainTextOutputSpeech) {
            PlainTextOutputSpeech plainTextOutputSpeech = (PlainTextOutputSpeech) outputSpeech;
            return plainTextOutputSpeech.getText();
        }

        return null;
    }

    private void newSession() {
        Session.Builder builder = Session.builder();
        sessionId = "SessionId." + UUID.randomUUID();
        builder.withSessionId(sessionId);
        session = builder.build();
    }

    private SpeechletRequestEnvelope<IntentRequest> getEnvelope(String intent, String... slots) throws IOException, NoSuchFieldException, IllegalAccessException {
        SpeechletRequestEnvelope<IntentRequest> envelope = (SpeechletRequestEnvelope<IntentRequest>) SpeechletRequestEnvelope.fromJson(buildJson(intent, slots));

        // Set session via reflection

        Field f1 = envelope.getClass().getDeclaredField("session");
        f1.setAccessible(true);
        f1.set(envelope, session);

        return envelope;
    }

    private boolean isExpected(SpeechletResponse response, String expected) {
        return false;
    }

    private String buildJson(String intent, String... slots) {
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
                "    \"sessionId\": \"" + sessionId + "\",\n" +
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