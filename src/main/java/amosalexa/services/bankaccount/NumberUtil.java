package amosalexa.services.bankaccount;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class NumberUtil {

    /**
     * default format for currency value e.g 32,34
     * @param number input
     * @return formatted string
     */
    public static String getCurrencyFormat(Number number){
       return format(number.doubleValue(), "#0.00", 2);
    }

    /**
     * round a double value
     * @param value input value
     * @param places decimal places round to
     * @return formatted string
     */
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    /**
     * applies a format
     * @param value input
     * @param format format
     * @param places decimal digits to round
     * @return formatted string
     */
    private static String format(double value, String format, int places){
        NumberFormat formatter = new DecimalFormat(format);
        return formatter.format(round(value, places));
    }

    /**
     * return only the decimal of a number
     * @param val input val
     * @return decimal as integers
     */
    public static int getDecimal(Number val){
        BigDecimal bd = new BigDecimal( val.doubleValue() - Math.floor( val.doubleValue()));
        bd = bd.setScale(4,RoundingMode.HALF_DOWN);
       return Double.valueOf(bd.doubleValue() * 100).intValue();
    }

}
