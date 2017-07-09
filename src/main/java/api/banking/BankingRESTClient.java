package api.banking;

import amosalexa.AmosAlexaSpeechlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.banking.Account;
import model.banking.Card;
import okhttp3.*;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import static amosalexa.AmosAlexaSpeechlet.USER_ID;
import static org.springframework.hateoas.client.Hop.rel;

/**
 * Banking Rest Client
 * <p>
 * more information visit the api guide: https://s3.eu-central-1.amazonaws.com/amos-bank/api-guide.html
 */

public class BankingRESTClient {

    /**
     * Banking API Endpoint
     */
    public static final String BANKING_API_ENDPOINT = "http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com";

    /**
     * Banking API base URL v1.0
     */
    public static final String BANKING_API_BASEURL_V1 = "/api/v2_0";

    /**
     * Logger
     */
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(BankingRESTClient.class);

    /**
     * REST template
     */
    private RestTemplate restTemplate = new RestTemplate();

    /**
     *
     */
    private static BankingRESTClient bankingRESTClient = new BankingRESTClient();

    /**
     * @return BankingRESTClient
     */
    public static BankingRESTClient getInstance() {
        // Refresh the user's access token if necessary
        AuthenticationAPI.updateAccessToken(USER_ID);

        return bankingRESTClient;
    }

    public static HttpHeaders generateHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + AuthenticationAPI.getAccessToken(AmosAlexaSpeechlet.USER_ID));
        return headers;
    }

    /**
     * GET HTTP request to the banking endpoint.
     *
     * @param objectPath object path of the API interface
     * @param cl         response mapping class
     * @return banking object
     */
    public Object getBankingModelObject(String objectPath, Class cl) throws RestClientException {
        String url = BANKING_API_ENDPOINT + BANKING_API_BASEURL_V1 + objectPath;
        log.info("GET from API: " + objectPath + " - Mapping to: " + cl);

        //return restTemplate.getForObject(url, cl);

        HttpEntity entity = new HttpEntity(null, generateHttpHeaders());
        return restTemplate.exchange(url, HttpMethod.GET, entity, cl).getBody();
    }

    /**
     * POST HTTP request to the banking endpoint.
     *
     * @param objectPath object path of the API interface
     * @param request    post object
     * @param cl         response mapping class
     * @return banking object
     */
    public Object postBankingModelObject(String objectPath, Object request, Class cl) throws RestClientException {
        String url = BANKING_API_ENDPOINT + BANKING_API_BASEURL_V1 + objectPath;
        log.info("POST to API: " + objectPath + " - Mapping to: " + cl);

        //return restTemplate.postForObject(url, request, cl);

        HttpEntity entity = new HttpEntity(request, generateHttpHeaders());
        return restTemplate.exchange(url, HttpMethod.POST, entity, cl).getBody();
    }

    /**
     * PUT HTTP request to the banking endpoint.
     *
     * @param objectPath endpoint object
     * @param request    post object
     */
    public void putBankingModelObject(String objectPath, Object request) throws RestClientException {
        String url = BANKING_API_ENDPOINT + BANKING_API_BASEURL_V1 + objectPath;
        log.info("PUT to API: " + objectPath);

        //restTemplate.put(url, request);

        HttpEntity entity = new HttpEntity(request, generateHttpHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
    }

    /**
     * DELETE HTTP request to the banking endpoint.
     *
     * @param objectPath endpoint object
     */
    public void deleteBankingModelObject(String objectPath) throws RestClientException {
        String url = BANKING_API_ENDPOINT + BANKING_API_BASEURL_V1 + objectPath;
        log.info("DELETE to API: " + objectPath);

        //restTemplate.delete(url);

        HttpEntity entity = new HttpEntity(null, generateHttpHeaders());
        restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }
}
