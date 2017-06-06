package amosalexa.services;


import amosalexa.services.bankcontact.AlexaDeviceAddressClient;
import amosalexa.services.bankcontact.BankContactService;
import amosalexa.services.bankcontact.exceptions.DeviceAddressClientException;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.Context;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceAddressUtil {

    private static final Logger log = LoggerFactory.getLogger(DeviceAddressUtil.class);

    /**
     * tries to get the device address
     * @param requestEnvelope SpeechletRequestEnvelope
     */
    public static void getDeviceAddress(SpeechletRequestEnvelope<IntentRequest> requestEnvelope){

        try {

            BankContactService.consentToken = requestEnvelope.getSession().getUser().getPermissions().getConsentToken();
            SystemState systemState = getSystemState(requestEnvelope.getContext());
            String deviceId = systemState.getDevice().getDeviceId();
            String apiEndpoint = systemState.getApiEndpoint();

            AlexaDeviceAddressClient alexaDeviceAddressClient = new AlexaDeviceAddressClient(deviceId, BankContactService.consentToken , apiEndpoint);

            BankContactService.deviceAddress = alexaDeviceAddressClient.getFullAddress();

            if (BankContactService.deviceAddress == null) {
                log.error("Requested device address is null");
            }

        } catch (DeviceAddressClientException e) {
            log.error("Device Address Client failed to successfully return the address.");
        } catch (NullPointerException e){
            log.warn("No Permission!");
        }
    }

    /**
     * Helper method that retrieves the system state from the request context.
     *
     * @param context request context.
     * @return SystemState the systemState
     */
    private static SystemState getSystemState(Context context) {
        return context.getState(SystemInterface.class, SystemState.class);
    }
}