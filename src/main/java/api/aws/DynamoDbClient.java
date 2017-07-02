package api.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DynamoDbClient {

    private AmazonDynamoDBClient dynamoDB;
    public final static DynamoDbClient instance = new DynamoDbClient();

    /**
     * Creates a new DynamoDB client.
     */
    private DynamoDbClient() {
        // TODO: Use credentials of common AMOS ALEXA account
        // Currently we are using the credentials of Julian's private AWS account
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAIUOLL3674W3T67IQ", "X4KiAVCPab5aiW0c/93y7PnABVsPlj6YYqmfSkng");
        dynamoDB = new AmazonDynamoDBClient(credentials);
        dynamoDB.setEndpoint("http://dynamodb.us-east-1.amazonaws.com");
    }

    /**
     * Retrieves a list of all entries in the table.
     *
     * @param tableName name of the DynamoDB table
     * @param factory   factory that creates new instances
     * @param <T>       implementation of {@link DynamoDbStorable}
     * @return list of all items
     */
    public <T extends DynamoDbStorable> List<T> getItems(String tableName, DynamoDbStorable.Factory<T> factory) {
        ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
        ScanResult result = dynamoDB.scan(scanRequest);

        LinkedList<T> items = new LinkedList<>();

        for (Map<String, AttributeValue> dynamoDbItem : result.getItems()) {
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
     * Gets an item from the DynamoDB.
     *
     * @param tableName name of the DynamoDB table
     * @param id        id of the item to get
     * @param factory   implementation of the {@link DynamoDbStorable.Factory} interface which is used to create the items
     * @param <T>       type of the item to get
     * @return the item with the specified id
     */
    public <T extends DynamoDbStorable> T getItem(String tableName, int id, DynamoDbStorable.Factory<T> factory) {
        T newItem = factory.newInstance();
        newItem.setId(id);
        GetItemResult result = dynamoDB.getItem(tableName, newItem.getDynamoDbKey());

        if (result == null || result.getItem() == null) {
            return null;
        }

        for (Map.Entry<String, AttributeValue> attribute : result.getItem().entrySet()) {
            try {
                newItem.setDynamoDbAttribute(attribute.getKey(), attribute.getValue());
            } catch (DynamoDbStorable.UnknownAttributeException e) {
                e.printStackTrace();
            }
        }

        return newItem;
    }

    /**
     * Inserts or updates the item.
     * <p>
     * Creates a new item if the id of the item is {@code 0}, otherwise updates the item.
     *
     * @param tableName name of the DynamoDB table
     * @param item      item to insert or update
     * @param <T>       implementation of {@link DynamoDbStorable}
     */
    public <T extends DynamoDbStorable> void putItem(String tableName, T item) {
        if (item.getId() == 0) {
            // Create a new item by fetching the id first.
            Map<String, AttributeValue> request = new TreeMap<>();
            request.put("table_name", new AttributeValue(tableName));

            // Try to get the last id and leave it at 0 if cannot find last id
            int id = 0;
            try {
                GetItemResult result = dynamoDB.getItem("last_ids", request);
                if (result.getItem() != null) {
                    AttributeValue aid = result.getItem().get("id");
                    id = Integer.parseInt(aid.getN());
                }
            } catch (ResourceNotFoundException ignored) {
            }

            // Increment the id
            id++;

            // Set the id to the item and leave other fields unchanged
            item.setId(id);

            // Store the id
            request.put("id", new AttributeValue().withN(Integer.toString(id)));
            dynamoDB.putItem("last_ids", request);
        }

        dynamoDB.putItem(tableName, item.getDynamoDbItem());
    }

    /**
     * Deletes the item.
     *
     * @param tableName name of the DynamoDB table
     * @param item      item to delete
     * @param <T>       implementation of {@link DynamoDbStorable}
     */
    public <T extends DynamoDbStorable> void deleteItem(String tableName, T item) {
        dynamoDB.deleteItem(tableName, item.getDynamoDbKey());
    }

    /**
     * Completely wipes the table.
     *
     * @param tableName table to wipe
     */
    public <T extends DynamoDbStorable> void clearItems(String tableName, DynamoDbStorable.Factory<T> factory) {
        List<T> items = getItems(tableName, factory);
        for (DynamoDbStorable item : items) {
            deleteItem(tableName, item);
        }
    }

}
