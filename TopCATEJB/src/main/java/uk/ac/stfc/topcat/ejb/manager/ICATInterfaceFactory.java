/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.stfc.topcat.ejb.manager;

import java.net.MalformedURLException;
import uk.ac.stfc.topcat.core.icat.ICATWebInterfaceBase;
import uk.ac.stfc.topcat.icatclient.v331.ICATInterfacev331;
import uk.ac.stfc.topcat.icatclient.v340.ICATInterfacev340;
import uk.ac.stfc.topcat.icatclient.v341.ICATInterfacev341;
import uk.ac.stfc.topcat.icatclient.v400.ICATInterfacev400;
import uk.ac.stfc.topcat.icatclient.v410.ICATInterfacev410;
import uk.ac.stfc.topcat.icatclient.v420.ICATInterfacev420;
import uk.ac.stfc.topcat.icatclient.v43.ICATInterfacev43;

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
        } else if (version.compareToIgnoreCase("v341") == 0) {
            return new ICATInterfacev341(URL, serverName);
        } else if (version.compareToIgnoreCase("v400") == 0) {
            return new ICATInterfacev400(URL, serverName);
        } else if (version.compareToIgnoreCase("v410") == 0) {
            return new ICATInterfacev410(URL, serverName);
        } else if (version.compareToIgnoreCase("v420") == 0) {
            return new ICATInterfacev420(URL, serverName);
        } else if (version.compareToIgnoreCase("v42") == 0) {
            return new ICATInterfacev420(URL, serverName);
        } else if (version.compareToIgnoreCase("v43") == 0) {
            return new ICATInterfacev43(URL, serverName);
        }
        return null;
    }
}
