package org.icatproject.topcat;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.icatproject.topcat.domain.Cart;
import org.icatproject.topcat.repository.CartRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;


@RunWith(Arquillian.class)
public class PersistencePreloadedDataTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addPackage(Cart.class.getPackage())
            .addClasses(CartRepository.class)
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @PersistenceContext
    EntityManager em;

    @Inject
    UserTransaction utx;

    @EJB
    CartRepository cartRepository;


    @Before
    public void preparePersistenceTest() throws Exception {

    }

    @Test
    @UsingDataSet("cart.json")
    public void getCart_dls_vcf21513() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", "dls");
        params.put("userName", "vcf21513");

        Cart cart = cartRepository.getCartByFacilityNameAndUser(params);

        assertNotNull(cart);
        assertEquals(cart.getId().longValue(), 1000L);
        assertEquals(cart.getCartItems().size(), 3);
    }

    @Test
    @UsingDataSet("cart.json")
    public void getCart_dls_wayne() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", "dls");
        params.put("userName", "wayne");

        Cart cart = cartRepository.getCartByFacilityNameAndUser(params);

        assertNotNull(cart);
        assertEquals(cart.getId().longValue(), 1001L);
        assertEquals(cart.getCartItems().size(), 2);
    }

    @Test
    @UsingDataSet("cart.json")
    public void getCart_isis_vcf21513() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", "isis");
        params.put("userName", "CLRC\\vcf21513");

        Cart cart = cartRepository.getCartByFacilityNameAndUser(params);

        assertNotNull(cart);
        assertEquals(cart.getId().longValue(), 1002L);
        assertEquals(cart.getCartItems().size(), 4);
    }

}
