/**
 *
 * Copyright (c) 2009-2013
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 * Neither the name of the STFC nor the names of its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package uk.ac.stfc.topcat.gwt.client.widget;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.stfc.topcat.core.gwt.module.TFacility;
import uk.ac.stfc.topcat.gwt.client.Constants;
import uk.ac.stfc.topcat.gwt.client.LoginInterface;
import uk.ac.stfc.topcat.gwt.client.UtilityService;
import uk.ac.stfc.topcat.gwt.client.UtilityServiceAsync;
import uk.ac.stfc.topcat.gwt.client.authentication.AuthenticationPlugin;
import uk.ac.stfc.topcat.gwt.client.authentication.AuthenticationPluginFactory;
import uk.ac.stfc.topcat.gwt.client.callback.EventPipeLine;
import uk.ac.stfc.topcat.gwt.client.factory.MyCookieFactory;
import uk.ac.stfc.topcat.gwt.client.model.AuthenticationModel;
import uk.ac.stfc.topcat.gwt.client.model.TopcatCookie;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;
import com.google.web.bindery.autobean.shared.AutoBean;

/**
 * This class is a widget for login window. It will check to see what types of
 * login are available for the facility. If there are more than one type a combo
 * box will be displayed.
 * <p>
 *
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class LoginWidget extends Window {
    private final UtilityServiceAsync utilityService = UtilityService.Util.getInstance();
    private LoginInterface loginHandler = null;
    private LayoutContainer authTypeContainer = new LayoutContainer();
    private TFacility facility;
    private ComboBox<AuthenticationModel> authTypesBox;
    private LayoutContainer authenticationWidget;
    private AuthenticationPlugin plugin;

    public LoginWidget() {
        setBlinkModal(true);
        setModal(true);

        setHeadingText("New Window");
        RowLayout rowLayout = new RowLayout(Orientation.VERTICAL);
        setLayout(rowLayout);

        // Set up the container for the authentication types
        TableLayout tl_layoutContainer = new TableLayout(2);
        tl_layoutContainer.setCellSpacing(5);
        authTypeContainer.setLayout(tl_layoutContainer);

        // Set up the label and combo box for the authentication types
        LabelField lblfldAuthType = new LabelField("Authentication Type");
        authTypeContainer.add(lblfldAuthType);
        authTypesBox = getAuthTypesBox();
        authTypeContainer.add(authTypesBox);

        authTypeContainer.setAutoHeight(true);
        add(authTypeContainer);

        authenticationWidget = new LayoutContainer();
        authenticationWidget.setHeight("0px");
        authenticationWidget.setLayout(new FitLayout());
        authenticationWidget.setAutoHeight(true);
        add(authenticationWidget);

        setWidth(310);
        setLayout(new FitLayout());
        setAutoHeight(true);
    }

    public void setLoginHandler(LoginInterface loginHandler) {
        this.loginHandler = loginHandler;
    }

    @Override
    public void show() {
        if (authTypesBox.getStore().getCount() == 1) {
            if (plugin != null) {
                if (plugin.showable()) {
                    super.show();
                    setFocusWidget(plugin.getWidget());
                } else {
                    plugin.authenticate();
                }
            }
        } else {
            if (authTypesBox.getStore().getCount() > 1) {
                if (Cookies.getCookie("topcat") != null) {
                    //get topcat cookie
                    String cookie = Cookies.getCookie("topcat");
                    String lastAuthenticationType = "";
                    String lastFacility = "";

                    Map<String, String> servers = new HashMap<String, String>();
                    TopcatCookie topcatCookie;

                    //deserialize cookie
                    try {
                        topcatCookie = EventPipeLine.getInstance().deserializeCookie(cookie);
                    } catch(Exception e) {
                        //can't deserialize cookie so create a new one
                        MyCookieFactory factory = GWT.create(MyCookieFactory.class);
                        AutoBean<TopcatCookie> topcatCookieBean = factory.topcatCookie();
                        topcatCookie = topcatCookieBean.as();
                    }

                    lastFacility = topcatCookie.getLastAuthenticationFacility();

                    if (topcatCookie.getServers() != null) {
                        servers = topcatCookie.getServers();
                    }

                    lastAuthenticationType = servers.get(lastFacility);

                    //iterate liststore to see if authentication type matches the one set in cookie
                    for(AuthenticationModel authenticationModel : authTypesBox.getStore().getModels() ) {
                        //popup login box when not logged in and on a fresh page load
                        if (authenticationModel.getFacilityName().equalsIgnoreCase(lastFacility) && authenticationModel.getDisplayName().equalsIgnoreCase(lastAuthenticationType)){
                            //check if plugin is showable and display
                            if (AuthenticationPluginFactory.getInstance().getPlugin(lastAuthenticationType).showable()) {
                                authTypesBox.setValue(authenticationModel);
                                showPlugin(authenticationModel);
                            }
                        }
                    }

                    //iterate liststore to see if authentication type matches the one set in cookie
                    for(AuthenticationModel authenticationModel : authTypesBox.getStore().getModels() ) {
                        //popup for when a login box is clicked for a specific facility
                        if (authenticationModel.getFacilityName().equalsIgnoreCase(facility.getName()) && authenticationModel.getDisplayName().equalsIgnoreCase(servers.get(facility.getName()))){
                            //check if plugin is showable and display
                            if (AuthenticationPluginFactory.getInstance().getPlugin(servers.get(facility.getName())).showable()) {
                                authTypesBox.setValue(authenticationModel);
                                showPlugin(authenticationModel);
                            }
                        }
                    }

                    //use default as non matching cookie
                    super.show();
                } else {
                    super.show();
                }
            }
        }
    }

    /**
     * Show the login widget with the corresponding auth methods for the given
     * facility.
     *
     * @param facility
     */
    public void show(TFacility facility) {
        this.facility = facility;
        setHeadingText("Login to " + facility.getName());
        getAuthenticationTypes(facility.getName());
    }

    public String getFacilityName() {
        return facility.getName();
    }

    /**
     * Get the combo box for the authentication types.
     *
     * @return
     */
    private ComboBox<AuthenticationModel> getAuthTypesBox() {
        ComboBox<AuthenticationModel> authTypesBox = new ComboBox<AuthenticationModel>();
        authTypesBox.addSelectionChangedListener(new SelectionChangedListener<AuthenticationModel>() {
            @Override
            public void selectionChanged(SelectionChangedEvent<AuthenticationModel> se) {
                showPlugin(se.getSelectedItem());
            }
        });
        authTypesBox.setStore(new ListStore<AuthenticationModel>());
        authTypesBox.setDisplayField("displayName");
        authTypesBox.setTypeAhead(true);
        authTypesBox.setTriggerAction(TriggerAction.ALL);
        authTypesBox.addListener(Events.Expand, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });
        authTypesBox.addListener(Events.Collapse, new Listener<ComponentEvent>() {
            @Override
            public void handleEvent(ComponentEvent event) {
                EventPipeLine.getInstance().getTcEvents().fireResize();
            }
        });

        authTypesBox.select(1);

        return authTypesBox;
    }

    /**
     * Call out to get the list of authentication types for the given facility.
     *
     * @param facilityName
     */
    private void getAuthenticationTypes(final String facilityName) {
        plugin = null;
        authTypesBox.getStore().removeAll();
        authTypesBox.clear();
        authTypeContainer.hide();
        authenticationWidget.removeAll();
        EventPipeLine.getInstance().showRetrievingData();
        utilityService.getAuthenticationDetails(facilityName, new AsyncCallback<List<AuthenticationModel>>() {
            @Override
            public void onSuccess(List<AuthenticationModel> result) {
                EventPipeLine.getInstance().hideRetrievingData();
                authTypesBox.getStore().add(result);
                if (result.size() > 1) {
                    // there is more than one type so the user will have to
                    // select one
                    authTypesBox.getStore().sort("displayName", SortDir.ASC);
                    showAuthSelectionBox();
                } else if (result.size() == 1) {
                    // there is only one so we will use it
                    showPlugin(result.get(0));
                } else {
                    // oops no results
                    hide();
                    EventPipeLine.getInstance().showErrorDialog(
                            "Error no authentication types found for " + facilityName);
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                EventPipeLine.getInstance().hideRetrievingData();
                EventPipeLine.getInstance()
                        .showErrorDialog("Error retrieving authentication types for " + facilityName);
            }
        });
    }

    /**
     * Show the widget with the auth selection box
     */
    private void showAuthSelectionBox() {
        show();
        authTypeContainer.show();
        authTypesBox.focus();
    }

    /**
     * Show the widget with the selected auth plugin.
     *
     * @param model
     */
    private void showPlugin(AuthenticationModel model) {
        if (model == null) {
            // result of selecting auth type and then switching to a different
            // facility
            return;
        }

        authenticationWidget.removeAll();
        // get the required auth plugin
        plugin = AuthenticationPluginFactory.getInstance().getPlugin(model.getPluginName());
        plugin.setAuthenticationModel(model);
        plugin.setFacility(facility);
        plugin.setLoginHandler(loginHandler);

        //getcookie
        String cookie = Cookies.getCookie("topcat");

        //if cookie is not empty, deserialize it to a TopcatCookie object
        if (cookie != null) {
            TopcatCookie topcatCookie = null;
            Map<String, String> servers = new HashMap<String, String>();

            //handle problems if cookie cannot be serialized i.e old cookie format or cookie was edited by user
            try {
                topcatCookie = EventPipeLine.getInstance().deserializeCookie(cookie);
            } catch(Exception e) {
                //can't deserialize cookie so create a new one
                MyCookieFactory factory = GWT.create(MyCookieFactory.class);
                AutoBean<TopcatCookie> topcatCookieBean = factory.topcatCookie();
                topcatCookie = topcatCookieBean.as();
            }

            //set the last authentication
            topcatCookie.setLastAuthenticationFacility(model.getFacilityName());

            //make sure server is not null
            if (topcatCookie.getServers() != null) {
                servers = topcatCookie.getServers();
            }

            //add the last selected authentication type to map with facility as the key
            servers.put(model.getFacilityName(), model.getDisplayName());
            topcatCookie.setServers(servers);
            cookie = EventPipeLine.getInstance().serializeCookie(topcatCookie);
        } else {
            //create new TopcatCookie via AutoBean
            MyCookieFactory factory = GWT.create(MyCookieFactory.class);
            AutoBean<TopcatCookie> topcatCookieBean = factory.topcatCookie();
            TopcatCookie topcatCookie = topcatCookieBean.as();
            topcatCookie.setLastAuthenticationFacility(model.getFacilityName());

            Map<String, String> servers = new HashMap<String, String>();
            servers.put(model.getFacilityName(), model.getDisplayName());
            topcatCookie.setServers(servers);
            cookie = EventPipeLine.getInstance().serializeCookie(topcatCookie);
        }

        //work out expiry date
        Date now = new Date();
        CalendarUtil.addDaysToDate(now, Constants.LOGIN_COOKIE_NUMBER_OF_DAYS_EXPIRY);

        //set cookie
        Cookies.setCookie("topcat", cookie, now);


        if (plugin.showable()) {
            super.show();
            authenticationWidget.add(plugin.getWidget());
            authenticationWidget.layout(true);
            setFocusWidget(plugin.getWidget());
        }
    }

    /**
     * Determine if this widget contain only 1 authentication type and if the authentication
     * is showable. This prevents looping issue that may encounter if only a single
     * redirect authentication is set and the user is automatically redirected when
     * landing on the homepage or if a redirect authentication fails
     *
     * @return
     */
    public boolean isShowable() {
        boolean showable = false;

        if (authTypesBox.getStore().getCount() == 1) {
            List<AuthenticationModel> models = authTypesBox.getStore().getModels();
            AuthenticationModel aModel = models.get(0);

            plugin = AuthenticationPluginFactory.getInstance().getPlugin(aModel.getPluginName());

            if (plugin != null) {
                showable = plugin.showable();

            }
        }

        return showable;
    }

}
