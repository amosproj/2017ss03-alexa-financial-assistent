package amosalexa.services;

/**
 *
 * class which contains centralized information, especially account number due to the fact that we developed yet for one account
 */
public class AccountData {

    //** maybe later from session storage
    public static String ACCOUNT_DEFAULT = "9999999999";

    //** account with no money --> 1 Euro
    public static String ACCOUNT_NO_MONEY = "0000000333";

    //** account with few money --> 150 Euro
    public static String ACCOUNT_FEW_MONEY = "0000000444";

}
