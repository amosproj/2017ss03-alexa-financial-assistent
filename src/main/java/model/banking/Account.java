package model.banking;


public class Account {

    private Number creditcardLimit;
    private Number balance;
    private String openingDate;
    private Number creditLimit;
    private String number;
    private Number interestRate;
    private Number withdrawalFee;
    private String iban;

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

    public void setBalance (double balance)
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
}