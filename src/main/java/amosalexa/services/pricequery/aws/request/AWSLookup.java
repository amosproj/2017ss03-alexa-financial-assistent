package amosalexa.services.pricequery.aws.request;

import amosalexa.services.pricequery.aws.creator.ItemCreator;
import amosalexa.services.pricequery.aws.creator.OfferCreator;
import amosalexa.services.pricequery.aws.model.Item;
import amosalexa.services.pricequery.aws.model.Offer;
import amosalexa.services.pricequery.aws.util.AWSUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AWSLookup {

    public static List<Item> itemSearch(String keyword, int itemPage, String sort) throws ParserConfigurationException, SAXException, IOException {

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

    public static Offer offerLookup(String ASIN) throws ParserConfigurationException, SAXException, IOException {

       Map<String, String> params = new HashMap<>();

        params.put("Operation", "ItemLookup");
        params.put("ResponseGroup", "Offers");
        params.put("ItemId", ASIN);

        AWSRequest awsRequest = new AWSRequest(params);
        String xml = awsRequest.signedRequest();

        return OfferCreator.createOffer(xml);
    }

    public static Item itemLookup(String ASIN) throws ParserConfigurationException, SAXException, IOException {

        Map<String, String> params = new HashMap<>();

        params.put("Operation", "ItemLookup");
        params.put("ResponseGroup", "Large");
        params.put("ItemId", ASIN);

        AWSRequest awsRequest = new AWSRequest(params);
        String xml = awsRequest.signedRequest();

        return ItemCreator.createItem(xml);
    }

}
