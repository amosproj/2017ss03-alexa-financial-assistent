package model.banking;

import java.util.HashMap;
import java.util.Map;

public class Account {

    private Number creditcardLimit;
    private final static String creditcardLimitSlot = "kreditkartenlimit";
    private String creditcardLimitText = "Dein " + creditcardLimitSlot + " beträgt <say-as interpret-as=\"unit\">€" + creditcardLimit + "</say-as>\n";

    private Number balance;
    private final static String balanceSlot = "kontostand";
    private String balanceText = "Dein " + balanceSlot + " beträgt <say-as interpret-as=\"unit\">€" + balance + "</say-as>\n";

    private String openingDate;
    private final static String openingDateSlot = "eröffnungsdatum";
    private String openingDateText = "Dein " + openingDateSlot + " war " + openingDate;

    private Number creditLimit;
    private final static String creditLimitSlot = "kreditlimit";
    private String creditLimitText = "Dein " + creditLimitSlot + " beträgt <say-as interpret-as=\"unit\">€" + creditcardLimit + "</say-as>\n";

    private String number;
    private final static String  numberSlot = "kontonummer";
    private String numberText = "Deine " + numberSlot + " lautet " + number;

    private Number interestRate;
    private final static String interestRateSlot = "zinssatz";
    private String interestRateText = "Dein " + interestRateSlot + " ist aktuell " + interestRate;

    private Number withdrawalFee;
    private final static String withdrawalFeeSlot = "abhebegebühr";
    private String withdrawalFeeText = "Deine " + withdrawalFeeSlot + " beträgt <say-as interpret-as=\"unit\">€" + withdrawalFee + "</say-as>\n";

    private String iban;
    private final static String ibanSlot = "iban";
    private String ibanText = "Deine " + ibanSlot + " lautet " + iban;

    private final Map<String, String> speechTexts = new HashMap<String, String>(){{
        put(creditcardLimitSlot, creditcardLimitText);
        put(balanceSlot, balanceText);
        put(openingDateSlot, openingDateText);
        put(creditcardLimitSlot, creditLimitText);
        put(numberSlot, numberText);
        put(interestRateSlot, interestRateText);
        put(withdrawalFeeSlot, withdrawalFeeText);
        put(ibanSlot, ibanText);
    }};

    public Number getCreditcardLimit ()
    {
        return creditcardLimit;
    }

    public void setCreditcardLimit (double creditcardLimit)
    {
        this.creditcardLimit = creditcardLimit;
    }

    public Number getBalance ()
    {
        return balance;
    }

    public void setBalance (Number balance)
    {
        this.balance = balance;
    }

    public String getOpeningDate ()
    {
        return openingDate;
    }

    public void setOpeningDate (String openingDate)
    {
        this.openingDate = openingDate;
    }

    public Number getCreditLimit ()
    {
        return creditLimit;
    }

    public void setCreditLimit (double creditLimit)
    {
        this.creditLimit = creditLimit;
    }

    public String getNumber ()
    {
        return number;
    }

    public void setNumber (String number)
    {
        this.number = number;
    }

    public Number getInterestRate ()
    {
        return interestRate;
    }

    public void setInterestRate (double interestRate)
    {
        this.interestRate = interestRate;
    }

    public Number getWithdrawalFee ()
    {
        return withdrawalFee;
    }

    public void setWithdrawalFee (double withdrawalFee)
    {
        this.withdrawalFee = withdrawalFee;
    }

    public String getIban ()
    {
        return iban;
    }

    public void setIban (String iban)
    {
        this.iban = iban;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [creditcardLimit = "+creditcardLimit+", balance = "+balance+", openingDate = "+openingDate+", creditLimit = "+creditLimit+", number = "+number+", interestRate = "+interestRate+", withdrawalFee = "+withdrawalFee+", iban = "+iban+"]";
    }

    public static String getCreditcardLimitSlot() {
        return creditcardLimitSlot;
    }

    public static String getBalanceSlot() {
        return balanceSlot;
    }

    public static String getOpeningDateSlot() {
        return openingDateSlot;
    }

    public static String getCreditLimitSlot() {
        return creditLimitSlot;
    }

    public static String getNumberSlot() {
        return numberSlot;
    }

    public static String getInterestRateSlot() {
        return interestRateSlot;
    }

    public static String getWithdrawalFeeSlot() {
        return withdrawalFeeSlot;
    }

    public static String getIbanSlot() {
        return ibanSlot;
    }

    public String getCreditcardLimitText() {
        return creditcardLimitText;
    }

    public String getBalanceText() {
        return balanceText;
    }

    public String getOpeningDateText() {
        return openingDateText;
    }

    public String getCreditLimitText() {
        return creditLimitText;
    }

    public String getNumberText() {
        return numberText;
    }

    public String getInterestRateText() {
        return interestRateText;
    }

    public String getWithdrawalFeeText() {
        return withdrawalFeeText;
    }

    public String getIbanText() {
        return ibanText;
    }

    public Map<String, String> getSpeechTexts() {
        return speechTexts;
    }
}