package amosalexa.services.bankaccount.banktransfer;

public class BankAccount {
    private String namePerson;
    private String accountNumber;
    private String iban;

    public String getNamePerson() {
        return namePerson;
    }
    public void setNamePerson(String namePerson) {
        this.namePerson = namePerson;
    }


    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getIban() {
        return iban;
    }
    public void setIban(String iban) {
        this.iban = iban;
    }

    BankAccount(String namePerson, String accountNumber, String iban) {
        this.namePerson = namePerson;
        this.accountNumber = accountNumber;
        this.iban = iban;
    }

}
