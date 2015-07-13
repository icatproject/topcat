package org.icatproject.topcat.icatclient;

import java.net.MalformedURLException;

public class ICATClientFactory {
    private static ICATClientFactory instance = new ICATClientFactory();

    private ICATClientFactory() {
    }

    public static ICATClientFactory getInstance() {
        return instance;
    }

    public ICATClientInterface createICATClient(String version, String URL) throws MalformedURLException {
        if (version.equals("4.5")) {
            return new ICATClient45(URL);
        }

        return null;
    }

}
