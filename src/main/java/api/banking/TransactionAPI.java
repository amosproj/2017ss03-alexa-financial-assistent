package api.banking;


import model.banking.Transaction;
import org.slf4j.LoggerFactory;

public class TransactionAPI {

	/**
	 * Logger
	 */
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(TransactionAPI.class);

	private static BankingRESTClient bankingRESTClient = BankingRESTClient.getInstance();

	public static Transaction createTransaction(Number amount, String sourceAccount, String destinationAccount, String valueDate, String description) {
		Transaction newTransaction = new Transaction();
		newTransaction.setAmount(amount);
		newTransaction.setSourceAccount(sourceAccount);
		newTransaction.setDestinationAccount(destinationAccount);
		newTransaction.setValueDate(valueDate);
		newTransaction.setDescription(description);

		return createTransaction(newTransaction);
	}

	public static Transaction createTransaction(Transaction newTransaction) {
		return (Transaction) bankingRESTClient.postBankingModelObject("/transactions", newTransaction, Transaction.class);
	}

}
