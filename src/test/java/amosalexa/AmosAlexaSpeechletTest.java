package amosalexa;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class AmosAlexaSpeechletTest {

    private Session session;
    private final String SESSION_ID = "SessionId.2682fed6-193f-48b3-afd7-c6185d075ddf";

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

        AmosAlexaSpeechlet amosAlexaSpeechlet = AmosAlexaSpeechlet.getInstance();
        SpeechletResponse response = amosAlexaSpeechlet.onIntent(getEnvelope(intent, slots));
        assertEquals(((PlainTextOutputSpeech) response.getOutputSpeech()).getText(), expectedOutput);
    }

    private void newSession() {
        Session.Builder builder = Session.builder();
        builder.withSessionId(SESSION_ID);
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

        for (String slot : slots) {
            String[] slotParts = slot.split(":");
            slotsJson.append("\"").append(slotParts[0]).append("\":");
            slotsJson.append("{");
            slotsJson.append("\"name\":\"").append(slotParts[0]).append("\",");
            slotsJson.append("\"value\":\"").append(slotParts[1]).append("\"");
            slotsJson.append("}");
        }

        String json = "{\n" +
                "  \"session\": {\n" +
                "    \"sessionId\": \"" + SESSION_ID + "\",\n" +
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