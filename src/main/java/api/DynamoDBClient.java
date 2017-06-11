package api;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DynamoDBClient {

    private AmazonDynamoDBClient dynamoDB;

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
