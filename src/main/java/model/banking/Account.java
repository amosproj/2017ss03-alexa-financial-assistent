package model.banking;


public class Account {

    private double creditcardLimit;
    private double balance;
    private String openingDate;
    private double creditLimit;
    private String number;
    private double interestRate;
    private double withdrawalFee;
    private String iban;

    public double getCreditcardLimit ()
    {
        return creditcardLimit;
    }

    public void setCreditcardLimit (double creditcardLimit)
    {
        this.creditcardLimit = creditcardLimit;
    }

    public double getBalance ()
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

    public double getCreditLimit ()
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

    public double getInterestRate ()
    {
        return interestRate;
    }

    public void setInterestRate (double interestRate)
    {
        this.interestRate = interestRate;
    }

    public double getWithdrawalFee ()
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