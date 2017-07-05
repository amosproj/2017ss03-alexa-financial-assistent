package amosalexa.services.contacts;

import api.aws.DynamoDbClient;
import model.db.Contact;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ContactTest {

    @Test
    public void createAndDeleteContact() {
        Contact contact = new Contact("Lucas", "DE12345678901234");

        DynamoDbClient.instance.putItem(Contact.TABLE_NAME + "_test", contact);

        List<Contact> contacts = DynamoDbClient.instance.getItems(Contact.TABLE_NAME + "_test", Contact::new);

        assertTrue(contacts.contains(contact));

        DynamoDbClient.instance.deleteItem(Contact.TABLE_NAME + "_test", contact);

        contacts = DynamoDbClient.instance.getItems(Contact.TABLE_NAME + "_test", Contact::new);

        assertFalse(contacts.contains(contact));
    }

    /*@Test
    public void createSomeContacts() {
        Contact contact1 = new Contact("Bob Marley", "DE12345678901234");
        Contact contact2 = new Contact("Bob Ray Simmons", "DE12345678901234");

        DynamoDbClient.instance.putItem(Contact.TABLE_NAME, contact1);
        DynamoDbClient.instance.putItem(Contact.TABLE_NAME, contact2);
    }*/

}