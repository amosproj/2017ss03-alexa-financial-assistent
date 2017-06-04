package model.banking;

import org.springframework.hateoas.ResourceSupport;

import java.util.Date;

/*
  This class represents a standing order.
 */
public class StandingOrder extends ResourceSupport {

    private Number standingOrderId;
    private String payee;
    private Number amount;
    private String sourceAccount;
    private String destinationAccount;
    private String firstExecution;
    private ExecutionRate executionRate;
    private String description;
    private StandingOrderStatus status;

    public Number getStandingOrderId() {
        return standingOrderId;
    }

    public void setStandingOrderId(Number standingOrderId) {
        this.standingOrderId = standingOrderId;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public Number getAmount() {
        return amount;
    }

    public void setAmount(Number amount) {
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

    public String getFirstExecution() {
        return firstExecution;
    }

    public void setFirstExecution(String firstExecution) {
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

    public boolean isSavingsPlanStandingOrder() {
        //FIXME hardcoded savings account iban?
        return destinationAccount.equals("DE39100000007777777777");
    }

    public String getSpeechOutput() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dauerauftrag Nummer ").append(standingOrderId).append(": ").append("Ueberweise ").
                append(getExecutionRateString()).append(amount).append(" Euro ").append(isSavingsPlanStandingOrder() ? "auf dein Sparkonto" : "an "
                + payee).append(". ");
        return builder.toString();
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
}