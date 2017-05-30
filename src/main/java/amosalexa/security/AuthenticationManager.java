package amosalexa.security;


import amosalexa.SessionStorage;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationManager extends AbstractSpeechService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationManager.class);

    /**
     *
     */
    private static final String secretPIN = "1234";

    /**
     * intents
     */
    private final static String AUTHENTICATION_PIN_INTENT = "AuthenticationPIN";

    /**
     * slots
     */
    private static final String SLOT_AUTH_PIN = "AuthenticationPINSlots";


    /**
     * session attribute
     */
    private static final String AUTHENTICATION_ATTRIBUTE = "authentication";
    private static final String AUTHENTICATION_SESSION = "authentication";


    /**
     * cards
     */
    private final static String AUTHENTICATION_CARD = "PIN-Authentisierung";

    /**
     * Messages
     */
    private final static String INCORRECT_MESSAGE = "Dein PIN war nicht korrekt! Sag deinen PIN noch einmal.";
    private final static String CORRECT_MESSAGE = "Akzeptiert!";
    private final static String AUTH_REQ_MESSAGE = "Authentisiere dich bitte mit deinem PIN";


    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        Intent intent = requestEnvelope.getRequest().getIntent();

        String userPIN = intent.getSlot(SLOT_AUTH_PIN) != null ? intent.getSlot(SLOT_AUTH_PIN).getValue() : "";

        if (!userPIN.equals(secretPIN)) {
            log.info("PIN wrong! User: " + userPIN + " Actual: " + secretPIN);
            return getAskResponse(AUTHENTICATION_CARD, INCORRECT_MESSAGE);
        }

        log.info("PIN was correct");
        SessionStorage.getInstance().putObject(AUTHENTICATION_SESSION, AUTHENTICATION_ATTRIBUTE, Boolean.TRUE);

        return getResponse(AUTHENTICATION_CARD, CORRECT_MESSAGE);
    }


    public static SpeechletResponse authenticate() {

        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(AUTH_REQ_MESSAGE);

        return SpeechletResponse.newTellResponse(speech);
    }


    public static boolean isAuthenticated() {

        SessionStorage sessionStorage = SessionStorage.getInstance();
        Boolean authenticated = (Boolean) sessionStorage.getObject(AUTHENTICATION_SESSION, AUTHENTICATION_ATTRIBUTE);

        return authenticated == null ? false : authenticated;
    }

    public static void revokeAuthentication(){
        SessionStorage.getInstance().putObject(AUTHENTICATION_SESSION, AUTHENTICATION_ATTRIBUTE, Boolean.FALSE);
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, AUTHENTICATION_PIN_INTENT);
    }

    public AuthenticationManager(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

}
