package amosalexa;

import java.util.HashMap;
import java.util.Map;

/**
 * This class helps to store arbitrary objects and link them to a session.
 * TODO: Implement removal of sessions and objects
 */
public class SessionStorage {

    /**
     * This class contains session related values.
     */
    class Storage {
        private Map<String,Object> storage;

        private Storage() {
            storage = new HashMap<>();
        }

        public void put(String key, Object value) {
            storage.put(key, value);
        }

        public Object get(String key) {
            if (!storage.containsKey(key)) {
                return null;
            }
            return storage.get(key);
        }
    }

    private Map<String,Storage> sessionStorage;

    public SessionStorage() {
        sessionStorage = new HashMap<>();
    }

    /**
     * Returns a Storage object or creates it if it doesn't exist yet.
     * @param sessionId
     * @return Storage object
     */
    public Storage getStorage(String sessionId) {
        if (sessionStorage.containsKey(sessionId)) {
            return sessionStorage.get(sessionId);
        }

        Storage storage = new Storage();
        sessionStorage.put(sessionId, storage);
        return storage;
    }

    /**
     * Puts an arbitrary object into the session storage and links it to a session.
     * @param sessionId
     * @param key
     * @param value
     */
    public void putObject(String sessionId, String key, Object value) {
        Storage storage = getStorage(sessionId);
        storage.put(key, value);
    }

    /**
     * Retrieves an arbitrary objects that has been stored before.
     * @param sessionId
     * @param key
     * @return
     */
    public Object getObject(String sessionId, String key) {
        Storage storage = getStorage(sessionId);
        return storage.get(key);
    }

}
