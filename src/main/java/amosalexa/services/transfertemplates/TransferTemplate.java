package amosalexa.services.transfertemplates;

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

public class TransferTemplate {
    private int id;
    private String target;
    private double amount;
    private Date createdAt;

    private static int LAST_ID = 0;
    private static String DEFAULT_FILENAME = "transfer_templates.csv";

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

    private TransferTemplate(String csvLine) {
        String[] values = csvLine.split(";");

        this.id = Integer.parseInt(values[0]);
        this.target = values[1];
        this.amount = Double.parseDouble(values[2]);
        this.createdAt = new Date(Long.parseLong(values[3]));
    }

    private String toCSVLine() {
        return this.id + ";" + this.target + ";" + this.amount + ";" + createdAt.getTime();
    }

    public static Map<Integer, TransferTemplate> readTransferTemplate() throws IOException {
        return readTransferTemplate(DEFAULT_FILENAME);
    }

    public static Map<Integer, TransferTemplate> readTransferTemplate(String filename) throws IOException {
        Map<Integer, TransferTemplate> templateMap = new HashMap<>();

        Files.lines(FileSystems.getDefault().getPath(filename), Charset.forName("UTF-8")).forEach(l -> {
            TransferTemplate transferTemplate = new TransferTemplate(l);
            templateMap.put(transferTemplate.id, transferTemplate);
        });

        return templateMap;
    }

    public static void writeTransferTemplates(Map<Integer, TransferTemplate> templateMap) throws FileNotFoundException, UnsupportedEncodingException {
        writeTransferTemplates(DEFAULT_FILENAME, templateMap);
    }

    public static void writeTransferTemplates(String filename, Map<Integer, TransferTemplate> templateMap) throws FileNotFoundException, UnsupportedEncodingException {
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
        return id == tt.id && amount == tt.amount && target.equals(tt.target) && createdAt.equals(tt.createdAt);
    }
}
