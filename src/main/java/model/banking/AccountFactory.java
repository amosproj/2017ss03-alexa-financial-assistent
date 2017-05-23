package model.banking;

import api.BankingRESTClient;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import model.banking.account.Account;
import model.banking.account.CardResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static org.springframework.hateoas.client.Hop.rel;

/**
 * Factory to get or create new accounts at the endpoint
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

    /**
     * Get all cards (credit / debit) for the given account
     * @param number Account number
     * @return Collection of CardResponses
     * @throws URISyntaxException
     * @throws HttpClientErrorException
     */
    public Collection<CardResponse> getCardsForAccount(String number) throws HttpClientErrorException {
        Traverson traverson = null;
        try {
            traverson = new Traverson(new URI(BankingRESTClient.BANKING_API_ENDPOINT + BankingRESTClient.BANKING_API_BASEURL_V1 + "/accounts/" + number + "/cards"),
					MediaTypes.HAL_JSON);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ParameterizedTypeReference<Resources<CardResponse>> typeRefDevices = new ParameterizedTypeReference<Resources<CardResponse>>() {};

        Resources<CardResponse> resCardResponses = traverson.follow(rel("$._links.self.href")).toObject(typeRefDevices);
        Collection<CardResponse> cards = resCardResponses.getContent();

        return cards;
    }
}
