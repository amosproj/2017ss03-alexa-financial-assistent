package api;

import amosalexa.services.transfertemplates.TransferTemplate;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class DynamoDbClientTest {
    private DynamoDbClient client = DynamoDbClient.instance;

    @Test
    public void getItemsTest() {
        client.getItems(TransferTemplate.TABLE_NAME, TransferTemplate.factory);
    }

    @Test
    public void putAndDeleteItemTest() {
        TransferTemplate transferTemplate1 = TransferTemplate.make("max", 10.0);
        TransferTemplate transferTemplate2 = TransferTemplate.make("johannes", 10.0);

        assertEquals(transferTemplate1.getId() + 1, transferTemplate2.getId());

        List<TransferTemplate> transferTemplateList = client.getItems(TransferTemplate.TABLE_NAME, TransferTemplate.factory);

        assert(transferTemplateList.contains(transferTemplate1));
        assert(transferTemplateList.contains(transferTemplate2));

        client.deleteItem(TransferTemplate.TABLE_NAME, transferTemplate1);

        transferTemplateList = client.getItems("transfer_template", TransferTemplate.factory);

        assertFalse(transferTemplateList.contains(transferTemplate1));
        assert(transferTemplateList.contains(transferTemplate2));

        client.deleteItem(TransferTemplate.TABLE_NAME, transferTemplate2);

        transferTemplateList = client.getItems("transfer_template", TransferTemplate.factory);

        assertFalse(transferTemplateList.contains(transferTemplate1));
        assertFalse(transferTemplateList.contains(transferTemplate2));
    }

    @Test
    public void createAndDeleteItemTest() {
        TransferTemplate mockTemplate = new TransferTemplate(0) {
            TransferTemplate init() {
                this.createdAt = new Date();
                this.target = "alex";
                this.amount = 5.0;
                return this;
            }
        }.init();

        assertEquals(0, mockTemplate.getId());

        client.putItem(TransferTemplate.TABLE_NAME, mockTemplate);

        assertNotEquals(0, mockTemplate.getId());

        client.deleteItem(TransferTemplate.TABLE_NAME, mockTemplate);

        List<TransferTemplate> transferTemplateList = client.getItems("transfer_template", TransferTemplate.factory);

        assertFalse(transferTemplateList.contains(mockTemplate));
    }

}