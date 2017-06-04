package api.banking;


import model.banking.Transaction;
import org.slf4j.LoggerFactory;

public class TransactionAPI {

	/**
	 * Logger
	 */
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(TransactionAPI.class);

	private static BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();

	/**
	 * Create transaction
	 *
	 * @param amount          the amount
	 * @param sourceIban      the source IBAN (NOT ACCOUNT NUMBER)
	 * @param destinationIban the destination IBAN (NOT ACCOUNT NUMBER)
	 * @param valueDate       the value date
	 * @param description     the description
	 * @return the transaction
	 */
	public static Transaction createTransaction(Number amount, String sourceIban, String destinationIban, String valueDate, String description) {
		Transaction newTransaction = new Transaction();
		newTransaction.setAmount(amount);
		newTransaction.setSourceAccount(sourceIban);
		newTransaction.setDestinationAccount(destinationIban);
		newTransaction.setValueDate(valueDate);
		newTransaction.setDescription(description);

		return createTransaction(newTransaction);
	}

	public static Transaction createTransaction(Transaction newTransaction) {
		return (Transaction) bankingRESTClient.postBankingModelObject("/transactions", newTransaction, Transaction.class);
	}

}
