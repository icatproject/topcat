package org.icatproject.topcat;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;

import org.icatproject.topcat.domain.Cart;
import org.icatproject.topcat.domain.CartItem;
import org.icatproject.topcat.domain.EntityType;
import org.icatproject.topcat.domain.ParentEntity;
import org.icatproject.topcat.repository.CartRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;


@RunWith(Arquillian.class)
public class PersistenceTest {
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
        clearData();
    }

    public void clearData() throws Exception {
        utx.begin();
        em.joinTransaction();

        TypedQuery<Cart> query = em.createQuery("SELECT c from Cart c", Cart.class);
        List<Cart> carts = query.getResultList();

        for(Cart cart : carts) {
            em.remove(cart);
        }

        em.flush();

        utx.commit();
    }

    @Test
    public void ebjLoaded() {
        assertNotNull(cartRepository);
    }


    @Test
    public void addNewCartWithItems() {
        List<CartItem> cartItems = new ArrayList<CartItem>();

        Cart cart = new Cart();
        cart.setFacilityName("dls");
        cart.setUserName("vcf21513");

        CartItem cartItem1 = new CartItem();
        cartItem1.setEntityId(new Long(910034180));
        cartItem1.setName("CM12170");
        cartItem1.setEntityType(EntityType.investigation);

        CartItem cartItem2 = new CartItem();
        cartItem2.setEntityId(new Long(910034179));
        cartItem2.setName("CM12170");
        cartItem2.setEntityType(EntityType.investigation);

        CartItem cartItem3 = new CartItem();
        cartItem3.setEntityId(new Long(910034178));
        cartItem3.setName("CM12170");
        cartItem3.setEntityType(EntityType.investigation);

        cartItem1.setCart(cart);
        cartItem2.setCart(cart);
        cartItem3.setCart(cart);

        cartItems.add(cartItem1);
        cartItems.add(cartItem2);
        cartItems.add(cartItem3);

        cart.setCartItems(cartItems);

        cart = cartRepository.save(cart);

        assertNotNull(cart.getId());

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", cart.getFacilityName());
        params.put("userName", cart.getUserName());
        Cart existCart = cartRepository.getCartByFacilityNameAndUser(params);

        assertEquals(cart.getId(), existCart.getId());

    }


    @Test
    public void addNewCartWithItemsAndParents() {
        List<CartItem> cartItems = new ArrayList<CartItem>();

        Cart cart = new Cart();
        cart.setFacilityName("dls");
        cart.setUserName("vcf21513");

        CartItem cartItem1 = new CartItem();
        cartItem1.setEntityId(new Long(910034180));
        cartItem1.setName("CM12170");
        cartItem1.setEntityType(EntityType.datafile);
        cartItem1.setCart(cart);

        List<ParentEntity> parentEntities = new ArrayList<ParentEntity>();

        ParentEntity parentEntity1 = new ParentEntity();
        parentEntity1.setEntityType(EntityType.investigation);
        parentEntity1.setEntityId(5000001L);
        parentEntity1.setCartItem(cartItem1);

        ParentEntity parentEntity2 = new ParentEntity();
        parentEntity2.setEntityType(EntityType.dataset);
        parentEntity2.setEntityId(6000001L);
        parentEntity2.setCartItem(cartItem1);

        parentEntities.add(parentEntity1);
        parentEntities.add(parentEntity2);

        cartItem1.setParentEntities(parentEntities);

        cartItems.add(cartItem1);

        cart.setCartItems(cartItems);

        cart = cartRepository.save(cart);

        assertNotNull(cart.getId());

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", "dls");
        params.put("userName", "vcf21513");
        Cart myCart = cartRepository.getCartByFacilityNameAndUser(params);

        assertNotNull(myCart);
        assertEquals(1, myCart.getCartItems().size());
        assertEquals(2, myCart.getCartItems().get(0).getParentEntities().size());
        assertEquals(EntityType.investigation, myCart.getCartItems().get(0).getParentEntities().get(0).getEntityType());
        assertEquals(new Long(5000001), myCart.getCartItems().get(0).getParentEntities().get(0).getEntityId());
        assertEquals(EntityType.dataset, myCart.getCartItems().get(0).getParentEntities().get(1).getEntityType());
        assertEquals(new Long(6000001), myCart.getCartItems().get(0).getParentEntities().get(1).getEntityId());
    }


    @Test
    public void modifyCartWithItems() {
        List<CartItem> cartItems = new ArrayList<CartItem>();

        Cart cart = new Cart();
        cart.setFacilityName("dls");
        cart.setUserName("vcf21513");

        CartItem cartItem1 = new CartItem();
        cartItem1.setEntityId(new Long(910034180));
        cartItem1.setName("CM12170");
        cartItem1.setEntityType(EntityType.investigation);

        CartItem cartItem2 = new CartItem();
        cartItem2.setEntityId(new Long(910034179));
        cartItem2.setName("CM12170");
        cartItem2.setEntityType(EntityType.investigation);

        CartItem cartItem3 = new CartItem();
        cartItem3.setEntityId(new Long(910034178));
        cartItem3.setName("CM12170");
        cartItem3.setEntityType(EntityType.investigation);

        cartItem1.setCart(cart);
        cartItem2.setCart(cart);
        cartItem3.setCart(cart);

        cartItems.add(cartItem1);
        cartItems.add(cartItem2);
        cartItems.add(cartItem3);

        cart.setCartItems(cartItems);

        cart = cartRepository.save(cart);

        assertNotNull(cart.getId());

        Long cartId = cart.getId();

        System.out.println("new cartId:" + cartId);
        System.out.println("new cartId item count:" + cart.getCartItems().size());

        List<CartItem> cart1Items = new ArrayList<CartItem>();

        Cart cart1 = new Cart();
        cart1.setFacilityName("dls");
        cart1.setUserName("vcf21513");

        CartItem cart1Item1 = new CartItem();
        cart1Item1.setEntityId(new Long(922222222));
        cart1Item1.setName("CM12170");
        cart1Item1.setEntityType(EntityType.investigation);

        CartItem cart1Item2 = new CartItem();
        cart1Item2.setEntityId(new Long(922222223));
        cart1Item2.setName("CM12170");
        cart1Item2.setEntityType(EntityType.investigation);

        CartItem cart1Item3 = new CartItem();
        cart1Item3.setEntityId(new Long(922222224));
        cart1Item3.setName("CM12170");
        cart1Item3.setEntityType(EntityType.investigation);

        CartItem cart1Item4 = new CartItem();
        cart1Item4.setEntityId(new Long(922222225));
        cart1Item4.setName("CM12170");
        cart1Item4.setEntityType(EntityType.investigation);

        cart1Item1.setCart(cart1);
        cart1Item2.setCart(cart1);
        cart1Item3.setCart(cart1);
        cart1Item4.setCart(cart1);

        cart1Items.add(cart1Item1);
        cart1Items.add(cart1Item2);
        cart1Items.add(cart1Item3);
        cart1Items.add(cart1Item4);

        cart1.setCartItems(cart1Items);

        cartRepository.save(cart1);


        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", cart.getFacilityName());
        params.put("userName", cart.getUserName());
        Cart existCart = cartRepository.getCartByFacilityNameAndUser(params);

        System.out.println("existCartId:" + existCart.getId());
        System.out.println("existCartId item count:" + existCart.getCartItems().size());

        assertEquals(existCart.getCartItems().size(), 4);

        Cart myCart = cartRepository.getCartByFacilityNameAndUser(params);
        assertEquals(myCart.getId(), cartId);

    }


    @Test
    public void modifyCartWithItemsAndParents() {
        List<CartItem> cartItems = new ArrayList<CartItem>();

        Cart cart = new Cart();
        cart.setFacilityName("dls");
        cart.setUserName("vcf21513");

        CartItem cartItem = new CartItem();
        cartItem.setEntityId(new Long(910034180));
        cartItem.setName("CM12170");
        cartItem.setEntityType(EntityType.datafile);
        cartItem.setCart(cart);

        List<ParentEntity> parentEntities = new ArrayList<ParentEntity>();

        ParentEntity parentEntity1 = new ParentEntity();
        parentEntity1.setEntityType(EntityType.investigation);
        parentEntity1.setEntityId(5000001L);
        parentEntity1.setCartItem(cartItem);

        ParentEntity parentEntity2 = new ParentEntity();
        parentEntity2.setEntityType(EntityType.dataset);
        parentEntity2.setEntityId(6000001L);
        parentEntity2.setCartItem(cartItem);

        parentEntities.add(parentEntity1);
        parentEntities.add(parentEntity2);

        cartItem.setParentEntities(parentEntities);

        cartItems.add(cartItem);

        cart.setCartItems(cartItems);

        cart = cartRepository.save(cart);

        assertNotNull(cart.getId());
        Long cartId = cart.getId();

        assertEquals(1, cart.getCartItems().size());
        assertEquals(2, cart.getCartItems().get(0).getParentEntities().size());


        System.out.println("new cartId:" + cartId);
        System.out.println("new cartId item count:" + cart.getCartItems().size());

        List<CartItem> cart1Items = new ArrayList<CartItem>();

        Cart cart1 = new Cart();
        cart1.setFacilityName("dls");
        cart1.setUserName("vcf21513");

        CartItem cart1Item1 = new CartItem();
        cart1Item1.setEntityId(new Long(922222222));
        cart1Item1.setName("CM12170");
        cart1Item1.setEntityType(EntityType.datafile);

        CartItem cart1Item2 = new CartItem();
        cart1Item2.setEntityId(new Long(822222223));
        cart1Item2.setName("CM12170");
        cart1Item2.setEntityType(EntityType.dataset);

        List<ParentEntity> parentEntities1 = new ArrayList<ParentEntity>();
        List<ParentEntity> parentEntities2 = new ArrayList<ParentEntity>();

        ParentEntity parentEntity3 = new ParentEntity();
        parentEntity3.setEntityType(EntityType.investigation);
        parentEntity3.setEntityId(2000001L);
        parentEntity3.setCartItem(cart1Item1);

        ParentEntity parentEntity4 = new ParentEntity();
        parentEntity4.setEntityType(EntityType.dataset);
        parentEntity4.setEntityId(3000001L);
        parentEntity4.setCartItem(cart1Item1);

        ParentEntity parentEntity5 = new ParentEntity();
        parentEntity5.setEntityType(EntityType.investigation);
        parentEntity5.setEntityId(4000001L);
        parentEntity5.setCartItem(cart1Item2);

        parentEntities1.add(parentEntity3);
        parentEntities1.add(parentEntity4);
        cart1Item1.setParentEntities(parentEntities1);

        parentEntities2.add(parentEntity5);
        cart1Item2.setParentEntities(parentEntities2);

        cart1Item1.setCart(cart1);
        cart1Item2.setCart(cart1);


        cart1Items.add(cart1Item1);
        cart1Items.add(cart1Item2);


        cart1.setCartItems(cart1Items);

        cartRepository.save(cart1);


        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", cart.getFacilityName());
        params.put("userName", cart.getUserName());
        Cart myCart = cartRepository.getCartByFacilityNameAndUser(params);

        System.out.println("existCartId:" + myCart.getId());
        System.out.println("existCartId item count:" + myCart.getCartItems().size());

        assertEquals(2, myCart.getCartItems().size());

        for(CartItem c : myCart.getCartItems()) {
            //System.out.println("CartItem: entityType: " + c.getEntityType() + " entityId: " + c.getEntityId());

            if (c.getEntityId().equals(922222222)) {
                assertEquals(2, c.getParentEntities().size());

                for (ParentEntity p : c.getParentEntities()) {
                    System.out.println("ParentEntity: entityType: " + p.getEntityType() + " entityId: " + p.getEntityId());

                    if (p.getEntityType() == EntityType.investigation) {
                        assertEquals(new Long(2000001), p.getEntityId());
                    }

                    if (p.getEntityType() == EntityType.dataset) {
                        assertEquals(new Long(3000001), p.getEntityId());
                    }
                }

            }

            if (c.getEntityId().equals(922222222)) {
                assertEquals(1, c.getParentEntities().size());
            }

            assertEquals(EntityType.investigation, c.getParentEntities().get(0).getEntityType());

            /*for (ParentEntity p : c.getParentEntities()) {
                System.out.println("ParentEntity: entityType: " + p.getEntityType() + " entityId: " + p.getEntityId());
            }*/
        }


        //assertEquals(EntityType.investigation, myCart.getCartItems().get(0).getParentEntities().get(0).getEntityType());
        //assertEquals(EntityType.dataset, myCart.getCartItems().get(0).getParentEntities().get(1).getEntityType());

        //assertEquals(1, myCart.getCartItems().get(1).getParentEntities().size());
        //assertEquals(EntityType.investigation, myCart.getCartItems().get(1).getParentEntities().get(0).getEntityType());

    }

}
