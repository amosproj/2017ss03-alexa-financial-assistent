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

    private static int LAST_ID = 0;
    private static String DEFAULT_FILENAME = "test_transfer_templates.csv";

    public static Factory factory = (Factory<TransferTemplate>) TransferTemplate::new;

    private TransferTemplate() {
    }

    public TransferTemplate(String target, double amount) {
        this.id = ++LAST_ID;
        this.target = target;
        this.amount = amount;
        this.createdAt = new Date();
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

    @Deprecated
    private TransferTemplate(String csvLine) {
        String[] values = csvLine.split(";");

        this.id = Integer.parseInt(values[0]);
        this.target = values[1];
        this.amount = Double.parseDouble(values[2]);
        this.createdAt = new Date(Long.parseLong(values[3]));
    }

    @Deprecated
    private String toCSVLine() {
        return this.id + ";" + this.target + ";" + this.amount + ";" + createdAt.getTime();
    }

    @Deprecated
    public static Map<Integer, TransferTemplate> readTransferTemplateFromFile() throws IOException {
        return readTransferTemplateFromFile(DEFAULT_FILENAME);
    }

    @Deprecated
    public static Map<Integer, TransferTemplate> readTransferTemplateFromFile(String filename) throws IOException {
        Map<Integer, TransferTemplate> templateMap = new HashMap<>();

        Files.lines(FileSystems.getDefault().getPath(filename), Charset.forName("UTF-8")).forEach(l -> {
            TransferTemplate transferTemplate = new TransferTemplate(l);
            templateMap.put(transferTemplate.id, transferTemplate);
        });

        return templateMap;
    }

    @Deprecated
    public static void writeTransferTemplatesToFile(Map<Integer, TransferTemplate> templateMap) throws FileNotFoundException, UnsupportedEncodingException {
        writeTransferTemplatesToFile(DEFAULT_FILENAME, templateMap);
    }

    @Deprecated
    public static void writeTransferTemplatesToFile(String filename, Map<Integer, TransferTemplate> templateMap) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(filename, "UTF-8");

        for (Map.Entry<Integer, TransferTemplate> entry : templateMap.entrySet()) {
            writer.print(entry.getValue().toCSVLine() + "\n");
        }

        writer.close();
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
