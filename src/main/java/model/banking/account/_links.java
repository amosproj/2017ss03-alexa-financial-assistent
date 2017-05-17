package model.banking.account;

import com.fasterxml.jackson.annotation.JsonProperty;

public class _links {

    private Self self;

    @JsonProperty("account")
    private AccountLink account;

    public Self getSelf() {
        return self;
    }

    public void setSelf(Self self) {
        this.self = self;
    }

    public AccountLink getAccount() {
        return account;
    }

    public void setAccount(AccountLink account) {
        this.account = account;
    }
}
