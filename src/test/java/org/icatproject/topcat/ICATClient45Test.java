package org.icatproject.topcat;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJB;

import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.icatclient.ICATClientFactory;
import org.icatproject.topcat.icatclient.ICATClientInterface;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Test;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ICATClient45Test {
    /*
    private static Properties prop;
    private static String icatSessionId;
    private static ICATClientInterface service;

    @EJB
    private ICATClientBean icatClientService;

    @BeforeClass
    public static void setup() throws MalformedURLException {
        prop = new Properties();
        InputStream is = ICATClient45Test.class.getClassLoader().getResourceAsStream("icatserver.properties");
        try {
            prop.load(is);
        } catch (Exception e) {
            System.out.println("Problem loading icatserver.properties: " + e.getClass() + " " + e.getMessage());
        }

        String icatUrl = prop.getProperty("icat.server.url");
        String version = prop.getProperty("icat.server.version");

        service = ICATClientFactory.getInstance().createICATClient(version, icatUrl);

        //get an icatsession for tests.
        icatSessionId = getIcatSession();

    }

    private static String getIcatSession() throws MalformedURLException {
        String authenticationType = prop.getProperty("icat.login.authentication.type");
        String username = prop.getProperty("icat.login.username");
        String password = prop.getProperty("icat.login.password");

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);

        String icatSessionId = null;

        try {
            icatSessionId = service.login(authenticationType, parameters);
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        } catch (InternalException e) {
            fail(e.getMessage());
        }

        //System.out.println("Test global icatsessionId: " + icatSessionId);

        return icatSessionId;
    }


    @Test
    public void login() throws MalformedURLException {
        String icatUrl = prop.getProperty("icat.server.url");
        String version = prop.getProperty("icat.server.version");
        String authenticationType = prop.getProperty("icat.login.authentication.type");
        String username = prop.getProperty("icat.login.username");
        String password = prop.getProperty("icat.login.password");

        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("username", username);
        parameters.put("password", password);

        String sessionId = null;

        ICATClientInterface service = ICATClientFactory.getInstance().createICATClient(version, icatUrl);

        try {
            sessionId = service.login(authenticationType, parameters);
        } catch (AuthenticationException | InternalException e) {
            fail(e.getMessage());
        }

        //System.out.println("login test icatSessionId: " + sessionId);

        assertTrue(sessionId.length() == 36);
    }

    @Test
    public void timeRemaining() {
        Long timeRemaining = null;

        try {
            timeRemaining = service.getRemainingMinutes(icatSessionId);
        } catch (TopcatException e) {
            fail(e.getMessage());
        }

        //System.out.println("Time remaining: " + timeRemaining);

        assertTrue(timeRemaining > 0);
    }

    */

    /*
    @Test
    public void refresh() throws InterruptedException {
        Long startTimeRemaining = null;
        Long endTimeRemaining = null;

        Thread.sleep(8000000);

        try {
            startTimeRemaining = service.getRemainingMinutes(icatSessionId);
        } catch (IcatException e) {
            fail(e.getMessage());
        }

        try {
            service.refresh(icatSessionId);
        } catch (IcatException e) {
            fail(e.getMessage());
        }

        try {
            endTimeRemaining = service.getRemainingMinutes(icatSessionId);
        } catch (IcatException e) {
            fail(e.getMessage());
        }

        assertTrue(endTimeRemaining > startTimeRemaining);
    }
    */

    /*

    @Test
    public void username() {
        String username = null;

        try {
            username = service.getUserName(icatSessionId);
        } catch (TopcatException e) {
            fail(e.getMessage());
        }

        assertEquals(prop.getProperty("icat.my.icatusername"), username);
    }



    @Test
    public void isSessionValid() {
        Boolean isValid = null;

        try {
            isValid = service.isSessionValid(icatSessionId);
        } catch (TopcatException e) {
            fail(e.getMessage());
        }

        assertTrue(isValid);
    }


    @Test
    public void z_logout() { //z_ to make sure test its runs last
        Boolean isValid = null;

        try {
            isValid = service.isSessionValid(icatSessionId);
        } catch (TopcatException e) {
            fail(e.getMessage());
        }

        if (isValid == false) {
            fail("session must be valid fo this test to start with");
        }

        try {
            service.logout(icatSessionId);
        } catch (TopcatException e) {
            fail(e.getMessage());
        }

        isValid = null;

        try {
            isValid = service.isSessionValid(icatSessionId);
        } catch (TopcatException e) {
            fail(e.getMessage());
        }

        assertFalse(isValid);
    }



    */

}
