package api;


import api.banking.AccountAPI;
import api.banking.TransactionAPI;
import model.banking.Transaction;
import org.joda.time.DateTime;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import static org.junit.Assert.*;

public class TransactionTest {

	private static final Logger log = LoggerFactory.getLogger(TransactionTest.class);

	private static final String ACCOUNT_NUMBER1 = "0000000000";
	private static final String ACCOUNT_IBAN1 = "DE77100000000000000000";
	private static final String ACCOUNT_IBAN2 = "DE50100000000000000001";
	private static final String VALUE_DATE = new DateTime(2017, 5, 1, 12, 0).toLocalDate().toString();

	@Test
	public void testAndGetTransaction() {
		Transaction newTransaction = TransactionAPI.createTransaction(1, ACCOUNT_IBAN1, ACCOUNT_IBAN2, VALUE_DATE, "TestDescription");

		assertEquals(1, newTransaction.getValue());
		assertEquals(ACCOUNT_IBAN1, newTransaction.getSourceAccount());
		assertEquals(ACCOUNT_IBAN2, newTransaction.getDestinationAccount());
		assertEquals(VALUE_DATE, newTransaction.getValueDate());
		assertEquals("TestDescription", newTransaction.getDescription());

		// Currently broken
		Collection<Transaction> transactions = AccountAPI.getTransactionsForAccount(ACCOUNT_NUMBER1);

		boolean foundTransaction = false;

		log.info("Found transactions: " + transactions.size());
		for(Transaction transaction : transactions) {
			if(transaction.getDescription().equals("TestDescription")) {
				foundTransaction = true;
				break;
			}
		}

		assertTrue(foundTransaction);
	}

}
