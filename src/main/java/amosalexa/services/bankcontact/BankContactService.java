package amosalexa.services.bankcontact;


import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.ui.*;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.walkercrou.places.Place;

import java.util.HashSet;
import java.util.Set;

public class BankContactService implements SpeechService{

    private static final Logger log = LoggerFactory.getLogger(BankContactService.class);

    /**
     * This is the default title that this skill will be using for cards.
     */
    private static final String BANK_CONTACT_CARD = "Bank Kontakt Informationen";

    /**
     * Slots with different bank names
     */
    private static final String SLOT_NAME_BANK = "BankNameSlots";

    /**
     * The permissions that this skill relies on for retrieving addresses. If the consent token isn't
     * available or invalid, we will request the user to grant us the following permission
     * via a permission card.
     *
     * Another Possible value if you only want permissions for the country and postal code is:
     * read::alexa:device:all:address:country_and_postal_code
     * Be sure to check your permissions settings for your skill on https://developer.amazon.com/
     */
    private static final String ALL_ADDRESS_PERMISSION = "read::alexa:device:all:address";

    private static final String WELCOME_TEXT = "Welcome to the Sample Device Address API Skill! What do you want to ask?";
    private static final String HELP_TEXT = "You can use this skill by asking something like: whats my address";
    private static final String UNHANDLED_TEXT = "This is unsupported. Please ask something else.";
    private static final String ERROR_TEXT = "There was an error with the skill. Please try again.";

    private static final String BANK_CONTACT_INTENT = "BankContactInformation";

    public BankContactService(SpeechletSubject speechletSubject){
        subscribe(speechletSubject);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest intentRequest = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        log.info("onIntent requestId={}, sessionId={}", intentRequest.getRequestId(),
                session.getSessionId());

        Intent intent = intentRequest.getIntent();
        String intentName = getIntentName(intent);

        log.info("Intent received: {}", intentName);

        switch(intentName) {

            case "BankContactInformation":

                /**
                 * device address can only be requested from a real device
                 * - assuming we received the device address - creating dummy address
                 */


                /*
                String consentToken = session.getUser().getPermissions().getConsentToken();

                if (consentToken == null) {
                    log.info("The user hasn't authorized the skill. Sending a permissions card.");
                    return getPermissionsResponse();
                }

                try {
                    SystemState systemState = getSystemState(requestEnvelope.getContext());

                    String deviceId = systemState.getDevice().getDeviceId();
                    String apiEndpoint = systemState.getApiEndpoint();

                    AlexaDeviceAddressClient alexaDeviceAddressClient = new AlexaDeviceAddressClient(
                            deviceId, consentToken, apiEndpoint);

                    Address addressObject = alexaDeviceAddressClient.getFullAddress();


                    if (addressObject == null) {
                        return getAskResponse(BANK_CONTACT_CARD, ERROR_TEXT);
                    }

                    return getAddressResponse(
                            addressObject.getAddressLine1(),
                            addressObject.getStateOrRegion(),
                            addressObject.getPostalCode());
                } catch (UnauthorizedException e) {
                    return getPermissionsResponse();
                } catch (DeviceAddressClientException e) {
                    log.error("Device Address Client failed to successfully return the address.", e);
                    return getAskResponse(BANK_CONTACT_CARD, ERROR_TEXT);
                }
                // This is one of the many Amazon built in intents.
                // Refer to the following for a list of all available built in intents:
                // https://developer.amazon.com/public/solutions/alexa/alexa-skills-kit/docs/built-in-intent-ref/standard-intents

                */

                String slotValue = requestEnvelope.getRequest().getIntent().getSlot(SLOT_NAME_BANK).getValue();

                if(slotValue.isEmpty()){
                    slotValue = "Deutsche Bank";
                }
                log.warn(getClass().getCanonicalName() + "Slot Value: " + slotValue);

                Address dummyAddress = new Address();
                LatLng deviceLocation = GeoCoder.getLatLng(dummyAddress);

                log.warn(getClass().getCanonicalName() + "Device Location : " + deviceLocation);
                Place bank = PlaceFinder.findNearbyPlace(deviceLocation, slotValue);

                log.warn(getClass().getCanonicalName() + " Place : " + bank.getName());

                if(bank.getName().isEmpty()){
                   // getAskResponse(BANK_CONTACT_CARD, HELP_TEXT);
                } else {
                   return getBankContactResponse(bank);
                }
            case "AMAZON.HelpIntent":
                return getAskResponse(BANK_CONTACT_CARD, HELP_TEXT);
            default:
                return getAskResponse(BANK_CONTACT_CARD, UNHANDLED_TEXT);
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the GetAddress intent.
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getBankContactResponse(Place deutscheBank) {
        String speechText = deutscheBank.getName() + " hat die Telefonnummer: " + deutscheBank.getPhoneNumber();

        SimpleCard card = getSimpleCard(BANK_CONTACT_CARD, speechText);

        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the GetAddress intent.
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getAddressResponse(String streetName, String state, String zipCode) {
        String speechText = "Your address is " + streetName + " " + state + ", " + zipCode;

        SimpleCard card = getSimpleCard(BANK_CONTACT_CARD, speechText);

        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Helper method that will get the intent name from a provided Intent object. If a name does not
     * exist then this method will return null.
     * @param intent intent object provided from a skill request.
     * @return intent name or null.
     */
    private String getIntentName(Intent intent) {
        return (intent != null) ? intent.getName() : null;
    }

    /**
     * Helper method that retrieves the system state from the request context.
     * @param context request context.
     * @return SystemState the systemState
     */
    private SystemState getSystemState(Context context) {
        return context.getState(SystemInterface.class, SystemState.class);
    }


    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, BANK_CONTACT_INTENT);
    }

    /**
     * Creates a {@code SpeechletResponse} for permission requests.
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getPermissionsResponse() {
        String speechText = "You have not given this skill permissions to access your address. " +
                "Please give this skill permissions to access your address.";

        // Create the permission card content.
        // The differences between a permissions card and a simple card is that the
        // permissions card includes additional indicators for a user to enable permissions if needed.
        AskForPermissionsConsentCard card = new AskForPermissionsConsentCard();
        card.setTitle(BANK_CONTACT_CARD);

        Set<String> permissions = new HashSet<>();
        permissions.add(ALL_ADDRESS_PERMISSION);
        card.setPermissions(permissions);

        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Helper method for retrieving an Ask response with a simple card and reprompt included.
     * @param cardTitle Title of the card that you want displayed.
     * @param speechText speech text that will be spoken to the user.
     * @return the resulting card and speech text.
     */
    private SpeechletResponse getAskResponse(String cardTitle, String speechText) {
        SimpleCard card = getSimpleCard(cardTitle, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        Reprompt reprompt = getReprompt(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    /**
     * Helper method that returns a reprompt object. This is used in Ask responses where you want
     * the user to be able to respond to your speech.
     * @param outputSpeech The OutputSpeech object that will be said once and repeated if necessary.
     * @return Reprompt instance.
     */
    private Reprompt getReprompt(OutputSpeech outputSpeech) {
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(outputSpeech);

        return reprompt;
    }

    /**
     * Helper method for retrieving an OutputSpeech object when given a string of TTS.
     * @param speechText the text that should be spoken out to the user.
     * @return an instance of SpeechOutput.
     */
    private PlainTextOutputSpeech getPlainTextOutputSpeech(String speechText) {
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return speech;
    }

    /**
     * Helper method that creates a card object.
     * @param title title of the card
     * @param content body of the card
     * @return SimpleCard the display card to be sent along with the voice response.
     */
    private SimpleCard getSimpleCard(String title, String content) {
        SimpleCard card = new SimpleCard();
        card.setTitle(title);
        card.setContent(content);

        return card;
    }

}
