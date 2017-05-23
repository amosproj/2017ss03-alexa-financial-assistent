package amosalexa.services.pricequery.aws.request;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import amosalexa.services.pricequery.aws.util.XMLParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class AWSRequest {

    final static Logger log = Logger.getLogger(AWSRequest.class);

    private static final String AWS_SECRET_KEY = "vVafGHDBSi5ma78QwwcEmLWVMsDUEmd13XaNcjDh";
    private static final String AWS_KEY = "AKIAJD4O7BRHWN5W3JIA";
    private static final String AWS_ENDPOINT = "webservices.amazon.de";

    private Map<String, String> addParams;

    public AWSRequest(Map<String, String> addParams) {
        this.addParams = addParams;
    }

    private String createSignedURL() {

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Version", "2009-03-31");
        params.put("AssociateTag","preisspion-21");

        params.putAll(addParams);

        SignedRequestsHelper helper = null;
        try {
            helper = SignedRequestsHelper.getInstance(AWS_ENDPOINT, AWS_KEY, AWS_SECRET_KEY);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        assert helper != null;
        return helper.sign(params);
    }

    /**
     * @return String xmlString
     */
    public String signedRequest() {

        Document response = null;
        try {
            response = getResponse(createSignedURL());
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        String xmlString = XMLParser.xmlToString(response);

        return XMLParser.getPrettyXML(xmlString);
    }


    private Document getResponse(String url) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(url);
        return doc;
    }

}
