package amosalexa.services.pricequery.aws.creator;

import amosalexa.services.pricequery.aws.model.Offer;
import amosalexa.services.pricequery.aws.util.AWSUtil;
import amosalexa.services.pricequery.aws.util.XMLParser;
import java.sql.Timestamp;


public class OfferCreator {

    private static String offerSummaryXML;
    private static String offers;
    private static String asin;

    public static Offer createOffer(String xml) {

        asin = XMLParser.readValue(xml, new String[]{"Items", "Item", "ASIN"});
        offerSummaryXML = XMLParser.readValue(xml, new String[]{"Items", "Item", "OfferSummary"});
        offers = XMLParser.readValue(xml, new String[]{"Items", "Item", "Offers", "Offer", "OfferListing"});

        return createOffer();
    }

    public static Offer createOffer() {

        Offer offer = new Offer();
        offer.setAsin(asin);

        /** Offer Summary **/

        java.util.Date date = new java.util.Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        offer.setTime(timestamp);

        String lowestNewPrice = XMLParser.readValue(offerSummaryXML, new String[]{"LowestNewPrice", "Amount"});
        if(AWSUtil.isNumeric(lowestNewPrice)){
            offer.setLowestNewPrice(Integer.parseInt(lowestNewPrice));
        }

        String currencyCode = XMLParser.readValue(offerSummaryXML, new String[]{"LowestNewPrice", "CurrencyCode"});
        offer.setCurrencyCode(currencyCode);

        String lowestUsedPrice = XMLParser.readValue(offerSummaryXML, new String[]{"LowestUsedPrice"});
        if(AWSUtil.isNumeric(lowestUsedPrice)){
            offer.setLowestUsedPrice(Integer.parseInt(lowestUsedPrice));
        }

        String totalNew = XMLParser.readValue(offerSummaryXML, new String[]{"TotalNew"});
        if(AWSUtil.isNumeric(totalNew)) {
            offer.setTotalNew(Integer.parseInt(totalNew));
        }

        String totalUsed = XMLParser.readValue(offerSummaryXML, new String[]{"TotalUsed"});
        if(AWSUtil.isNumeric(totalUsed)) {
            offer.setTotalUsed(Integer.parseInt(totalUsed));
        }
        /** Offers **/

        String offerListingId = XMLParser.readValue(offers, new String[]{"OfferListingId"});
        offer.setOfferListingId(offerListingId);


        String amazonNewPrice = XMLParser.readValue(offers, new String[]{"Price", "Amount"});
        if(AWSUtil.isNumeric(amazonNewPrice)) {
            offer.setAmazonNewPrice(Integer.parseInt(amazonNewPrice));
        }

        String amazonAmountSaved = XMLParser.readValue(offers, new String[]{"AmountSaved", "Amount"});
        if(AWSUtil.isNumeric(amazonAmountSaved)){
            offer.setAmazonAmountSaved(Integer.parseInt(amazonAmountSaved));
        }

        String availability = XMLParser.readValue(offers, new String[]{"Availability"});
        if(availability.length() < 50){
            offer.setAmazonAvailability(availability);
        }

        String eligibleForSuperSaverShipping = XMLParser.readValue(offers, new String[]{"IsEligibleForSuperSaverShipping"});
        offer.setEligibleForSuperSaverShipping(eligibleForSuperSaverShipping.equals("1"));

        String eligibleForPrime = XMLParser.readValue(offers, new String[]{"IsEligibleForPrime"});
        offer.setEligibleForPrime(eligibleForPrime.equals("1"));

        return offer;
    }
}
