package model.banking.account;

import java.util.Date;

/*
  This class represents a standing order.
 */
public class StandingOrder {

    public enum ExecutionRate {
        MONTHLY,
        QUARTERLY,
        HALF_YEARLY,
        YEARLY
    }

    public enum StandingOrderStatus {
        ACTIVE,
        INACTIVE
    }

    private Long standingOrderId;

    private String payee;

    private double amount;

    private String sourceAccount;

    private String destinationAccount;

    private Date firstExecution;

    private ExecutionRate executionRate;

    private String description;

    private StandingOrderStatus status;

    private _links _links;

    public Long getStandingOrderId() {
        return standingOrderId;
    }

    public void setStandingOrderId(Long standingOrderId) {
        this.standingOrderId = standingOrderId;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public Date getFirstExecution() {
        return firstExecution;
    }

    public void setFirstExecution(Date firstExecution) {
        this.firstExecution = firstExecution;
    }

    public ExecutionRate getExecutionRate() {
        return executionRate;
    }

    public void setExecutionRate(ExecutionRate executionRate) {
        this.executionRate = executionRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StandingOrderStatus getStatus() {
        return status;
    }

    public void setStatus(StandingOrderStatus status) {
        this.status = status;
    }

    public model.banking.account._links get_links() {
        return _links;
    }

    public void set_links(model.banking.account._links _links) {
        this._links = _links;
    }

    public String getExecutionRateString() {
        if (this.executionRate.equals(ExecutionRate.MONTHLY))
            return "monatlich ";
        if (this.executionRate.equals(ExecutionRate.QUARTERLY))
            return "vierteljaehrlich ";
        if (this.executionRate.equals(ExecutionRate.HALF_YEARLY))
            return "halbjaehrlich ";
        if (this.executionRate.equals(ExecutionRate.YEARLY))
            return "jaehrlich ";
        else return "";
    }
}