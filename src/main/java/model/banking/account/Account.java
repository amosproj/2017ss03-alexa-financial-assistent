package model.banking.account;

import model.banking.Links;

public class Account
{
    private String creditcardLimit;

    private String balance;

    private String openingDate;

    private String creditLimit;

    private Links _links;

    private String number;

    private String interestRate;

    private String withdrawalFee;

    private String iban;

    public String getCreditcardLimit ()
    {
        return creditcardLimit;
    }

    public void setCreditcardLimit (String creditcardLimit)
    {
        this.creditcardLimit = creditcardLimit;
    }

    public String getBalance ()
    {
        return balance;
    }

    public void setBalance (String balance)
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

    public String getCreditLimit ()
    {
        return creditLimit;
    }

    public void setCreditLimit (String creditLimit)
    {
        this.creditLimit = creditLimit;
    }

    public Links get_links ()
    {
        return _links;
    }

    public void set_links (Links _links)
    {
        this._links = _links;
    }

    public String getNumber ()
    {
        return number;
    }

    public void setNumber (String number)
    {
        this.number = number;
    }

    public String getInterestRate ()
    {
        return interestRate;
    }

    public void setInterestRate (String interestRate)
    {
        this.interestRate = interestRate;
    }

    public String getWithdrawalFee ()
    {
        return withdrawalFee;
    }

    public void setWithdrawalFee (String withdrawalFee)
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
        return "ClassPojo [creditcardLimit = "+creditcardLimit+", balance = "+balance+", openingDate = "+openingDate+", creditLimit = "+creditLimit+", Links = "+_links+", number = "+number+", interestRate = "+interestRate+", withdrawalFee = "+withdrawalFee+", iban = "+iban+"]";
    }
}