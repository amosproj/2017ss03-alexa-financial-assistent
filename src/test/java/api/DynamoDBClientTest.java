package api;

import amosalexa.services.transfertemplates.TransferTemplate;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DynamoDBClientTest {
    private DynamoDBClient client;

    @Before public void initialize() {
        client = new DynamoDBClient();
    }

    @Test
    public void getItemsTest() {
        client.getItems("transfer_template", TransferTemplate.factory);
    }

    @Test
    public void putAndDeleteItemTest() {
        TransferTemplate transferTemplate1 = new TransferTemplate("max", 10.0);
        TransferTemplate transferTemplate2 = new TransferTemplate("johannes", 10.0);

        client.putItem("transfer_template", transferTemplate1);
        client.putItem("transfer_template", transferTemplate2);

        List<TransferTemplate> transferTemplateList = client.getItems("transfer_template", TransferTemplate.factory);

        assert(transferTemplateList.contains(transferTemplate1));
        assert(transferTemplateList.contains(transferTemplate2));

        client.deleteItem("transfer_template", transferTemplate1);

        transferTemplateList = client.getItems("transfer_template", TransferTemplate.factory);

        assertFalse(transferTemplateList.contains(transferTemplate1));
        assert(transferTemplateList.contains(transferTemplate2));

        client.deleteItem("transfer_template", transferTemplate2);

        transferTemplateList = client.getItems("transfer_template", TransferTemplate.factory);

        assertFalse(transferTemplateList.contains(transferTemplate1));
        assertFalse(transferTemplateList.contains(transferTemplate2));
    }

}