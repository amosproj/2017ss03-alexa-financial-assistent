package apiTest;

import model.banking.account.Account;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        log.error("Account: " + account.toString());
    }

}
