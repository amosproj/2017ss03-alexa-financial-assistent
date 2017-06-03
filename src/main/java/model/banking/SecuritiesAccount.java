package model.banking;


import java.util.Date;

public class SecuritiesAccount {

	private Number securitiesAccountId;
	private String clearingAccount;
	private Date openingDate;

	public Number getSecuritiesAccountId() {
		return securitiesAccountId;
	}

	public void setSecuritiesAccountId(Number securitiesAccountId) {
		this.securitiesAccountId = securitiesAccountId;
	}

	public String getClearingAccount() {
		return clearingAccount;
	}

	public void setClearingAccount(String clearingAccount) {
		this.clearingAccount = clearingAccount;
	}

	public Date getOpeningDate() {
		return openingDate;
	}

	public void setOpeningDate(Date openingDate) {
		this.openingDate = openingDate;
	}

}
