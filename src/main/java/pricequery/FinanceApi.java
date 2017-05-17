package pricequery;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class FinanceApi {
    OkHttpClient client = new OkHttpClient();

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public static String getStockPrice(String stock) {

        try {
            FinanceApi finance = new FinanceApi();
            String response = finance.run("http://finance.google.com/finance/info?client=ig&q=NASDAQ:" + stock);

            response = response.substring(5, response.length() - 2);

            //create a JSON-Object of the String
            final JSONObject obj = new JSONObject(response);

            // read out the current stock price
            //Gson gson = new Gson();


            JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();

            // JsonObject jsonObject = new JsonParser().parse("{\"pcls_fix\": \"68.38\"}").getAsJsonObject();
            String stockPrice = jsonObject.get("pcls_fix").getAsString();

            return stockPrice;
        }
        catch (IOException io) {
            System.out.println("Error: " + io);
        }
        catch (JSONException json) {
            System.out.println("Error: " + json);
        }

        return null;

    }
}