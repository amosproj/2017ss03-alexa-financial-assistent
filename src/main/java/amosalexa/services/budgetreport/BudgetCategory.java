package amosalexa.services.budgetreport;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BudgetCategory {

    private String colorCode;
    private String nameCategory;

    private Double limitCategory;
    private Double categoryAmountSpend;

    public BudgetCategory(String nameCategory, String colorCode
            , Double limitCategory, Double categoryAmountSpend) {
        this.nameCategory = nameCategory;
        this.colorCode = colorCode;
        this.limitCategory = limitCategory;
        this.categoryAmountSpend = categoryAmountSpend;
    }


    public String getAmountTotal() {
        return String.valueOf(round(categoryAmountSpend, 2));
    }

    public String getAmountPercentage() {
        Double per = categoryAmountSpend / limitCategory;
        return String.valueOf(round(per, 2));
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getNameCategory() {
        return nameCategory;
    }

    public String getLimitCategory() {
        return String.valueOf(limitCategory);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
