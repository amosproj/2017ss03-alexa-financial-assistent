package model.db;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "category")
public class CategoryDB {

    private String id;

    private String name;

    private double limit;

    private String accountNumber;

    public CategoryDB() {
    }

    public CategoryDB(String accountNumber, String name, double limit) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.limit = limit;
    }

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    public String getId() {
        return id;
    }

    public CategoryDB setId(String id) {
        this.id = id;
        return this;
    }

    @DynamoDBAttribute
    public String getName() {
        return name;
    }

    public CategoryDB setName(String name) {
        this.name = name;
        return this;
    }

    @DynamoDBAttribute
    public double getLimit() {
        return limit;
    }

    public CategoryDB setLimit(double limit) {
        this.limit = limit;
        return this;
    }

    @DynamoDBAttribute
    public String getAccountNumber() {
        return accountNumber;
    }

    public CategoryDB setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }
}