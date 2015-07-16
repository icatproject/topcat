package org.icatproject.topcat.web.rest;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.icatproject.topcat.domain.Availability;
import org.icatproject.topcat.domain.BooleanValue;
import org.icatproject.topcat.domain.Cart;
import org.icatproject.topcat.domain.CartItem;
import org.icatproject.topcat.domain.Download;
import org.icatproject.topcat.domain.DownloadItem;
import org.icatproject.topcat.domain.EntityType;
import org.icatproject.topcat.domain.LongValue;
import org.icatproject.topcat.domain.StringValue;
import org.icatproject.topcat.domain.Status;
import org.icatproject.topcat.exceptions.AuthenticationException;
import org.icatproject.topcat.exceptions.IcatException;
import org.icatproject.topcat.exceptions.InternalException;
import org.icatproject.topcat.exceptions.TopcatException;
import org.icatproject.topcat.icatclient.ICATClientBean;
import org.icatproject.topcat.repository.CartRepository;
import org.icatproject.topcat.repository.DownloadRepository;

import com.github.javafaker.Faker;


@Stateless
@LocalBean
@Path("v1")
public class AdminResource {
    static final Logger logger = Logger.getLogger(AdminResource.class);

    @EJB
    private DownloadRepository downloadRepository;

    @EJB
    private CartRepository cartRepository;

    @EJB
    private ICATClientBean icatClientService;


    @GET
    @Path("/downloads/facility/{facilityName}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDownloadsByFacilityName(
            @PathParam("facilityName") String facilityName,
            @QueryParam("status") String status,
            @QueryParam("transport") String transport,
            @QueryParam("preparedId") String preparedId) {
        logger.info("getDownloadsByFacilityName() called");

        Map<String, String> params = new HashMap<String, String>();
        params.put("facilityName", facilityName);
        params.put("status", status);
        params.put("transport", transport);
        params.put("preparedId", preparedId);

        List<Download> downloads = new ArrayList<Download>();
        downloads = downloadRepository.getDownloadsByFacilityName(params);

        return Response.ok().entity(new GenericEntity<List<Download>>(downloads){}).build();
    }


    @GET
    @Path("/generate-fixture/download")
    @Produces({MediaType.APPLICATION_JSON})
    public Response generateDownloadFixtures() {
        logger.info("loadFixture() called");
        Faker faker = new Faker();
        Long count = 0L;

        String[] facilitites = {"dls", "sig"};
        String[] users = {"wayne", "rachel", "jane", "dave"};
        String[] transports = {"https", "globus"};

        for(int x = 0; x < 20; x++) {
            logger.info("loadFixture() called");
            int facilityIdx = new Random().nextInt(facilitites.length);
            String facility = (facilitites[facilityIdx]);

            int userIdx = new Random().nextInt(users.length);
            String user = (users[userIdx]);

            int transportIdx = new Random().nextInt(transports.length);
            String transport = (transports[transportIdx]);

            List<DownloadItem> downloadItems = new ArrayList<DownloadItem>();

            Download download = new Download();
            download.setFacilityName(facility);
            download.setFileName(faker.lorem().fixedString(12).toLowerCase().replace(" ", ""));
            download.setPreparedId(UUID.randomUUID().toString());
            download.setUserName(user);
            download.setTransport(transport);
            download.setStatus(Status.values()[new Random().nextInt(Status.values().length)]);
            download.setEmail(user + "@stfc.ac.uk");


            int numItems = new Random().nextInt(10 - 2) + 2;

            for(int i = 0; i < numItems; i++) {

                DownloadItem item = new DownloadItem();
                item.setEntityType(EntityType.values()[new Random().nextInt(EntityType.values().length)]);

                int entityId = new Random().nextInt(200000 - 1) + 1;

                item.setEntityId(new Long(entityId));
                item.setDownload(download);

                downloadItems.add(item);
                download.setDownloadItems(downloadItems);
            }

            downloadRepository.save(download);
            count++;
        }

        LongValue id = new LongValue(count);

        return Response.ok().entity(id).build();
    }

    @GET
    @Path("/generate-fixture/cart")
    @Produces({MediaType.APPLICATION_JSON})
    public Response generateCartFixtures() {
        logger.info("generateCartFixtures() called");

        List<CartItem> cartItems = new ArrayList<CartItem>();

        Cart cart = new Cart();
        cart.setFacilityName("dls");
        cart.setUserName("vcf21513");
        cart.setSize(null);
        cart.setAvailability(Availability.ARCHIVE);

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

        cartRepository.save(cart);

        LongValue id = new LongValue(cart.getId());

        return Response.ok().entity(id).build();
    }


    @GET
    @Path("/generate-fixture/cart1")
    @Produces({MediaType.APPLICATION_JSON})
    public Response generateCartFixtures1() {
        logger.info("generateCartFixtures1() called");

        List<CartItem> cartItems = new ArrayList<CartItem>();

        Cart cart = new Cart();
        cart.setFacilityName("dls");
        cart.setUserName("vcf21513");
        cart.setSize(null);
        cart.setAvailability(Availability.ONLINE);

        CartItem cartItem1 = new CartItem();
        cartItem1.setEntityId(new Long(922222222));
        cartItem1.setName("CM12170");
        cartItem1.setEntityType(EntityType.investigation);

        CartItem cartItem2 = new CartItem();
        cartItem2.setEntityId(new Long(922222223));
        cartItem2.setName("CM12170");
        cartItem2.setEntityType(EntityType.investigation);

        CartItem cartItem3 = new CartItem();
        cartItem3.setEntityId(new Long(922222224));
        cartItem3.setName("CM12170");
        cartItem3.setEntityType(EntityType.investigation);

        CartItem cartItem4 = new CartItem();
        cartItem4.setEntityId(new Long(922222225));
        cartItem4.setName("CM12170");
        cartItem4.setEntityType(EntityType.investigation);

        cartItem1.setCart(cart);
        cartItem2.setCart(cart);
        cartItem3.setCart(cart);
        cartItem4.setCart(cart);

        cartItems.add(cartItem1);
        cartItems.add(cartItem2);
        cartItems.add(cartItem3);
        cartItems.add(cartItem4);

        cart.setCartItems(cartItems);

        cartRepository.save(cart);

        LongValue id = new LongValue(cart.getId());

        return Response.ok().entity(id).build();
    }


    @GET
    @Path("/generate-fixture/cart2")
    @Produces({MediaType.APPLICATION_JSON})
    public Response generateCartFixtures2() {
        logger.info("generateCartFixtures2() called");

        List<CartItem> cartItems = new ArrayList<CartItem>();

        Cart cart = new Cart();
        cart.setFacilityName("dls");
        cart.setUserName("vcf21513");
        cart.setSize(null);
        cart.setAvailability(Availability.ARCHIVE);

        CartItem cartItem1 = new CartItem();
        cartItem1.setEntityId(new Long(88888888));
        cartItem1.setName("CM12170");
        cartItem1.setEntityType(EntityType.investigation);

        cartItem1.setCart(cart);

        cartItems.add(cartItem1);

        cart.setCartItems(cartItems);

        cartRepository.save(cart);

        LongValue id = new LongValue(cart.getId());

        return Response.ok().entity(id).build();
    }


    @GET
    @Path("/ping")
    @Produces({MediaType.APPLICATION_JSON})
    public Response ping() {
        logger.info("ping() called");
        return Response.ok().entity("ok").build();

    }


    @POST
    @Path("/login")
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @FormParam("serverName") String serverName,
            @FormParam("authenticationType") String authenticationType,
            @FormParam("username") String username,
            @FormParam("password") String password) throws MalformedURLException, AuthenticationException, InternalException {

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("username", username);
        parameters.put("password", password);

        String icatSessionId = icatClientService.login(serverName, authenticationType, parameters);
        StringValue topcatIcatSession = new StringValue(icatSessionId);

        return Response.ok().entity(topcatIcatSession).build();
    }


    @GET
    @Path("/session/{serverName}/{icatSessionId}/valid")
    @Produces({MediaType.APPLICATION_JSON})
    public Response isSessionValid(@PathParam("serverName") String serverName, @QueryParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        Boolean result = icatClientService.isSessionValid(serverName, icatSessionId);
        BooleanValue value = new BooleanValue(result);

        return Response.ok().entity(value).build();
    }


    @GET
    @Path("/session/{serverName}/{icatSessionId}/remain")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getTimeRemaining(@PathParam("serverName") String serverName, @QueryParam("icatSessionId") String icatSessionId) throws TopcatException, MalformedURLException {
        Long result = icatClientService.getRemainingMinutes(serverName, icatSessionId);
        LongValue value = new LongValue(result);

        return Response.ok().entity(value).build();
    }

    @GET
    @Path("/exception/{status}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exceptionTest(@PathParam("status") int status) throws IcatException {
        if (status == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            throw new IcatException("Internal Exeption");
        }

        if (status == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new IcatException("Bad Request Exeption");
        }

        if (status == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new IcatException("Insufficient Privileges Exception");
        }

        if (status == HttpURLConnection.HTTP_BAD_REQUEST) {
            throw new IcatException("Bad Request Exception");
        }

        //throw internal for all others
        throw new IcatException("Internal Exeption");

    }



}
