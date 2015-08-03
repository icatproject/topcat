package org.icatproject.topcat.idsclient;

import java.net.MalformedURLException;

public class IdsClientFactory {
    private static IdsClientFactory instance = new IdsClientFactory();

    private IdsClientFactory() {
    }

    public static IdsClientFactory getInstance() {
        return instance;
    }

    public IdsClientInterface createIdsClient(String URL) throws MalformedURLException {
        return new IdsClient13(URL);
    }

}
