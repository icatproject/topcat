package org.icatproject.topcat.utils;

import org.icatproject.ids.client.DataSelection;
import org.icatproject.ids.client.IdsClient.Flag;
import org.icatproject.topcat.domain.Cart;
import org.icatproject.topcat.domain.CartItem;
import org.icatproject.topcat.domain.EntityType;

public class CartUtils {
    public static DataSelection cartToDataSelection(Cart cart) {
        DataSelection dataSelection = new DataSelection();

        for (CartItem cartItem : cart.getCartItems()) {
            if (cartItem.getEntityType() == EntityType.investigation) {
                dataSelection.addInvestigation(cartItem.getEntityId());
            }

            if (cartItem.getEntityType() == EntityType.dataset) {
                dataSelection.addDataset(cartItem.getEntityId());
            }

            if (cartItem.getEntityType() == EntityType.datafile) {
                dataSelection.addDatafile(cartItem.getEntityId());
            }
        }

        return dataSelection;
    }


    public static Flag getZipFlag(String zip) {
        //Support only ZIP and ZIP_AND_COMPRESS for now.
        //In order to support NONE, we have to check only a datafile is  in data selection and remove the outname.
        //However, the outname is passed by the frontend
        if (zip == null ) {
            return Flag.ZIP;
        }

        zip = zip.toUpperCase();

        if(zip.equals("ZIP_AND_COMPRESS")) {
            return Flag.ZIP_AND_COMPRESS;
        } else {
            return Flag.ZIP;
        }
    }
}
