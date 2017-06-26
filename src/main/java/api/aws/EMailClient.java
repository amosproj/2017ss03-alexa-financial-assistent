package api.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EMailClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(EMailClient.class);

	private static final String FROM = "alexa.finance@web.de";
	private static final String TO = "alexa.finance@web.de";

	/**
	 * Sends an example e mail.
	 *
	 * @param subject the subject
	 * @param body    the body
	 * @return true on success
	 */
	public static boolean SendEMail(String subject, String body) {
		// Construct an object to contain the recipient address.
		Destination destination = new Destination().withToAddresses(TO);

		// Create the subject and body of the message.
		Content cSubject = new Content().withData(subject);
		Content cTextBody = new Content().withData(body);
		Body bBody = new Body().withText(cTextBody);

		// Create a message with the specified subject and body.
		Message message = new Message().withSubject(cSubject).withBody(bBody);

		// Assemble the email.
		SendEmailRequest request = new SendEmailRequest().withSource(FROM)
				.withDestination(destination).withMessage(message);

		try {
			/*AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
					.withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {
						@Override
						public String getAWSAccessKeyId() {
							return "...";
						}

						@Override
						public String getAWSSecretKey() {
							return "...";
						}
					}))
					.withRegion(Regions.EU_WEST_1).build();*/

			AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
					.withCredentials(new CustomEnvironmentVariableCredentialsProvider("AWS_SES_ACCESS_KEY", "AWS_SES_SECRET_KEY"))
					.withRegion(Regions.EU_WEST_1).build();

			// Send the email.
			client.sendEmail(request);
			return true;
		} catch (Exception ex) {
			LOGGER.error("EMailClient SendEMail error: " + ex.getMessage());
			return false;
		}
	}
}
