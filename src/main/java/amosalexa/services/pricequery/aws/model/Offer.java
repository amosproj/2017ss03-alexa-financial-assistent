package amosalexa.services.pricequery.aws.model;

import java.sql.Timestamp;

public class Offer {

    private Long id;

    private String asin;
    private Timestamp time;

    private String offerListingId;
    private String currencyCode;

    private int lowestNewPrice;
    private int totalNew;

    private int lowestUsedPrice;
    private int totalUsed;

    private int amazonNewPrice;
    private int amazonAmountSaved;


    private String amazonAvailability;

    private boolean eligibleForPrime;

    private boolean eligibleForSuperSaverShipping;

    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getOfferListingId() {
        return offerListingId;
    }

    public void setOfferListingId(String offerListingId) {
        this.offerListingId = offerListingId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getLowestNewPrice() {
        return lowestNewPrice;
    }

    public void setLowestNewPrice(int lowestNewPrice) {
        this.lowestNewPrice = lowestNewPrice;
    }

    public int getTotalNew() {
        return totalNew;
    }

    public void setTotalNew(int totalNew) {
        this.totalNew = totalNew;
    }

    public int getLowestUsedPrice() {
        return lowestUsedPrice;
    }

    public void setLowestUsedPrice(int lowestUsedPrice) {
        this.lowestUsedPrice = lowestUsedPrice;
    }

    public int getTotalUsed() {
        return totalUsed;
    }

    public void setTotalUsed(int totalUsed) {
        this.totalUsed = totalUsed;
    }

    public int getAmazonNewPrice() {
        return amazonNewPrice;
    }

    public void setAmazonNewPrice(int amazonNewPrice) {
        this.amazonNewPrice = amazonNewPrice;
    }

    public int getAmazonAmountSaved() {
        return amazonAmountSaved;
    }

    public void setAmazonAmountSaved(int amazonAmountSaved) {
        this.amazonAmountSaved = amazonAmountSaved;
    }

    public String getAmazonAvailability() {
        return amazonAvailability;
    }

    public void setAmazonAvailability(String amazonAvailability) {
        this.amazonAvailability = amazonAvailability;
    }

    public boolean isEligibleForPrime() {
        return eligibleForPrime;
    }

    public void setEligibleForPrime(boolean eligibleForPrime) {
        this.eligibleForPrime = eligibleForPrime;
    }

    public boolean isEligibleForSuperSaverShipping() {
        return eligibleForSuperSaverShipping;
    }

    public void setEligibleForSuperSaverShipping(boolean eligibleForSuperSaverShipping) {
        this.eligibleForSuperSaverShipping = eligibleForSuperSaverShipping;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
