package amosalexa.services.bankaccount;


import amosalexa.SessionStorage;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.util.Map;
import java.util.Objects;

public class BalanceLimitService extends AbstractSpeechService implements SpeechService {

	private static final String SET_BALANCE_LIMIT_INTENT = "SetBalanceLimitIntent";
	private static final String SET_BALANCE_LIMIT_DIALOG = "BalanceLimitService";
	private static final String CONTEXT = "DIALOG_CONTEXT";
	private static final String CARD_TITLE = "Kontolimit";
	private static final String NEW_BALANCE_LIMIT = "NewBalanceLimit";

	public BalanceLimitService(SpeechletSubject speechletSubject) {
		subscribe(speechletSubject);
	}

	@Override
	public void subscribe(SpeechletSubject speechletSubject) {
		speechletSubject.attachSpeechletObserver(this, SET_BALANCE_LIMIT_INTENT);
		speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
		speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
	}

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {
		Intent intent = requestEnvelope.getRequest().getIntent();
		Session session = requestEnvelope.getSession();

		SessionStorage.Storage sessionStorage = SessionStorage.getInstance().getStorage(session.getSessionId());

		String dialogContext = (String)sessionStorage.get(CONTEXT);

		// TODO: Temporary fix, handle this in AmosAlexaSpeechlet / SpeechService
		if(!intent.getName().equals(SET_BALANCE_LIMIT_INTENT) && !dialogContext.equals(SET_BALANCE_LIMIT_DIALOG))
			return null; // This intent must be handled by another service

		if(intent.getName().equals(SET_BALANCE_LIMIT_INTENT)) {
			// Set context
			sessionStorage.put(CONTEXT, SET_BALANCE_LIMIT_DIALOG);

			Map<String, Slot> slots = intent.getSlots();
			Slot balanceLimitAmountSlot = slots.get("BalanceLimitAmount");

			if(balanceLimitAmountSlot == null || balanceLimitAmountSlot.getValue() == null) {
				// TODO: Error handling
				return null;
			}

			String balanceLimitAmount = balanceLimitAmountSlot.getValue();

			if(balanceLimitAmount.equals("?")) {
				// TODO: Error handling
				return null;
			}

			sessionStorage.put(NEW_BALANCE_LIMIT, balanceLimitAmount);
			return getBalanceLimitAskResponse(balanceLimitAmount);

		} else if(intent.getName().equals("AMAZON.YesIntent")) {
			if(!sessionStorage.containsKey(NEW_BALANCE_LIMIT)) {
				// TODO: Error handling
				return null;
			}
			return setBalanceLimit((String)sessionStorage.get(NEW_BALANCE_LIMIT));
		} else if(intent.getName().equals("AMAZON.NoIntent")) {
			return getResponse(CARD_TITLE, "");
		}

		return null;
	}

	private SpeechletResponse getBalanceLimitAskResponse(String balanceLimitAmount) {
		return getAskResponse(CARD_TITLE, "Möchtest du dein Kontolimit wirklich auf " + balanceLimitAmount + " Euro setzen?");
	}

	private SpeechletResponse setBalanceLimit(String balanceLimitAmount) {
		// TODO: Set new limit
		return getResponse(CARD_TITLE, "Okay, dein Kontolimit wurde auf " + balanceLimitAmount + " Euro gesetzt.");
	}

}
