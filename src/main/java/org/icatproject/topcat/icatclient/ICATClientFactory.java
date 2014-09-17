package org.icatproject.topcat.icatclient;

import java.net.MalformedURLException;

public class ICATClientFactory {
    private static ICATClientFactory instance = new ICATClientFactory();

    private ICATClientFactory() {
    }

    public static ICATClientFactory getInstance() {
        return instance;
    }

    public ICATClientInterface createICATClient(String serverName, String version, String URL) throws MalformedURLException {
        if (version.equals("v43")) {
            return new ICATClient43(URL, serverName);
        }

        return null;
    }

}
