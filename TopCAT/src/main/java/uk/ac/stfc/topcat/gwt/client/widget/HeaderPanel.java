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

/**
 * Imports
 */
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.google.gwt.user.client.ui.Image;

/**
 * This is a widget, A Header panel to the TopCAT. It holds Logo and
 * LoginPanels.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class HeaderPanel extends Composite {

    private LoginPanel loginPanel;
    private Image image;
    private LayoutContainer messageContainer;
    private Text message;

    public HeaderPanel() {

        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.setSize("100%", "100px");
        layoutContainer.setLayout(new TableRowLayout());

        image = new Image();
        image.setHeight("71px");
        TableData td_image = new TableData();
        td_image.setPadding(2);
        layoutContainer.add(image, td_image);
    
        messageContainer = new LayoutContainer();
        messageContainer.setLayout(new RowLayout(Orientation.VERTICAL));
        messageContainer.setWidth("500px");
        messageContainer.add(new LabelField("Announcement"), new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(10, 30, 5, 30)));
        message = new Text();
        messageContainer.add(message, new RowData(Style.DEFAULT, Style.DEFAULT, new Margins(5, 30, 5, 30)));
        layoutContainer.add(messageContainer);
        messageContainer.hide();

        loginPanel = new LoginPanel();
        loginPanel.getVerticalPanel().setSize("100%", "100%");
        loginPanel.setSize("100%", "100%");
        loginPanel.setAutoHeight(true);
        TableData td_loginPanel = new TableData();
        td_loginPanel.setPadding(2);
        td_loginPanel.setHorizontalAlign(HorizontalAlignment.RIGHT);
        td_loginPanel.setWidth("100%");
        layoutContainer.add(loginPanel, td_loginPanel);

        layoutContainer.setAutoHeight(true);
        layoutContainer.setBorders(true);
        initComponent(layoutContainer);
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    public void setLogoURL(String url) {
        image.setUrl(url);
    }

    public void setMessage(String message) {
        if (!message.isEmpty()) {
            this.message.setText(message);
            messageContainer.show();
        }
    }
}
