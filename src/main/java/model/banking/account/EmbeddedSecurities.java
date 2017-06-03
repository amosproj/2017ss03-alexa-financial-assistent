package model.banking.account;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmbeddedSecurities {

    @JsonProperty("securityResponseList")
    private Security[] securities;

    public Security[] getSecurities() {
        return securities;
    }

    public void setSecurities(Security[] securities) {
        this.securities = securities;
    }
}
