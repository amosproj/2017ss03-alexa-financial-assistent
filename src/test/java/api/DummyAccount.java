package api;

import org.joda.time.DateTime;

public class DummyAccount {

    private String number = "0000000000";

    private double balance = 1.0;

    private String openingDate = new DateTime(2017, 5, 1, 12, 0).toLocalDate().toString();


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(String openingDate) {
        this.openingDate = openingDate;
    }
}
