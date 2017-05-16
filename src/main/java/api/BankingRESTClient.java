package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.LoggerFactory;
import pricequery.PriceQuerySpeechlet;

import java.io.IOException;

/**
 * Banking Rest Client
 *
 * more information visit the api guide: https://s3.eu-central-1.amazonaws.com/amos-bank/api-guide.html
 */

public class BankingRESTClient {

    /**
     * Banking API Endpoint
     */
    private static final String BANKING_API_ENDPOINT = "http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com";

    /**
     *
     */
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     *
     */
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PriceQuerySpeechlet.class);

    /**
     * Http Client
     */
    private static OkHttpClient client = new OkHttpClient.Builder().build();

    /**
     *
     */
    private static BankingRESTClient bankingRESTClient = new BankingRESTClient();

    /**
     *
     * @return BankingRESTClient
     */
    public static BankingRESTClient getInstance(){ return bankingRESTClient;}


    /**
     * makes a http request to the api endpoint
     * @param url api request url
     * @return String
     */
    private String doGetBankingModelObject(String url){

        log.error("Banking API Request - URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            log.error("IOException: Can not execute request.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * maps response of http request to the object model
     * @param objectPath object path of the API interface
     * @param cl mapping class
     * @return banking object
     */
    public Object getBankingModelObject(String objectPath, Class cl){
        log.info("GET from API: " + objectPath + " Mapping to: " + cl);
        String response = doGetBankingModelObject(BANKING_API_ENDPOINT + objectPath);
        try {
            return new ObjectMapper().readValue(response, cl);
        } catch (IOException e) {
            log.error("IOException Exception - Can not read value.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * actually post request for postBankingModelObject()
     * @param url uri
     * @param json body
     * @return JSON response
     */
    private String doPostBankingModelObject(String url, String json){

        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * POST http request to the banking endpoint
     * @param objectPath endpoint object
     * @param json body
     * @param cl mapping class
     * @return banking object
     */
    public Object postBankingModelObject(String objectPath, String json, Class cl){
        log.info("POST from API: " + objectPath + " Body: " + json);
        String response = doPostBankingModelObject(BANKING_API_ENDPOINT + objectPath, json);
        try {
            return new ObjectMapper().readValue(response, cl);
        } catch (IOException e) {
            log.error("IOException Exception - Can not read value.");
            e.printStackTrace();
        }
        return null;
    }
}
