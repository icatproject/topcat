/**
 * 
 * Copyright (c) 2009-2010
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
import java.util.Map;

import uk.ac.stfc.topcat.gwt.client.Constants;

import com.extjs.gxt.ui.client.widget.Composite;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.HtmlContainer;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;

/**
 * This is a widget, A Footer panel to the TopCAT. It holds links.
 * 
 */
public class FooterPanel extends Composite {

    private HtmlContainer html;

    public FooterPanel() {
        LayoutContainer layoutContainer = new LayoutContainer();
        layoutContainer.setHeight("60px");

        VBoxLayout vbl_layoutContainer = new VBoxLayout();
        vbl_layoutContainer.setVBoxLayoutAlign(VBoxLayoutAlign.CENTER);
        vbl_layoutContainer.setPack(BoxLayoutPack.END);
        layoutContainer.setLayout(vbl_layoutContainer);

        html = new HtmlContainer(
                "<div class=\"footer-links\" id='links'>Accessibility | Privacy Policy | Data Policy | Terms Of Use | Complaints Procedure | Feedback</div>");
        html.setAutoWidth(true);
        layoutContainer.add(html);
        initComponent(layoutContainer);
    }

    /**
     * Set the values of the footer links.
     * 
     * @param links
     */
    public void setLinks(Map<String, String> links) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"").append(links.get(Constants.ACCESSIBILITY)).append("\">Accessibility</a> | ");
        sb.append("<a href=\"").append(links.get(Constants.PRIVACY_POLICY)).append("\">Privacy Policy</a> | ");
        sb.append("<a href=\"").append(links.get(Constants.DATA_POLICY)).append("\">Data Policy</a> | ");
        sb.append("<a href=\"").append(links.get(Constants.TERMS_OF_USE)).append("\">Terms Of Use</a> | ");
        sb.append("<a href=\"").append(links.get(Constants.COMPLAINTS_PROCEDURE))
                .append("\">Complaints Procedure</a> | ");
        sb.append("<a href=\"").append(links.get(Constants.FEEDBACK)).append("\">Feedback</a>");
        html.add(new Html(sb.toString()), "#links");
    }

}
