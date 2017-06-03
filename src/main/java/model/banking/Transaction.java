package model.banking;


import org.springframework.hateoas.ResourceSupport;

public class Transaction extends ResourceSupport {

	private Number transactionId;
	private Number amount;
	private Number value;
	private String destinationAccount;
	private String sourceAccount;
	private String valueDate;
	private String description;

	public Number getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Number transactionId) {
		this.transactionId = transactionId;
	}

	public Number getAmount() {
		return amount;
	}

	public void setAmount(Number amount) {
		this.amount = amount;
	}

	public Number getValue() {
		return value;
	}

	public void setValue(Number value) {
		this.value = value;
	}

	public String getDestinationAccount() {
		return destinationAccount;
	}

	public void setDestinationAccount(String destinationAccount) {
		this.destinationAccount = destinationAccount;
	}

	public String getSourceAccount() {
		return sourceAccount;
	}

	public void setSourceAccount(String sourceAccount) {
		this.sourceAccount = sourceAccount;
	}

	public String getValueDate() {
		return valueDate;
	}

	public void setValueDate(String valueDate) {
		this.valueDate = valueDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
