package api;

import com.amazonaws.util.json.JSONException;
import model.banking.AccountFactory;
import model.banking.account.Account;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;


public class AccountTest {

    private static final Logger log = LoggerFactory.getLogger(AccountTest.class);

    private AccountFactory accountFactory = AccountFactory.getInstance();

    private DummyAccount dummyAccount;

    @Before
    public  void setUp(){
        dummyAccount = new DummyAccount();
    }

    /**
     * Account Information
     *
     * object path /api/v1_0/accounts/{accountnumber}
     *
     * https://s3.eu-central-1.amazonaws.com/amos-bank/api-guide.html#_konto_informationen
     */

    @Test
    public void testAccountObjectModel(){
        Account account = accountFactory.getAccount(dummyAccount.getNumber());
        assertEquals(account.getOpeningDate(), dummyAccount.getOpeningDate());
    }

    @Test
    public void testCreateAccount() throws JSONException {

        DummyAccount acc = new DummyAccount();

        // create account
        Account account =  accountFactory.createAccount(acc.getNumber(), acc.getBalance(), acc.getOpeningDate());

        // check value
        assertEquals(account.getBalance(), acc.getBalance(), 0.0);
        assertEquals(account.getNumber(), acc.getNumber());
        assertEquals(account.getOpeningDate(), acc.getOpeningDate());
    }

}
