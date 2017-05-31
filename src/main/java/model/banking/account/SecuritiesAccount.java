package model.banking.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties({"_links"})
public class SecuritiesAccount {

    private Long securitiesAccountId;

    private String clearingAccount;

    private String openingDate;

    @JsonProperty("_embedded")
    private EmbeddedSecurities embeddedSecurities;

    public Long getSecuritiesAccountId() {
        return securitiesAccountId;
    }

    public void setSecuritiesAccountId(Long securitiesAccountId) {
        this.securitiesAccountId = securitiesAccountId;
    }

    public String getClearingAccount() {
        return clearingAccount;
    }

    public void setClearingAccount(String clearingAccount) {
        this.clearingAccount = clearingAccount;
    }

    public String getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(String openingDate) {
        this.openingDate = openingDate;
    }

    public List<Security> getSecurities() {
        return (embeddedSecurities != null && embeddedSecurities.getSecurities().length > 0) ?
                new ArrayList(Arrays.asList(embeddedSecurities.getSecurities()))
                : null;
    }

    @Override
    public String toString() {
        return "SecuritiesAccount{" +
                "securitiesAccountId=" + securitiesAccountId +
                ", clearingAccount='" + clearingAccount + '\'' +
                ", openingDate='" + openingDate + '\'' +
                '}';
    }
}
