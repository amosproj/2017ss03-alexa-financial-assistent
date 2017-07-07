package amosalexa.services;


import amosalexa.SessionStorage;
import com.amazon.speech.speechlet.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DialogUtil {

    private static final Logger log = LoggerFactory.getLogger(DialogUtil.class);

    /**
     * sets a dialog state
     * @param name of the state
     */
    public static void setDialogState(String name, Session session){
        log.info("DialogState: " + name);
        SessionStorage.getInstance().putObject(session.getSessionId(), name, new Object());
    }

    /**
     * get dialog state
     * @param name of the state
     * @return Object
     */
    public static  Object getDialogState(String name, Session session){
        //get state
        Object object = SessionStorage.getInstance().getObject(session.getSessionId(), name);

        //revoke old state
        SessionStorage.getInstance().putObject(session.getSessionId(), name, null);

        return object;
    }
}
