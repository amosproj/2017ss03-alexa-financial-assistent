package model.db;

import amosalexa.services.budgettracker.BudgetManager;
import api.aws.DynamoDbClient;
import api.aws.DynamoDbStorable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a category for the budget tracker feature.
 * Every category has a (unique) name and a spending limit.
 * Categories are persisted in the Dynamo DB storage.
 */
public class Category implements Comparable<Category>, DynamoDbStorable {

    public static String TABLE_NAME = "category";

    private int id;

    //TODO name should be the primary key (and uniqe)
    private String name;

    /**
     * Spending limit for this category
     */
    private double limit;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, double limit, double spending) {
        this.name = name;
        this.limit = limit;
    }

    @Override
    public Map<String, AttributeValue> getDynamoDbItem() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("id", new AttributeValue().withN(Integer.toString(id)));
        map.put("name", new AttributeValue(name));
        map.put("limit", new AttributeValue(String.valueOf(limit)));
        return map;
    }

    @Override
    public Map<String, AttributeValue> getDynamoDbKey() {
        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put("id", new AttributeValue().withN(Integer.toString(id)));
        return keyMap;
    }

    @Override
    public void setDynamoDbAttribute(String attributeName, AttributeValue attributeValue) throws UnknownAttributeException {
        switch (attributeName) {
            case "id":
                this.id = Integer.parseInt(attributeValue.getN());
                break;
            case "name":
                this.name = attributeValue.getS();
                break;
            case "limit":
                this.limit = Double.parseDouble(attributeValue.getS());
                break;
        }
    }

    public Long getSpendingPercentage() {
        return Math.round((getSpending() / limit) * 100);
    }

    public double getSpending() {
        return BudgetManager.instance.getTotalSpendingForCategory(this.getId());
    }

    @Override
    public int compareTo(Category o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", limit=" + limit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Category category = (Category) o;

        if (id != category.id) return false;
        if (Double.compare(category.limit, limit) != 0) return false;
        return name != null ? name.equals(category.name) : category.name == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(limit);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public static String categoryListText(){
        StringBuilder categoryNames = new StringBuilder();
        List<Category> categories = DynamoDbClient.instance.getItems(Category.TABLE_NAME, Category::new);
        for(int i = 0; i < categories.size() - 1; i++){
            categoryNames.append(categories.get(i).getName()).append(", ");
        }
        categoryNames.append("oder ").append(categories.get(categories.size() - 1).getName());
        return categoryNames.toString();
    }
}