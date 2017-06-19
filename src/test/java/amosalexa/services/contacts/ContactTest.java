package amosalexa.services.contacts;

import api.DynamoDbClient;
import model.banking.Contact;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ContactTest {

    @Test
    public void createAndDeleteContact() {
        Contact contact = new Contact("Lucas", "DE12345678901234");

        DynamoDbClient.instance.putItem(Contact.TABLE_NAME, contact);

        List<Contact> contacts = DynamoDbClient.instance.getItems(Contact.TABLE_NAME, Contact::new);

        assertTrue(contacts.contains(contact));

        DynamoDbClient.instance.deleteItem(Contact.TABLE_NAME, contact);

        contacts = DynamoDbClient.instance.getItems(Contact.TABLE_NAME, Contact::new);

        assertFalse(contacts.contains(contact));
    }

}