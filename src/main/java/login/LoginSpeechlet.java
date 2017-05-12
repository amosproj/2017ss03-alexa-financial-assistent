/**
 * Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 * <p>
 * http://aws.amazon.com/apache2.0/
 * <p>
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package login;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SimpleCard;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.auth.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic speechlet for login purpose
 */
public class LoginSpeechlet implements Speechlet {

    private static final Logger log = LoggerFactory.getLogger(LoginSpeechlet.class);

    private String speechTextWelcome = "Welcome!";

    private String repromptTextWelcome = "Welcome!";

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        if ("UsernameIntent".equals(intentName)) {
            log.info(getClass().toString() + " Intent started: " + intentName);
            return getFacebookUsername(session);
        }
        if ("FriendsCountIntent".equals(intentName)) {
            log.info(getClass().toString() + " Intent started: " + intentName);
            return getFacebookFriendsCount(session);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    private SpeechletResponse getFacebookUsername(Session session) {
        String accessToken = session.getUser().getAccessToken();
        log.error("Access Token: " + accessToken);
        if (accessToken != null) {
            log.info("HAVE ACCESS TOKEN : " + accessToken);
            Facebook facebook = new FacebookFactory().getInstance();
            /*
            TODO
            Set OAuthAppId here, if you want to test.
             */
            facebook.setOAuthAppId("", "");
            facebook.setOAuthPermissions("public_profile");
            facebook.setOAuthAccessToken(new AccessToken(accessToken));
            try {
                log.info(facebook.getName());
                return getSpeechletResponse(facebook.getName(), repromptTextWelcome);
            } catch (FacebookException e) {
                log.error(e.getMessage());
            }
        } else {
            log.info("No access token given!");
        }
        return getSpeechletResponse("Error", "Error");
    }

    private SpeechletResponse getFacebookFriendsCount(Session session) {
        String accessToken = session.getUser().getAccessToken();
        log.error("Access Token: " + accessToken);
        if (accessToken != null) {
            log.info("HAVE ACCESS TOKEN : " + accessToken);
            Facebook facebook = new FacebookFactory().getInstance();
            /*
            TODO
            Set OAuthAppId here, if you want to test.
             */
            facebook.setOAuthAppId("", "");
            facebook.setOAuthPermissions("user_friends");
            facebook.setOAuthAccessToken(new AccessToken(accessToken));
            try {
                log.info(String.valueOf(facebook.getFriends().getCount()));
                return getSpeechletResponse(String.valueOf(facebook.getFriends().size()), repromptTextWelcome);
            } catch (FacebookException e) {
                log.error(e.getMessage());
            }
        } else {
            log.info("No access token given!");
        }
        return getSpeechletResponse("Error", "Error");
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        return getSpeechletResponse(speechTextWelcome, repromptTextWelcome);
    }

    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Login");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        return SpeechletResponse.newTellResponse(speech, card);
    }
}
