package org.icatproject.topcat.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.NoResultException;

import org.icatproject.topcat.domain.Cart;
import org.icatproject.topcat.domain.CartItem;
import org.icatproject.topcat.domain.ParentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Stateless
@LocalBean
@Singleton
public class CartRepository {
    private static final Logger logger = LoggerFactory.getLogger(CartRepository.class);

    @PersistenceContext(unitName="topcat")
    EntityManager em;

    public Cart getCart(String userName, String facilityName){
        TypedQuery<Cart> query = em.createQuery("select cart from Cart cart where cart.userName = :userName and cart.facilityName = :facilityName", Cart.class)
            .setParameter("userName", userName)
            .setParameter("facilityName", facilityName);
        try {
            return query.getSingleResult();
        } catch(NoResultException e){
            return null;
        }
    }

    public Cart getCartByFacilityNameAndUser(Map<String, String> params) {
        List<Cart> carts = new ArrayList<Cart>();

        String facilityName = params.get("facilityName");
        String userName = params.get("userName");

        if (em != null) {
            TypedQuery<Cart> query = em.createNamedQuery("Cart.findByFacilityNameAndUserName", Cart.class)
                    .setParameter("facilityName", facilityName)
                    .setParameter("userName", userName);

            carts = query.getResultList();
        }

        if (carts.isEmpty()) {
            return null;
        }

        return carts.get(0);
    }

    public int removeCartByFacilityNameAndUser(Map<String, String> params) {
        List<Cart> carts = new ArrayList<Cart>();

        String facilityName = params.get("facilityName");
        String userName = params.get("userName");

        int deletedCount = 0;

        if (em != null) {
            TypedQuery<Cart> query = em.createNamedQuery("Cart.findByFacilityNameAndUserName", Cart.class)
                    .setParameter("facilityName", facilityName)
                    .setParameter("userName", userName);

            carts = query.getResultList();
        }

        //only one should exist
        for (Cart cart : carts) {
            em.remove(cart);
            logger.info("Cart id " + cart.getId() + " belonging to " + userName + " for facility " + facilityName + " was cleared");
            deletedCount++;
        }

        em.flush();

        return deletedCount;

    }



    public Cart save(Cart cart) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", cart.getFacilityName());
        params.put("userName", cart.getUserName());

        /*Cart existCart = this.getCartByFacilityNameAndUser(params);
        if (existCart != null) {
            em.remove(existCart);
            em.flush();
        }

        logger.debug("cart save new called");
        em.persist(cart);
        em.flush();

        return cart;*/

        Cart existCart = this.getCartByFacilityNameAndUser(params);

        if (existCart != null) {
            existCart.setUpdatedAt(new Date());

            //persist the items in cart
            for (CartItem cartItem : cart.getCartItems()) {
                //persist parentEntity
                List<ParentEntity> parentEntities = cartItem.getParentEntities();


                for (ParentEntity parentEntity: parentEntities) {
                    parentEntity.setCartItem(cartItem);
                    em.persist(parentEntity);
                }

                cartItem.setParentEntities(parentEntities);

                cartItem.setCart(existCart);
                em.persist(cartItem);
            }

            //delete items from existing cart
            for(CartItem cartItem : existCart.getCartItems()) {
                em.remove(cartItem);
            }

            //set cart
            existCart.setCartItems(cart.getCartItems());

            em.flush();

            return existCart;
        } else {
            em.persist(cart);
            em.flush();

            return cart;
        }
    }

}
