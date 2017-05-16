package apiTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.LoggerFactory;
import pricequery.PriceQuerySpeechlet;

import java.io.IOException;

/**
 * Banking Rest Client
 */

public class BankingRESTClient {

    private static String BANKING_API_ENDPOINT = "http://amos-bank-lb-723794096.eu-central-1.elb.amazonaws.com";

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PriceQuerySpeechlet.class);

    private static OkHttpClient client = new OkHttpClient.Builder().build();

    private static BankingRESTClient bankingRESTClient = new BankingRESTClient();

    public static BankingRESTClient getInstance(){ return bankingRESTClient;}

    private String getJSON(String url){

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

    public Object getBankingModelObject(String servicePath, Class cl){
        log.debug("Request from API: " + servicePath + " Mapping to: " + cl);
        String json = getJSON(BANKING_API_ENDPOINT + servicePath);
        try {
            return new ObjectMapper().readValue(json, cl);
        } catch (IOException e) {
            log.error("IOException Exception - Can not read value.");
            e.printStackTrace();
        }
        return null;
    }

}
