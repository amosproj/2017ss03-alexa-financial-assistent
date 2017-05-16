package model.banking;

import api.BankingRESTClient;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import model.banking.account.Account;

/**
 * Factory to create new accounts at the endpoint
 */
public class AccountFactory {

    /**
     *
     */
    private static AccountFactory accountFactory = new AccountFactory();


    public static AccountFactory getInstance() {
        return accountFactory;
    }

    public Account createAccount(String number, Double balance, String openingDate) throws JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("number", number);
        jsonObject.put("balance", balance);
        jsonObject.put("openingDate", openingDate);

        BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();
        return (Account) bankingRESTClient.postBankingModelObject("/api/v1_0/accounts/generate", jsonObject.toString(), Account.class);
    }

    public Account getAccount(String number){
        BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();
        return (Account) bankingRESTClient.getBankingModelObject("/api/v1_0/accounts/" + number, Account.class);
    }
}
