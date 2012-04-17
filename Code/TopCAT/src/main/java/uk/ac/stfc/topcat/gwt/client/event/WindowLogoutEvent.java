/**
 * 
 * Copyright (c) 2009-2012
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
package uk.ac.stfc.topcat.gwt.client.event;

import uk.ac.stfc.topcat.gwt.client.eventHandler.WindowLogoutEventHandler;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class WindowLogoutEvent extends GwtEvent<WindowLogoutEventHandler> {

    public static Type<WindowLogoutEventHandler> TYPE = new Type<WindowLogoutEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final WindowLogoutEventHandler handler) {
        return eventBus.addHandler(WindowLogoutEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final WindowLogoutEventHandler handler) {
        return eventBus.addHandlerToSource(WindowLogoutEvent.TYPE, source, handler);
    }

    private final String facilityName;

    public WindowLogoutEvent(final String facilityName) {
        this.facilityName = facilityName;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    @Override
    public Type<WindowLogoutEventHandler> getAssociatedType() {
        return WindowLogoutEvent.TYPE;
    }

    @Override
    protected void dispatch(final WindowLogoutEventHandler handler) {
        handler.logout(this);
    }
}
