package amosalexa.depot;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;

public class DummyDepot {


    static String repromptTextWelcome = "Willkommen bei der Amos Banking App. Du kannst Überweisungen tätigen," +
            "dein SecuritiesAccount abfragen und erhältst Tipps zur Geldanlage.";

    public static SpeechletResponse getDepotComposition(Intent intent, Session session) {

        String[] stock1 = new String[]{"Apple", "5", "AAPL"};
        String[] stock2 = new String[]{"Tesla", "10", "TSLA"};
        String[] stock3 = new String[]{"Microsoft", "5", "MSFT"};


        return getSpeechletResponse("Du hast folgende Aktien im SecuritiesAccount: "
                        + stock1[1] + " Aktien von " + stock1[0] + ", "
                        + stock2[1] + " Aktien von " + stock2[0] + " und "
                        + stock3[1] + " Aktien von " + stock3[0]
                , repromptTextWelcome);
    }

    public static SpeechletResponse getDepotInformation(Intent intent, Session session) {

        String StockTesla = null;//FinanceApi.getStockPrice("TSLA");
        String StockApple = null;//FinanceApi.getStockPrice("AAPL");
        String StockMicrosoft = null;//FinanceApi.getStockPrice("MSFT");

        double numStocksApple = 5;
        double numStocksTesla = 10;
        double numStocksMicrosoft = 10;


        double DoubleDepotWert = Double.parseDouble(StockTesla) * numStocksTesla
                + Double.parseDouble(StockApple) * numStocksApple
                + Double.parseDouble(StockMicrosoft) * numStocksMicrosoft;


        String wertDepot = String.valueOf(DoubleDepotWert);

        return getSpeechletResponse("Der Gesamtwert deines Depots liegt aktuell bei"
                + wertDepot + "Dollar.", repromptTextWelcome);
    }

    private static SpeechletResponse getSpeechletResponse(String speechText, String repromptText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Bank Information");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }

}
