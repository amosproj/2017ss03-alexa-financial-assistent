package model.banking.account;


import org.springframework.hateoas.ResourceSupport;

import java.util.Date;

public class CardResponse extends ResourceSupport {

	public enum CardType {
		DEBIT,
		CREDIT
	}

	public enum Status {
		ACTIVE,
		INACTIVE,
		BLOCKED,
		EXPIRED
	}

	public Number cardId;
	public String cardNumber;
	public CardType cardType;
	public Status status;
	public Date expirationDate;

}
