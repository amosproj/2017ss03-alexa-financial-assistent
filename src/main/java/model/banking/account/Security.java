package model.banking.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties({"_links"})
public class Security {

    private Long securityId;

    private String isin;

    private String wkn;

    private String description;

    private int quantity;

    private double costPrice;

    private Date purchasingDate;

    private SECURITY_TYPE securityType;

    @Override
    public String toString() {
        return "Security{" +
                "securityId=" + securityId +
                ", isin='" + isin + '\'' +
                ", wkn='" + wkn + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", costPrice=" + costPrice +
                ", purchasingDate=" + purchasingDate +
                ", securityType=" + securityType +
                '}';
    }

    private enum SECURITY_TYPE {
        STOCK,
        BOND,
        FUND
    }

    public Long getSecurityId() {
        return securityId;
    }

    public void setSecurityId(Long securityId) {
        this.securityId = securityId;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getWkn() {
        return wkn;
    }

    public void setWkn(String wkn) {
        this.wkn = wkn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public Date getPurchasingDate() {
        return purchasingDate;
    }

    public void setPurchasingDate(Date purchasingDate) {
        this.purchasingDate = purchasingDate;
    }

    public SECURITY_TYPE getSecurityType() {
        return securityType;
    }

    public void setSecurityType(SECURITY_TYPE securityType) {
        this.securityType = securityType;
    }

}
