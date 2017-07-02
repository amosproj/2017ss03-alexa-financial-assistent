package amosalexa;

import amosalexa.server.Launcher;
import org.eclipse.jetty.server.Server;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use extended test for tests that connect to an external API and thus need internet connection and/or
 * have limited API requests and should not be executed with every local or travis build.
 * (Call extended tests explicitly by executing 'gradle extendedTests')
 */
@Category(AmosAlexaExtendedTest.class)
public class AmosAlexaExtendedTestImpl extends AbstractAmosAlexaSpeechletTest implements AmosAlexaExtendedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmosAlexaExtendedTestImpl.class);

    @Test
    public void bankContactTelephoneNumberTest() throws Exception {
        newSession();

        // pretend local environment
        Launcher.server = new Server();
        Launcher.server.start();

        testIntentMatches("BankTelephone", "Sparkasse Nürnberg - Geldautomat hat die Telefonnummer 0911 2301000");

        Launcher.server.stop();
    }

    @Test
    public void bankContactOpeningHoursTest() throws Exception {

        // pretend local environment
        Launcher.server = new Server();
        Launcher.server.start();

        newSession();
        testIntentMatches("BankOpeningHours", "Sparkasse Nürnberg - Geschäftsstelle hat am (.*)");
        testIntentMatches("BankOpeningHours", "OpeningHoursDate:2017-06-13", "Sparkasse Nürnberg - Geschäftsstelle Geöffnet am Dienstag von (.*) bis (.*)");

        Launcher.server.stop();
    }

    @Test
    public void bankContactAddressTest() throws Exception {

        // pretend local environment
        Launcher.server = new Server();
        Launcher.server.start();

        newSession();

        testIntentMatches("BankAddress", "Sparkasse Nürnberg - Geschäftsstelle hat die Adresse: Lorenzer Pl. 12, 90402 Nürnberg, Germany");
        testIntentMatches("BankAddress", "BankNameSlots:Deutsche Bank", "Deutsche Bank Filiale hat die Adresse: Landgrabenstraße 144, 90459 Nürnberg, Germany");

        Launcher.server.stop();
    }

    //TODO add SecuritiesAccount test that needs API support as well
}
