package amosalexa.services.transfertemplates;

import api.DynamoDBClient;
import api.DynamoDbStorable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.w3c.dom.Attr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TransferTemplate implements Comparable<TransferTemplate>, DynamoDbStorable {
    private int id;
    private String target;
    private double amount;
    private Date createdAt;

    public static Factory factory = (Factory<TransferTemplate>) TransferTemplate::new;
    private static String tableName = "transfer_template";

    private TransferTemplate() {
    }

    private TransferTemplate(String target, double amount) {
        this.target = target;
        this.amount = amount;
        this.createdAt = new Date();
    }

    public static TransferTemplate make(String target, double amount) {
        TransferTemplate transferTemplate = new TransferTemplate(target, amount);
        DynamoDBClient.instance.createItem(tableName, transferTemplate);
        DynamoDBClient.instance.putItem(tableName, transferTemplate);
        return transferTemplate;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public String getTarget() {
        return target;
    }

    public double getAmount() {
        return amount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TransferTemplate)) {
            return false;
        }

        TransferTemplate tt = (TransferTemplate) obj;

        return this == tt ||
                (id == tt.id && amount == tt.amount && target.equals(tt.target) && createdAt.equals(tt.createdAt));
    }

    @Override
    public int compareTo(TransferTemplate o) {
        if (id > o.id) {
            return 1;
        }
        if (id < o.id) {
            return -1;
        }
        return 0;
    }

    @Override
    public Map<String, AttributeValue> getDynamoDbItem() {
        Map<String, AttributeValue> map = new TreeMap<>();

        map.put("id", new AttributeValue().withN(Integer.toString(this.id)));
        map.put("target", new AttributeValue(this.target));
        map.put("amount", new AttributeValue().withN(Double.toString(this.amount)));
        map.put("createdAt", new AttributeValue().withN(Long.toString(this.createdAt.getTime())));

        return map;
    }

    @Override
    public Map<String, AttributeValue> getDynamoDbKey() {
        Map<String, AttributeValue> map = new TreeMap<>();
        map.put("id", new AttributeValue().withN(Integer.toString(this.id)));
        return map;
    }

    @Override
    public void setDynamoDbAttribute(String attributeName, AttributeValue attributeValue) {
        switch (attributeName) {
            case "id":
                this.id = Integer.parseInt(attributeValue.getN());
                break;
            case "target":
                this.target = attributeValue.getS();
                break;
            case "amount":
                this.amount = Double.parseDouble(attributeValue.getN());
                break;
            case "createdAt":
                this.createdAt = new Date(Long.parseLong(attributeValue.getN()));
                break;
            default:
                throw new RuntimeException("Unknown attribute");
        }
    }

}
