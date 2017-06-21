package model.banking;

import api.aws.DynamoDbStorable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a contact to which we can transfer money.
 */
public class Contact implements Comparable<Contact>, DynamoDbStorable {
    private int id;
    private String name;
    private String iban;
    private Date createdAt;

    //TODO contact_test is only for test purpose
    public static final String TABLE_NAME = "contact_test"; //"contact";

    public Contact() {
    }

    public Contact(String name, String iban) {
        this.name = name;
        this.iban = iban;
        this.createdAt = new Date();
    }

    public Contact(Integer id) {
        this.id = id;
    }

    @Override
    public Map<String, AttributeValue> getDynamoDbItem() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("id", new AttributeValue().withN(Integer.toString(id)));
        map.put("name", new AttributeValue(name));
        map.put("iban", new AttributeValue(iban));
        map.put("createdAt", new AttributeValue().withN(Long.toString(createdAt.getTime())));
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
            case "iban":
                this.iban = attributeValue.getS();
                break;
            case "createdAt":
                this.createdAt = new Date(Long.parseLong(attributeValue.getN()));
                break;
        }
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Contact)) {
            return false;
        }

        Contact oc = (Contact) o;
        return oc.id == id && oc.name.equals(name) && oc.iban.equals(iban) && oc.createdAt.equals(createdAt);
    }

    @Override
    public int compareTo(Contact o) {
        return Integer.compare(id, o.id);
    }

    public String getName() {
        return name;
    }

    public String getIban() {
        return iban;
    }

    //TODO should be moved, might be useful elsewhere (Account...)
    public String getIbanSsmlOutput() {
        String ssml = "<emphasis level=\"reduced\"><say-as interpret-as=\"characters\">";
        String firstPart = iban.startsWith(" ") ? iban.substring(0, 3).substring(1) : iban.substring(0, 2);
        ssml = ssml + firstPart + "<break time=\"10ms\"/>";
        ssml = ssml + "</say-as>";
        ssml = ssml + "<say-as interpret-as=\"digits\">";
        String firstNumberChar = iban.startsWith(" ") ? iban.substring(3, 4) : iban.substring(2, 3);
        ssml = ssml + firstNumberChar + "<break time=\"10ms\"/>";
        String remainingNumbers = iban.startsWith(" ") ? iban.substring(4) : iban.substring(3);
        for (char ch : remainingNumbers.toCharArray()) {
            ssml = ssml.concat(ch + "<break time=\"10ms\"/>");
        }
        ssml = ssml + "</say-as></emphasis>";
        return ssml;
    }
}
