package amosalexa.services.transfertemplates;

import amosalexa.AmosAlexaSpeechlet;
import amosalexa.SpeechletSubject;
import amosalexa.services.SpeechService;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;

import java.io.IOException;
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
            Integer offset = (Integer)session.getAttribute("TransferTemplateService.offset");

            if (offset != null) {
                return tellTemplates(session, offset, 3);
            }
        } else if ("AMAZON.NoIntent".equals(intentName)) {
            String offsetStr = (String)session.getAttribute("TransferTemplateService.offset");

            if (offsetStr != null) {
                session.setAttribute("TransferTemplateService.offset", null);
                return AmosAlexaSpeechlet.getSpeechletResponse("Okay, tschüss!", "", false);
            }
        } else if ("ListTransferTemplatesIntent".equals(intentName)) {
            return tellTemplates(session, 0, 3);
        } else if ("DeleteTransferTemplatesIntent".equals(intentName)) {

        } else if ("EditTransferTemplateIntent".equals(intentName)) {

        }

        return null;
    }

    SpeechletResponse tellTemplates(Session session, int offset, int limit) {
        Map<Integer, TransferTemplate> templateMap = null;

        try {
            templateMap = TransferTemplate.readTransferTemplateFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        List<TransferTemplate> transferTemplates = new ArrayList<TransferTemplate>(templateMap.values());

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

        boolean isAskResponse = transferTemplates.size() >= offset + limit;

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
