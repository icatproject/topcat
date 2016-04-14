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


}
