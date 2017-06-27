package amosalexa.services.budgetreport;

public class BudgetCategory {

    private String totalAmountSpend;
    private String colorCode;
    private String nameCategory;
    private String limitCategory;

    public BudgetCategory(String nameCategory, String colorCode, String totalAmountSpend, String limitCategory) {
        this.nameCategory = nameCategory;
        this.colorCode = colorCode;
        this.totalAmountSpend = totalAmountSpend;
        this.limitCategory = limitCategory;
    }


    public String getTotalAmountSpend() {
        return totalAmountSpend;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getNameCategory() {
        return nameCategory;
    }

    public String getLimitCategory() {
        return limitCategory;
    }
}
