package amosalexa.services.transfertemplates;

import amosalexa.AmosAlexaSpeechlet;
import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import api.DynamoDbClient;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.util.*;

public class TransferTemplateService implements SpeechService {

    public TransferTemplateService(SpeechletSubject speechletSubject) {
        subscribe(speechletSubject);
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        String intentName = request.getIntent().getName();

        if ("AMAZON.YesIntent".equals(intentName)) {
            Integer offset = (Integer) session.getAttribute("TransferTemplateService.offset");
            Integer templateId = (Integer) session.getAttribute("TransferTemplateService.delete");
            Integer editTemplateId = (Integer) session.getAttribute("TransferTemplateService.editTemplateId");

            if (offset != null) {
                return tellTemplates(session, offset, 3);
            }

            if (templateId != null) {
                TransferTemplate transferTemplate = new TransferTemplate(templateId);
                DynamoDbClient.instance.deleteItem(TransferTemplate.TABLE_NAME, transferTemplate);

                return AmosAlexaSpeechlet.getSpeechletResponse("Vorlage wurde gelöscht.", "", false);
            }

            if (editTemplateId != null) {
                Double newAmount = Double.parseDouble(session.getAttribute("TransferTemplateService.newAmount").toString());

                TransferTemplate transferTemplate = (TransferTemplate) DynamoDbClient.instance.getItem(TransferTemplate.TABLE_NAME, editTemplateId, TransferTemplate::new);
                transferTemplate.setAmount(newAmount);
                DynamoDbClient.instance.putItem(TransferTemplate.TABLE_NAME, transferTemplate);

                return AmosAlexaSpeechlet.getSpeechletResponse("Vorlage wurde erfolgreich gespeichert.", "", false);
            }
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            if (session.getAttribute("TransferTemplateService.offset") != null ||
                    session.getAttribute("TransferTemplateService.delete") != null ||
                    session.getAttribute("TransferTemplateService.editTemplateId") != null ||
                    session.getAttribute("TransferTemplateService.newAmount") != null) {
                return AmosAlexaSpeechlet.getSpeechletResponse("Okay, dann halt nicht. Tschüss!", "", false);
            }
        } else if ("ListTransferTemplatesIntent".equals(intentName)) {
            return tellTemplates(session, 0, 3);
        } else if ("DeleteTransferTemplatesIntent".equals(intentName)) {
            String templateIdStr = request.getIntent().getSlot("TemplateID").getValue();

            if (templateIdStr == null || templateIdStr.equals("")) {
                return null;
            } else {
                int templateId = Integer.parseInt(templateIdStr);
                session.setAttribute("TransferTemplateService.delete", templateId);

                return AmosAlexaSpeechlet.getSpeechletResponse("Möchtest du Vorlage Nummer " + templateId + " wirklich löschen?", "", true);
            }
        } else if ("EditTransferTemplateIntent".equals(intentName)) {
            String templateIdStr = request.getIntent().getSlot("TemplateID").getValue();
            String newAmountStr = request.getIntent().getSlot("NewAmount").getValue();

            if (templateIdStr == null || templateIdStr.equals("") || newAmountStr == null || newAmountStr.equals("")) {
                return null;
            } else {
                int templateId = Integer.parseInt(templateIdStr);

                TransferTemplate template = (TransferTemplate) DynamoDbClient.instance.getItem(TransferTemplate.TABLE_NAME, templateId, TransferTemplate::new);

                if (template == null) {
                    return AmosAlexaSpeechlet.getSpeechletResponse("Ich kann Vorlage " + templateId + " nicht finden.", "", false);
                }

                double newAmount = 0;
                try {
                    newAmount = Double.parseDouble(newAmountStr);
                } catch (NumberFormatException ignored) {
                    // TODO: Maybe do some error handling here
                }

                session.setAttribute("TransferTemplateService.editTemplateId", templateId);
                session.setAttribute("TransferTemplateService.newAmount", newAmount);

                return AmosAlexaSpeechlet.getSpeechletResponse("Möchtest du den Betrag von Vorlage " + templateId + " von " + template.getAmount() + " auf " + newAmount + " ändern?", "", true);
            }
        }

        return null;
    }

    SpeechletResponse tellTemplates(Session session, int offset, int limit) {
        List<TransferTemplate> templates = DynamoDbClient.instance.getItems("transfer_template", TransferTemplate::new);
        List<TransferTemplate> transferTemplates = new ArrayList<TransferTemplate>(templates);


        if (offset >= transferTemplates.size()) {
            session.setAttribute("TransferTemplateService.offset", null);
            return AmosAlexaSpeechlet.getSpeechletResponse("Keine weiteren Vorlagen.", "", false);
        }

        if (offset + limit >= transferTemplates.size()) {
            limit = transferTemplates.size() - offset;
        }

        Collections.sort(transferTemplates);

        StringBuilder response = new StringBuilder();

        for (int i = offset; i < offset + limit; i++) {
            TransferTemplate template = transferTemplates.get(i);

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(template.getCreatedAt());
            String dateFormatted = String.format("01.%02d.%d", calendar.get(Calendar.MONTH) + 2, calendar.get(Calendar.YEAR));

            response.append("Vorlage " + template.getId() + " vom " + dateFormatted + ": ");
            response.append("Überweise " + template.getAmount() + " Euro an " + template.getTarget() + ". ");
        }

        boolean isAskResponse = transferTemplates.size() > offset + limit;

        if (isAskResponse) {
            response.append("Weitere Vorlagen vorlesen?");
            session.setAttribute("TransferTemplateService.offset", offset + limit);
        } else {
            response.append("Keine weiteren Vorlagen.");
            session.setAttribute("TransferTemplateService.offset", null);
        }

        return AmosAlexaSpeechlet.getSpeechletResponse(response.toString(), "", isAskResponse);
    }

    @Override
    public void subscribe(SpeechletSubject speechletSubject) {
        speechletSubject.attachSpeechletObserver(this, "AMAZON.YesIntent");
        speechletSubject.attachSpeechletObserver(this, "AMAZON.NoIntent");
        speechletSubject.attachSpeechletObserver(this, "ListTransferTemplatesIntent");
        speechletSubject.attachSpeechletObserver(this, "DeleteTransferTemplatesIntent");
        speechletSubject.attachSpeechletObserver(this, "EditTransferTemplateIntent");
        speechletSubject.attachSpeechletObserver(this, "DeleteTransferTemplateIntent");
    }
}
