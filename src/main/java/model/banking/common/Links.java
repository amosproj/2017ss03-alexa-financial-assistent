package model.banking.common;

import model.banking.account.Transactions;

public class Links
{
    private Transactions transactions;

    private Self self;

    public Transactions getTransactions ()
    {
        return transactions;
    }

    public void setTransactions (Transactions transactions)
    {
        this.transactions = transactions;
    }

    public Self getSelf ()
    {
        return self;
    }

    public void setSelf (Self self)
    {
        this.self = self;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [transactions = "+transactions+", self = "+self+"]";
    }
}