package api;

import com.amazonaws.util.json.JSONException;
import model.banking.AccountFactory;
import model.banking.account.Account;
import org.joda.time.DateTime;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;


public class AccountTest {

    private static final Logger log = LoggerFactory.getLogger(AccountTest.class);


    /**
     * Account Information
     *
     * object path /api/v1_0/accounts/{accountnumber}
     *
     * https://s3.eu-central-1.amazonaws.com/amos-bank/api-guide.html#_konto_informationen
     */

    @Test
    public void testAccountObjectModel(){
        BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();
        Account account = (Account) bankingRESTClient.getBankingModelObject("/api/v1_0/accounts/9999999999", Account.class);
        log.error("Request Account: " + account.toString());
    }

    @Test
    public void testCreateAccount() throws JSONException {

        // init
        String number = "0000000000";
        double balance = 1.0;
        String openingDate = new DateTime(2017, 5, 1, 12, 0).toLocalDate().toString();

        AccountFactory accountFactory = AccountFactory.getInstance();

        // create account
        Account account =  accountFactory.createAccount(number, balance, openingDate);

        // check value
        assertEquals(account.getBalance(), balance, 0.0);
        assertEquals(account.getNumber(), number);
        assertEquals(account.getOpeningDate(), openingDate);
    }

}
