package model.banking.account;

import com.fasterxml.jackson.annotation.JsonProperty;

public class _embedded {

    @JsonProperty("standingOrderResponseList")
    private StandingOrder[] standingOrders;

    public StandingOrder[] getStandingOrders() {
        return standingOrders;
    }

    public void setStandingOrders(StandingOrder[] standingOrders) {
        this.standingOrders = standingOrders;
    }
}
