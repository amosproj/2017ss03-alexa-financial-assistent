package amosalexa.services.transfertemplates;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Julian
 * Copyright (c) 03.06.2017 Bitspark GmbH
 */
public class TransferTemplateTest {

    @Test
    public void readAndWriteTransferTemplate() throws Exception {
        final String TEST_FILENAME = "test_transfer_templates.csv";

        Map<Integer, TransferTemplate> templateMap1 = new HashMap<>();

        TransferTemplate t1 = new TransferTemplate("Gabriel", 10);
        TransferTemplate t2 = new TransferTemplate("Lukas", 20);
        TransferTemplate t3 = new TransferTemplate("Paul", 30);
        TransferTemplate t4 = new TransferTemplate("Robert", 15);
        TransferTemplate t5 = new TransferTemplate("Vladimir", 25);

        templateMap1.put(t1.getId(), t1);
        templateMap1.put(t2.getId(), t2);
        templateMap1.put(t3.getId(), t3);
        templateMap1.put(t4.getId(), t4);
        templateMap1.put(t5.getId(), t5);

        TransferTemplate.writeTransferTemplates(TEST_FILENAME, templateMap1);

        Map<Integer, TransferTemplate> templateMap2 = TransferTemplate.readTransferTemplate(TEST_FILENAME);

        assertEquals(templateMap1.size(), templateMap2.size());

        for (Map.Entry<Integer, TransferTemplate> entry : templateMap2.entrySet()) {
            TransferTemplate transferTemplate1 = templateMap1.get(entry.getKey());
            assertEquals(transferTemplate1, entry.getValue());
        }
    }

}