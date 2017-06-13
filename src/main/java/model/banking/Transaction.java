package model.banking;


import api.banking.AccountAPI;
import org.springframework.hateoas.ResourceSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Transaction extends ResourceSupport {

	private Number transactionId;
	private Number amount;
	private Number value;
	private String destinationAccount;
	private String sourceAccount;
	private String valueDate;
	private String description;
	private String payee;
	private String remitter;

	private static String getTransactionIdText(Transaction transaction){
		return "<break time=\"1s\"/>Nummer " + transaction.getTransactionId() + " ";
	}

	public static String getAskMoreTransactionText(){
		return "<break time=\"1s\"/> Möchtest du weitere Transaktionen hören";
	}

	private static String getTransactionFromAccountText(Transaction transaction) {
		return  Transaction.getTransactionIdText(transaction) + "Von deinem Konto auf das Konto von " + transaction.getPayee() +
				" in Höhe von <say-as interpret-as=\"unit\">€"
				+ Math.abs(transaction.getAmount().doubleValue()) + "</say-as>\n";
	}

	private static String getTransactionToAccountText(Transaction transaction) {
		return "Von " + transaction.getRemitter()+ " auf dein Konto in Höhe von <say-as interpret-as=\"unit\">€"
				+ Math.abs(transaction.getAmount().doubleValue()) + "</say-as>\n";
	}

	public static String getTransactionSizeText(int size){
		return "Du hast " + size + " Transaktionen. ";
	}

	/**
	 * checks if the transaction is from your account
	 * @param transaction transaction
	 * @return text for transaction
	 */
	public static String getTransactionText(Transaction transaction) {

		String transactionText = "";
		if (transaction.getSourceAccount() != null) {
			transactionText = Transaction.getTransactionToAccountText(transaction);
		}
		if (transaction.getDestinationAccount() != null) {
			transactionText = Transaction.getTransactionFromAccountText(transaction);
		}
		return transactionText;
	}

	/**
	 * request api for list of transaction @{@link Transaction}
	 * @return list of tranactions
	 */
	public static List<Transaction> getTransactions(Account account) {
		Collection<Transaction> transactions = AccountAPI.getTransactionsForAccount(account.getNumber());
		List<Transaction> txs = new ArrayList<>(transactions);
		Collections.reverse(txs);
		return txs;
	}

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

	public String getPayee() {
		if(payee == null){
			return getDestinationAccount();
		}
		return payee;
	}

	public Transaction setPayee(String payee) {
		this.payee = payee;
		return this;
	}

	public String getRemitter() {
		if(remitter == null){
			return getSourceAccount();
		}
		return remitter;
	}

	public Transaction setRemitter(String remitter) {
		this.remitter = remitter;
		return this;
	}
}
