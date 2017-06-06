package amosalexa.services.bankcontact;


import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.DeviceAddressUtil;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.walkercrou.places.Place;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BankContactService extends AbstractSpeechService implements SpeechService {

    private static final Logger log = LoggerFactory.getLogger(BankContactService.class);

    /**
     * This is the default title that this skill will be using for cards.
     */
    private static final String BANK_CONTACT_CARD = "Bank Kontakt Informationen";

    /**
     * Slots with different bank names
     */
    private static final String SLOT_BANK_NAME = "BankNameSlots";

    /**
     * Slot for dates
     */
    private static final String SLOT_NAME_OPENING_HOURS_DATE = "OpeningHoursDate";

    /**
     * Address of device - for simulation only dummy values possible
     */
    public static Address deviceAddress = new Address();

    /**
     * bank slot fall back
     */
    private static final String SLOT_NAME_BANK_FALLBACK = "Sparkasse";

    /**
     * The permissions that this skill relies on for retrieving addresses. If the consent token isn't
     * available or invalid, we will request the user to grant us the following permission
     * via a permission card.
     * <p>
     * Another Possible value if you only want permissions for the country and postal code is:
     * read::alexa:device:all:address:country_and_postal_code
     * Be sure to check your permissions settings for your skill on https://developer.amazon.com/
     */
    private static final String ALL_ADDRESS_PERMISSION = "read::alexa:device:all:address";


    /**
     * default speech texts
     */
    private static final String HELP_TEXT = "Ich kann dich nicht verstehen. Was möchtest du über deine Bank erfahren?";
    private static final String UNHANDLED_TEXT = "Das weiß ich nicht. Bitte, frage etwas anderes.";
    private static final String ERROR_TEXT = "Es ist ein Fehler aufgetreten. Bitte, versuche es noch einmal.";
    private static final String NO_OPENING_HOURS = "Es konnten keine Öffnungszeiten gefunden werden!";


    /**
     * Intents
     */
    private static final String BANK_OPENING_HOURS_INTENT = "BankOpeningHours";
    private static final String BANK_ADDRESS_INTENT = "BankAddress";

    /**
     * Slots
     */
    private String slotBankNameValue;
    private String slotDateValue;

    /**
     * Permissions
     */
    public static String consentToken = null;

    public BankContactService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest intentRequest = requestEnvelope.getRequest();

        Intent intent = intentRequest.getIntent();
        String intentName = getIntentName(intent);

        // slot values
        slotBankNameValue = intent.getSlot(SLOT_BANK_NAME) != null ? intent.getSlot(SLOT_BANK_NAME).getValue() : null;
        slotDateValue = intent.getSlot(SLOT_NAME_OPENING_HOURS_DATE) != null ? intent.getSlot(SLOT_NAME_OPENING_HOURS_DATE).getValue() : null;

        if (slotBankNameValue == null) {
            slotBankNameValue = SLOT_NAME_BANK_FALLBACK;
        }


        log.info(getClass().getCanonicalName() + "Slot Value : " + slotBankNameValue + " ( " + SLOT_BANK_NAME + " ) ");
        log.info(getClass().getCanonicalName() + "Slot Value : " + slotDateValue + " ( " + SLOT_NAME_OPENING_HOURS_DATE + " ) ");


        /*
        // Authenticate
        if (!AuthenticationManager.isAuthenticated()) {
            return AuthenticationManager.authenticate();
        }

        AuthenticationManager.revokeAuthentication();
        */



        // try to get device address - needs user permission and real device
        DeviceAddressUtil.getDeviceAddress(requestEnvelope);

        // check permission for device address
        if (consentToken == null) {
            log.info("Consent token is null. Ask for permission!");
            return getPermissionsResponse();
        }

        switch (intentName) {
            case BANK_ADDRESS_INTENT:
                return bankAddressResponse();
            case BANK_OPENING_HOURS_INTENT:
                return bankOpeningHoursResponse();
            case "AMAZON.HelpIntent":
                return getAskResponse(BANK_CONTACT_CARD, HELP_TEXT);
            default:
                return getAskResponse(BANK_CONTACT_CARD, UNHANDLED_TEXT);
        }
    }

    /**
     * gets the address of a bank
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse bankAddressResponse() {

        Place place = getPlace();

        if (place == null) {
            log.error("No place was found! Your address: " + deviceAddress.toString());
            return getAskResponse(BANK_CONTACT_CARD, ERROR_TEXT);
        }

        return doBankAddressResponse(place);
    }

    /**
     * Creates a {@code SpeechletResponse} for the GetAddress intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse doBankAddressResponse(Place place) {

        String speechText = place.getName() + " hat die Adresse: " + place.getAddress();

        SimpleCard card = getSimpleCard(BANK_CONTACT_CARD, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * search for a place with opening hours
     *
     * @return Place
     */
    private Place getPlace() {

        // finds nearby place according the slot value
        List<Place> places = PlaceFinder.findNearbyPlace(GeoCoder.getLatLng(deviceAddress), slotBankNameValue);

        // check the list of places for one with opening hours
        return PlaceFinder.findOpeningHoursPlace(places, slotBankNameValue);
    }


    private SpeechletResponse bankOpeningHoursResponse() {

        Place place = getPlace();
        if (place == null) {
            log.error("No place was found! Your address: " + deviceAddress.getAddressLine1());
            return getAskResponse(BANK_CONTACT_CARD, ERROR_TEXT);
        }

        return doBankOpeningHoursResponse(place, slotDateValue);
    }

    /**
     * Creates a {@code SpeechletResponse} for the opening hours intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse doBankOpeningHoursResponse(Place place, String slotDate) {

        if (slotDate == null) {
            return doCompleteBankOpeningHoursResponse(place);
        }
        String opening = PlaceFinder.getHours(place, true, slotDate);
        String closing = PlaceFinder.getHours(place, false, slotDate);
        String weekday = PlaceFinder.getWeekday(slotDate, Locale.GERMAN);

        if (closing == null || opening == null) {
            log.error("No opening hours for " + place.getName());
            return getAskResponse(BANK_CONTACT_CARD, NO_OPENING_HOURS);
        }

        String speechText = place.getName() + " Geöffnet am " + weekday + " von " + opening + " bis " + closing;

        SimpleCard card = getSimpleCard(BANK_CONTACT_CARD, speechText);
        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * response the opening hours for the whole week
     *
     * @param place Bank
     * @return SpeechletResponse
     */
    private SpeechletResponse doCompleteBankOpeningHoursResponse(Place place) {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(place.getName()).append(" hat am ");

        List<String> openingWeekdayHours = PlaceFinder.getCompleteWeekdayHours(place);

        for (String hours : openingWeekdayHours) {
            stringBuilder.append(hours);
        }

        return getSSMLResponse(BANK_CONTACT_CARD, stringBuilder.toString());
    }


    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, BANK_ADDRESS_INTENT);
        speechletSubject.attachSpeechletObserver(this, BANK_OPENING_HOURS_INTENT);
    }

    /**
     * Creates a {@code SpeechletResponse} for permission requests.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getPermissionsResponse() {

        String speechText = "Dieser Skill hat keine Berechtigung auf deine Adresse " +
                "Gib bitte diesem Skill die Berechtigung auf deine Adresse zu zugreifen";

        AskForPermissionsConsentCard card = new AskForPermissionsConsentCard();
        card.setTitle(BANK_CONTACT_CARD);

        Set<String> permissions = new HashSet<>();
        permissions.add(ALL_ADDRESS_PERMISSION);
        card.setPermissions(permissions);

        PlainTextOutputSpeech speech = getPlainTextOutputSpeech(speechText);
        return SpeechletResponse.newTellResponse(speech, card);

    }


}
