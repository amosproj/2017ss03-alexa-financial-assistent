package amosalexa.dialogsystem.dialogs;

import amosalexa.AmosAlexaSpeechlet;
import amosalexa.SessionStorage;
import amosalexa.dialogsystem.DialogHandler;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;
import model.banking.AccountFactory;
import model.banking.account.CardResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReplacementCardDialog implements DialogHandler {

	private enum ReplacementReason {
		BLOCKED,
		DAMAGED
	}

	private static final Logger logger = LoggerFactory.getLogger(ReplacementCardDialog.class);

	private static final String STORAGE_VALID_CARDS = "REPLACEMENT_VALID_CARDS";
	private static final String STORAGE_SELECTED_CARD = "REPLACEMENT_SELECTED_CARD";
	private static final String STORAGE_REASON = "REPLACEMENT_REASON";

	@Override
	public String getDialogName() {
		return "ReplacementCard";
	}

	@Override
	public SpeechletResponse handle(Intent intent, SessionStorage.Storage storage) throws SpeechletException {
		String intentName = intent.getName();

		if("ReplacementCardIntent".equals(intentName)) {
			return askForCardNumber(intent, storage, false);
		} else if("FourDigitNumberIntent".equals(intentName)) {
			return askIfBlockedOrDamaged(intent, storage);
		} else if("ReplacementCardReasonIntent".equals(intentName)) {
			return askForConfirmation(intent, storage);
		} else if("AMAZON.YesIntent".equals(intentName)) {
			return orderReplacement(intent, storage);
		} else if("AMAZON.NoIntent".equals(intentName)) {
			return cancelDialog();
		} else {
			throw new SpeechletException("Unhandled intent: " + intentName);
		}
	}

	private SpeechletResponse askForCardNumber(Intent intent, SessionStorage.Storage storage, boolean errored) {
		Collection<CardResponse> cards = AccountFactory.getInstance().getCardsForAccount("0000000000"); // TODO: Load account from session

		if(cards.size() == 0) {
			// This user does not have any cards, ordering a replacement card is impossible.
			PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
			speech.setText("Es wurden keine Kredit- oder EC-Karten gefunden.");
			return SpeechletResponse.newTellResponse(speech);
		} else {
			SsmlOutputSpeech speech = new SsmlOutputSpeech();
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("<speak>");

			if(errored) {
				stringBuilder.append("Entschuldigung, das habe ich nicht verstanden. ");
			}

			stringBuilder.append("Bestellung einer Ersatzkarte. Es wurden folgende Karten gefunden: ");

			List<String> userCards = new ArrayList<>();

			for (CardResponse card : cards) {
				// Check if this card is active
				if(card.status != CardResponse.Status.ACTIVE) {
					continue;
				}

				userCards.add(card.cardNumber);

				String prefix = (card.cardType == CardResponse.CardType.CREDIT ? "Kredit" : "EC-");
				stringBuilder.append(prefix + "karte mit den Endziffern <say-as interpret-as=\"digits\">" +
						card.cardNumber.substring(card.cardNumber.length() - 4) + "</say-as>. ");
			}

			// Store all card numbers in the session
			storage.put(STORAGE_VALID_CARDS, userCards);

			stringBuilder.append("Bitte gib die Endziffern der Karte an, für die du Ersatz benötigst.");
			stringBuilder.append("</speak>");
			speech.setSsml(stringBuilder.toString());

			// Create reprompt
			Reprompt reprompt = new Reprompt();
			reprompt.setOutputSpeech(speech);

			return SpeechletResponse.newAskResponse(speech, reprompt);
		}
	}

	private SpeechletResponse askIfBlockedOrDamaged(Intent intent, SessionStorage.Storage storage) {
		String fourDigits = intent.getSlot("FourDigits").getValue();

		logger.info("Digits: " + fourDigits);
		boolean validDigits = false;

		// Check if these digits are valid
		List<String> userCards = (List<String>)storage.get(STORAGE_VALID_CARDS);
		for(String cardNumber: userCards) {
			if(cardNumber.substring(cardNumber.length() - 4).equals(fourDigits)) {
				// Digits are valid
				storage.put(STORAGE_SELECTED_CARD, cardNumber);
				validDigits = true;
				break;
			}
		}

		// If these are invalid digits, ask again
		if(!validDigits) {
			return askForCardNumber(intent, storage, true);
		}

		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText("Wurde die Karte gesperrt oder wurde sie beschädigt?");

		// Create reprompt
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(speech);

		return SpeechletResponse.newAskResponse(speech, reprompt);
	}

	private SpeechletResponse askForConfirmation(Intent intent, SessionStorage.Storage storage) {
		if(!storage.containsKey(STORAGE_SELECTED_CARD)) {
			return askForCardNumber(intent, storage, true);
		}

		String replacementReason = intent.getSlot("ReplacementReason").getValue();
		logger.info("Replacement reason: " + replacementReason);

		if(replacementReason.equals("beschaedigt")) {
			logger.info("Beschädigt");
			storage.put(STORAGE_REASON, ReplacementReason.DAMAGED);
		} else if(replacementReason.equals("gesperrt")) {
			logger.info("Gesperrt");
			storage.put(STORAGE_REASON, ReplacementReason.BLOCKED);
		} else {
			return askIfBlockedOrDamaged(intent, storage);
		}

		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		String reason = storage.get(STORAGE_REASON) == ReplacementReason.DAMAGED ? "beschädigte" : "gesperrte";
		String lastDigits = (String)storage.get(STORAGE_SELECTED_CARD);
		lastDigits = lastDigits.substring(lastDigits.length() - 4);
		speech.setSsml("<speak>Soll ein Ersatz für die " + reason + " Karte mit den Endziffern <say-as interpret-as=\"digits\">" +
				 lastDigits + "</say-as> bestellt werden?</speak>");

		// Create reprompt
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(speech);

		return SpeechletResponse.newAskResponse(speech, reprompt);
	}

	private SpeechletResponse orderReplacement(Intent intent, SessionStorage.Storage storage) {
		if(!storage.containsKey(STORAGE_SELECTED_CARD)) {
			return askForCardNumber(intent, storage, true);
		}
		if(!storage.containsKey(STORAGE_REASON)) {
			return askIfBlockedOrDamaged(intent, storage);
		}

		// TODO: Actually order a replacement card

		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText("Okay, eine Ersatzkarte wurde bestellt.");

		return SpeechletResponse.newTellResponse(speech);
	}

	private SpeechletResponse cancelDialog() {
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText("");

		return SpeechletResponse.newTellResponse(speech);
	}
}
