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
package uk.ac.stfc.topcat.gwt.client.event;

import java.util.ArrayList;

import uk.ac.stfc.topcat.gwt.client.eventHandler.AddInvestigationTypeEventHandler;
import uk.ac.stfc.topcat.gwt.client.model.InvestigationType;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class AddInvestigationTypeEvent extends GwtEvent<AddInvestigationTypeEventHandler> {

    public static Type<AddInvestigationTypeEventHandler> TYPE = new Type<AddInvestigationTypeEventHandler>();

    public static HandlerRegistration register(final EventBus eventBus, final AddInvestigationTypeEventHandler handler) {
        return eventBus.addHandler(AddInvestigationTypeEvent.TYPE, handler);
    }

    public static HandlerRegistration registerToSource(final EventBus eventBus, Object source,
            final AddInvestigationTypeEventHandler handler) {
        return eventBus.addHandlerToSource(AddInvestigationTypeEvent.TYPE, source, handler);
    }

    private final String facilityName;
    private final ArrayList<InvestigationType> investigationTypes;

    public AddInvestigationTypeEvent(final String facilityName, final ArrayList<InvestigationType> investigationTypes) {
        this.facilityName = facilityName;
        this.investigationTypes = investigationTypes;
    }

    public String getFacilityName() {
        return this.facilityName;
    }

    public ArrayList<InvestigationType> getInvestigationTypes() {
        return this.investigationTypes;
    }

    @Override
    public Type<AddInvestigationTypeEventHandler> getAssociatedType() {
        return AddInvestigationTypeEvent.TYPE;
    }

    @Override
    protected void dispatch(final AddInvestigationTypeEventHandler handler) {
        handler.addInvestigationTypes(this);
    }
}
