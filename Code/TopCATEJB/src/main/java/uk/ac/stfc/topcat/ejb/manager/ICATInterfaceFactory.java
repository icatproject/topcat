/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.ejb.manager;

import java.net.MalformedURLException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.icatclient.v331.ICATInterfacev331;
import uk.ac.stfc.topcat.icatclient.v340.ICATInterfacev340;
import uk.ac.stfc.topcat.icatclient.v400.ICATInterfacev400;

/**
 * 
 * @author sn65
 */
public class ICATInterfaceFactory {
    private static ICATInterfaceFactory instance = new ICATInterfaceFactory();

    private ICATInterfaceFactory() {
    }

    public static ICATInterfaceFactory getInstance() {
        return instance;
    }

    public ICATWebInterfaceBase createICATInterface(String serverName, String version, String URL)
            throws MalformedURLException {

        if (version.compareToIgnoreCase("v331") == 0) {
            return new ICATInterfacev331(URL, serverName);
        } else if (version.compareToIgnoreCase("v340") == 0) {
            return new ICATInterfacev340(URL, serverName);
        } else if (version.compareToIgnoreCase("v400") == 0) {
            return new ICATInterfacev400(URL, serverName);
        }
        return null;
    }
}
