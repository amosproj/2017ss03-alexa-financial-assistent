package amosalexa.services.financing;

import amosalexa.SessionStorage;
import amosalexa.SpeechletSubject;
import amosalexa.services.AbstractSpeechService;
import amosalexa.services.SpeechService;
import amosalexa.services.bankaccount.BankAccountService;
import amosalexa.services.pricequery.aws.model.Item;
import amosalexa.services.pricequery.aws.model.Offer;
import amosalexa.services.pricequery.aws.request.AWSLookup;
import amosalexa.services.pricequery.aws.util.AWSUtil;
import api.banking.AccountAPI;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import model.banking.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AffordabilityService extends AbstractSpeechService implements SpeechService{


    private static final Logger log = LoggerFactory.getLogger(AffordabilityService.class);
    /**
     * intents
     */
    private final static String AFFORD_INTENT = "AffordProduct";
    /**
     * slot
     */
    private static final String KEYWORD_SLOT = "ProductKeyword";
    private static final String PRODUCT_SLOT = "ProductSelection";
    /**
     * cards
     */
    private static final String CARD = "BUY_CHECK";
    /**
     * speech texts
     */
    private static final String DESIRE_ASK = "Möchtest du ein Produkt kaufen?";
    private static final String SELECTION_ASK = "Welches Produkt möchtest du kaufen? Sag produkt a, b oder c ";
    private static final String ERROR = "Ich konnte deine Eingabe nicht verstehen. Sprich Lauter!";
    private static final String NO_RESULTS = "Die Suche ergab keine Ergebnisse";
    private static final String TOO_FEW_RESULTS = "Die Suche ergab keine Ergebnisse";
    private static final String CANT_AFFORD = "Das Produkt kannst du dir nicht leisten!";
    private static final String OTHER_SELECTION = "Möchtest du nach etwas anderem suchen?";
    private static final String BUY_ASK = "Willst du das Produkt in den Warenkorb legen?";
    private static final String CART_ACK = "Das Produkt wurde in deinen Warenkorb gelegt";
    private static final String SEARCH_ASK = "Was suchst du? Beginne deine Anfrage mit Ich suche ";
    /**
     * attributes
     */
    private static final String AFFORDABILITY_ATTRIBUTE = "affordability_attribute";
    /**
     * slot values
     */
    private static String keywordSlot;
    private static String productSelectionSlot;
    /**
     * session / intent
     */
    private static Session session;
    private static Intent intent;
    public AffordabilityService(SpeechletSubject speechletSubject){
        subscribe(speechletSubject);
    }

    @Override
    public String getDialogName() {
        return this.getClass().getName();
    }

    @Override
    public List<String> getStartIntents() {
        return Arrays.asList(
                AFFORD_INTENT
        );
    }

    @Override
    public List<String> getHandledIntents() {
        return Arrays.asList(
                AFFORD_INTENT,
                YES_INTENT,
                NO_INTENT
        );
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        for (String intent : getHandledIntents()) {
            speechletSubject.attachSpeechletObserver(this, intent);
        }
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) throws SpeechletException {

        intent = requestEnvelope.getRequest().getIntent();
        session = requestEnvelope.getSession();

        getSlots();

        // user decides if he wants to buy a product
        Object stateBuyAsk = getDialogState("buy?");
        if(stateBuyAsk != null){
            log.debug("Dialog State: buy?");
            if(intent.getName().equals(YES_INTENT)){
                setDialogState("which?");
                return getAskResponse(CARD, SELECTION_ASK);
            }
            return exitDialogRespond();
        }

        // user decides which product he wants to buy
        Object whichProState = getDialogState("which?");
        if(whichProState != null){
            log.debug("Dialog State: which?");
            if(productSelectionSlot != null){
                return getAffordabilityResponse();
            }
            setDialogState("which?");
            return getAskResponse(CARD, SELECTION_ASK);
        }

        // user decides if he want to look for other products
        Object lookForCheaperAsk = getDialogState("other?");
        if(lookForCheaperAsk != null){
            log.debug("Dialog State: other?");
            if(intent.getName().equals(YES_INTENT)){
                return getAskResponse(CARD, SEARCH_ASK);
            }
            return exitDialogRespond();
        }

        // user decides if he want to put the item in the cart
        Object putInCartAsk = getDialogState("cart?");
        if(putInCartAsk != null){
            log.debug("Dialog State: cart?");
            if (intent.getName().equals(YES_INTENT)){
                return getAskResponse(CARD, CART_ACK);
            }
            return exitDialogRespond();
        }

        return getProductInformation();
    }

    /**
     * default exit response
     * @return SpeechletResponse
     */
    private SpeechletResponse exitDialogRespond(){
        return getResponse(CARD, "Ok, Tschüss!");
    }

    /**
     * response if the selected items is affordable or not referring the account balance
     * @return SpeechletResponse
     */
    private SpeechletResponse getAffordabilityResponse(){


        log.info("Selection: " + "produkt " + productSelectionSlot.toLowerCase());
        Item selectedItem = (Item) SessionStorage.getInstance().getObject(session.getSessionId(), "produkt " + productSelectionSlot.toLowerCase());

        if(selectedItem == null){
            log.error("selected item is null");
            return getAskResponse(CARD, DESIRE_ASK);
        }

        // save selection in session
        SessionStorage.getInstance().putObject(session.getSessionId(), "selection", selectedItem);

        Account account = AccountAPI.getAccount(BankAccountService.AccountNumber);
        Number balance = account.getBalance();

        if(selectedItem.getOffer().getLowestNewPrice() > balance.doubleValue()){

            // save state too expensive
            SessionStorage.getInstance().putObject(session.getSessionId(), AFFORDABILITY_ATTRIBUTE, false);

            setDialogState("other?");
            return getAskResponse(CARD, account.getBalanceText() + " " +  getItemText(selectedItem)
                    + " " + CANT_AFFORD + " " + OTHER_SELECTION);
        }

        String confirmationText = "Produkt " + productSelectionSlot + " " +  selectedItem.getTitleShort() + " ";

        setDialogState("cart?");
        return getAskResponse(CARD, confirmationText + BUY_ASK);
    }

    /**
     * creates a speech response by the response of a amazon query
     * @return SpeechletResponse
     */
    private SpeechletResponse getProductInformation() {
        if (keywordSlot != null) {

            log.debug("Dialog State: amazon query?");

            List<Item> items = AWSLookup.itemSearch(keywordSlot, 1, null);

            if (items.isEmpty()) {
                log.error("no results by keywordSlot: " + keywordSlot);
                return getErrorResponse(NO_RESULTS);
            }

            if(items.size() < 3){
                log.error("too few results by keywordSlot: " + keywordSlot);
                return getErrorResponse(TOO_FEW_RESULTS);
            }


            StringBuilder text = new StringBuilder();

            for (int i = 0; i < 3; i++) {

                // look up offer
                Offer offer = AWSLookup.offerLookup(items.get(i).getASIN());
                items.get(i).setOffer(offer);

                // save in session
                SessionStorage.getInstance().putObject(session.getSessionId(), "produkt " + (char) ('a' + i) , items.get(0));

                // shorten title
                String productTitle = AWSUtil.shortTitle(items.get(i).getTitle());
                items.get(i).setTitleShort(productTitle);

                // build respond
                text.append("Produkt ")
                        .append((char) ('a' + i))
                        .append(" ")
                        .append(getItemText(items.get(i)));
            }

            text.append(DESIRE_ASK);
            setDialogState("buy?");

            return getAskResponse(CARD, text.toString());
        }

        log.debug("Dialog State: no keyword");
        return getResponse(CARD, ERROR);
    }


    /**
     * get all slot values
     */
    private void getSlots(){
        keywordSlot = intent.getSlot(KEYWORD_SLOT) != null ? intent.getSlot(KEYWORD_SLOT).getValue() : null;

        log.info("Search Keyword: " + keywordSlot);

        productSelectionSlot = intent.getSlot(PRODUCT_SLOT) != null ? intent.getSlot(PRODUCT_SLOT).getValue() : null;

        log.info("Product Selection: " + productSelectionSlot);
    }

    /**
     * returns the text for a item of the amazon search
     * @param item Item from Amazon
     * @return text
     */
    private String getItemText(Item item){
        return item.getTitleShort() + "kostet <say-as interpret-as=\"unit\">€" + item.getOffer().getLowestNewPrice() / 100 +
                "</say-as> <break time=\"1s\"/>";
    }

    /**
     * sets a dialog state
     * @param name of the state
     */
    protected void setDialogState(String name){
        SessionStorage.getInstance().putObject(session.getSessionId(), name, new Object());
    }

    /**
     * get dialog state
     * @param name of the state
     * @return Object
     */
    protected Object getDialogState(String name){

        //get state
        Object object = SessionStorage.getInstance().getObject(session.getSessionId(), name);

        //revoke old state
        SessionStorage.getInstance().putObject(session.getSessionId(), name, null);

        return object;
    }
}
