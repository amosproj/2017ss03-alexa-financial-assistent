package api;

import api.banking.AccountAPI;
import com.amazonaws.util.json.JSONException;
import model.banking.Account;
import model.banking.Card;
import model.banking.StandingOrder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;

import static org.junit.Assert.*;


public class AccountTest {

    private static final Logger log = LoggerFactory.getLogger(AccountTest.class);

    private Account dummyAccount;
    private static final String ACCOUNT_NUMBER = "0000000000";
    private static final String ACCOUNT_NUMBER2 = "0000000001";
    private static final String CARD_NUMBER = "0000000001";

    @Before
    public void setUp() {
        dummyAccount = new Account();
        dummyAccount.setNumber(ACCOUNT_NUMBER);
        dummyAccount.setBalance(1234);
        dummyAccount.setOpeningDate(new DateTime(2017, 5, 1, 12, 0).toLocalDate().toString());

        AccountAPI.createAccount(dummyAccount);
    }

    /**
     * Account Information
     *
     * object path /api/v1_0/accounts/{accountnumber}
     *
     * https://s3.eu-central-1.amazonaws.com/amos-bank/api-guide.html#_konto_informationen
     */

    @Test
    public void testGetAccount() {
        Account account = AccountAPI.getAccount(ACCOUNT_NUMBER);

        // check value
        assertEquals(account.getBalance().doubleValue(), dummyAccount.getBalance().doubleValue(), 0);
        assertEquals(account.getNumber(), dummyAccount.getNumber());
        assertEquals(account.getOpeningDate(), dummyAccount.getOpeningDate());
    }

    @Test
    public void testCreateAndGetCard() {
        Card card = new Card();
        card.setCardType(Card.CardType.DEBIT);
        card.setCardNumber(CARD_NUMBER);
        card.setStatus(Card.Status.ACTIVE);
        card.setExpirationDate(new DateTime(2018, 5, 1, 12, 0).toLocalDate().toString());
        card.setAccountNumber(ACCOUNT_NUMBER);

        Card newCard = AccountAPI.createCardForAccount(ACCOUNT_NUMBER, card);

        assertEquals(card.getCardType(), newCard.getCardType());
        assertEquals(card.getCardNumber(), newCard.getCardNumber());
        assertEquals(card.getStatus(), newCard.getStatus());
        assertEquals(card.getExpirationDate(), newCard.getExpirationDate());

        // Get cards
        Collection<Card> cards = AccountAPI.getCardsForAccount(ACCOUNT_NUMBER);

        boolean foundCard = false;

        for(Card card1 : cards) {
            if(card1.getCardNumber().equals(newCard.getCardNumber())) {
                foundCard = true;
                break;
            }
        }

        assertTrue(foundCard);
    }

    @Test
    public void testCreateAndGetStandingOrder() {
        String firstExecution = new DateTime(2018, 5, 1, 12, 0).toLocalDate().toString();

        StandingOrder standingOrder = AccountAPI.createStandingOrderForAccount(ACCOUNT_NUMBER, "Test", 123, ACCOUNT_NUMBER2,
                firstExecution, StandingOrder.ExecutionRate.MONTHLY, "Description");

        assertEquals(standingOrder.getPayee(), "Test");
        assertEquals(standingOrder.getAmount(), 123);
        assertEquals(standingOrder.getDestinationAccount(), ACCOUNT_NUMBER2);
        assertEquals(standingOrder.getFirstExecution(), firstExecution);
        assertEquals(standingOrder.getExecutionRate(), StandingOrder.ExecutionRate.MONTHLY);
        assertEquals(standingOrder.getDescription(), "Description");

        // Get cards
        Collection<StandingOrder> standingOrders = AccountAPI.getStandingOrdersForAccount(ACCOUNT_NUMBER);

        boolean foundStandingOrder = false;

        for(StandingOrder standingOrder1 : standingOrders) {
            if(standingOrder1.getStandingOrderId().equals(standingOrder.getStandingOrderId())) {
                foundStandingOrder = true;
                break;
            }
        }

        assertTrue(foundStandingOrder);
    }

}
