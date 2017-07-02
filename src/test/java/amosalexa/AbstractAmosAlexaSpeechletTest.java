package amosalexa;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class AbstractAmosAlexaSpeechletTest {

    private Session session;
    private String sessionId;
    // FIXME: Get the current account AccountNumber from the session
    protected static final String ACCOUNT_NUMBER = "9999999999";

    /************************************
     *          Helper methods          *
     ************************************/

    protected String testIntentMatches(String intent, String... params) throws IOException, NoSuchFieldException, IllegalAccessException {
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

    protected void testIntent(String intent, String... params) throws IOException, NoSuchFieldException, IllegalAccessException {
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

    protected String performIntent(String intent, String... params) throws IOException, NoSuchFieldException, IllegalAccessException {
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

    protected String getOutputSpeechText(OutputSpeech outputSpeech) {
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

    protected void newSession() {
        Session.Builder builder = Session.builder();
        sessionId = "SessionId." + UUID.randomUUID();
        builder.withSessionId(sessionId);
        session = builder.build();
    }

    protected SpeechletRequestEnvelope<IntentRequest> getEnvelope(String intent, String... slots) throws IOException, NoSuchFieldException, IllegalAccessException {
        SpeechletRequestEnvelope<IntentRequest> envelope = (SpeechletRequestEnvelope<IntentRequest>) SpeechletRequestEnvelope.fromJson(buildJson(intent, slots));

        // Set session via reflection

        Field f1 = envelope.getClass().getDeclaredField("session");
        f1.setAccessible(true);
        f1.set(envelope, session);
        return envelope;
    }

    protected boolean isExpected(SpeechletResponse response, String expected) {
        return false;
    }

    protected String buildJson(String intent, String... slots) {
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
