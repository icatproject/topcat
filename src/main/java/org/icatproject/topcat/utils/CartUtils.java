package org.icatproject.topcat.utils;

import org.icatproject.ids.client.DataSelection;
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

}
