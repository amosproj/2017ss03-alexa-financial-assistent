package api;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DynamoDBClient {

    private AmazonDynamoDBClient dynamoDB;
    public final static DynamoDBClient instance = new DynamoDBClient();

    /**
     * Creates a new DynamoDB client.
     */
    public DynamoDBClient() {
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAIUOLL3674W3T67IQ", "X4KiAVCPab5aiW0c/93y7PnABVsPlj6YYqmfSkng");
        dynamoDB = new AmazonDynamoDBClient(credentials);
        dynamoDB.setEndpoint("http://dynamodb.us-east-1.amazonaws.com");
    }

    /**
     * Retrieves a list of all entries in the table.
     *
     * @param tableName name of the DynamoDB table
     * @param factory factory that creates new instances
     * @param <T> implementation of {@link DynamoDbStorable}
     * @return list of all items
     */
    public <T extends DynamoDbStorable> List<T> getItems(String tableName, DynamoDbStorable.Factory<T> factory) {
        ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
        ScanResult result = dynamoDB.scan(scanRequest);

        LinkedList<T> items = new LinkedList<>();

        for (Map<String, AttributeValue> dynamoDbItem : result.getItems()){
            T item = factory.newInstance();

            for (Map.Entry<String, AttributeValue> attribute : dynamoDbItem.entrySet()) {
                try {
                    item.setDynamoDbAttribute(attribute.getKey(), attribute.getValue());
                } catch (DynamoDbStorable.UnknownAttributeException e) {
                    e.printStackTrace();
                }
            }

            items.add(item);
        }

        return items;
    }

    public <T extends DynamoDbStorable> T getItem(String tableName, int id, DynamoDbStorable.Factory<T> factory) {
        T newItem = factory.newInstance();
        newItem.setId(id);
        GetItemResult result = dynamoDB.getItem(tableName, newItem.getDynamoDbKey());

        for (Map.Entry<String, AttributeValue> attribute : result.getItem().entrySet()) {
            try {
                newItem.setDynamoDbAttribute(attribute.getKey(), attribute.getValue());
            } catch (DynamoDbStorable.UnknownAttributeException e) {
                e.printStackTrace();
            }
        }

        return newItem;
    }

    public <T extends DynamoDbStorable> void createItem(String tableName, T newItem) {
        Map<String, AttributeValue> request = new TreeMap<>();
        request.put("table_name", new AttributeValue(tableName));

        int id = 0;
        try {
            GetItemResult result = dynamoDB.getItem("last_ids", request);
            if (result.getItem() != null) {
                AttributeValue aid = result.getItem().get("id");
                id = Integer.parseInt(aid.getN());
            }
        } catch (ResourceNotFoundException ignored) {
        }

        id++;
        newItem.setId(id);

        // now store the id
        request.put("id", new AttributeValue().withN(Integer.toString(id)));
        dynamoDB.putItem("last_ids", request);

        putItem(tableName, newItem);
    }

    /**
     * Inserts or updates the item.
     *
     * @param tableName name of the DynamoDB table
     * @param item item to insert or update
     * @param <T> implementation of {@link DynamoDbStorable}
     */
    public <T extends DynamoDbStorable> void putItem(String tableName, T item) {
        dynamoDB.putItem(tableName, item.getDynamoDbItem());
    }


    /**
     * Deletes the item.
     *
     * @param tableName name of the DynamoDB table
     * @param item item to delete
     * @param <T> implementation of {@link DynamoDbStorable}
     */
    public <T extends DynamoDbStorable> void deleteItem(String tableName, T item) {
        dynamoDB.deleteItem(tableName, item.getDynamoDbKey());
    }

}
