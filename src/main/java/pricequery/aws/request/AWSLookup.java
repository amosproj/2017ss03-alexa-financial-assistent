package pricequery.aws.request;

import org.apache.log4j.Logger;
import pricequery.aws.creator.ItemCreator;
import pricequery.aws.creator.OfferCreator;
import pricequery.aws.model.Item;
import pricequery.aws.model.Offer;
import pricequery.aws.util.AWSUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AWSLookup {

    final static Logger log = Logger.getLogger(AWSLookup.class);

    public static void main(String[] args) {

        List<Item> items = itemSearch("Iphone", 1, null);
        for (Item item : items){
            log.info(item.getTitle());

        }
    }

    public static List<Item> itemSearch(String keyword, int itemPage, String sort){

       Map<String, String> params = new HashMap<>();

        params.put("Operation", "ItemSearch");
        params.put("ResponseGroup", "Large");
        params.put("SearchIndex", "All");
        params.put("Sort", sort == null ? "" : sort);
        params.put("Keywords", AWSUtil.encodeString(keyword));
        params.put("ItemPage", Integer.toString(itemPage));

        AWSRequest awsRequest = new AWSRequest(params);
        String xml = awsRequest.signedRequest();

        return ItemCreator.createItems(xml);
    }

    public static Offer offerLookup(String ASIN){

       Map<String, String> params = new HashMap<>();

        params.put("Operation", "ItemLookup");
        params.put("ResponseGroup", "Offers");
        params.put("ItemId", ASIN);

        AWSRequest awsRequest = new AWSRequest(params);
        String xml = awsRequest.signedRequest();

        return OfferCreator.createOffer(xml);
    }

}
