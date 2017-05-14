package amosalexa;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Julian
 * Copyright (c) 14.05.2017 Bitspark GmbH
 */
public class SessionStorageTest {
    @Test
    public void getObject() throws Exception {
        SessionStorage sessionStorage = new SessionStorage();

        sessionStorage.putObject("sessid123", "key1", "val1");
        sessionStorage.putObject("sessid123", "key2", "val2");
        sessionStorage.putObject("sessid456", "key3", "val3");

        assertEquals(sessionStorage.getObject("sessid123", "key1"), "val1");
        assertEquals(sessionStorage.getObject("sessid123", "key2"), "val2");
        assertEquals(sessionStorage.getObject("sessid456", "key3"), "val3");
    }

}