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
package uk.ac.stfc.topcat.gwt.client.facility;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is Facility Plugin Factory which holds all the plugins that are
 * available in TopCAT.
 * <p>
 * 
 * @author Mr. Srikanth Nagella
 * @version 1.0, &nbsp; 30-APR-2010
 * @since iCAT Version 3.3
 */
public class FacilityPluginFactory {
    private static FacilityPluginFactory pluginFactory = new FacilityPluginFactory();
    // With out these GWT will not compile the plugins and add them to AJAX code
    private static DefaultFacilityPlugin defaultPlugin = DefaultFacilityPlugin.getInstance();
    @SuppressWarnings("unused")
    private static ISISFacilityPlugin isisPlugin = ISISFacilityPlugin.getInstance();
    @SuppressWarnings("unused")
    private static DiamondFacilityPlugin diamondPlugin = DiamondFacilityPlugin.getInstance();        
    
    private HashMap<String, FacilityPlugin> facilityPluginMap;

    private FacilityPluginFactory() {
        facilityPluginMap = new HashMap<String, FacilityPlugin>();
    }

    public static FacilityPluginFactory getInstance() {
        return pluginFactory;
    }

    public void registerPlugin(String pluginName, FacilityPlugin plugin) {
        facilityPluginMap.put(pluginName, plugin);
    }

    public FacilityPlugin getPlugin(String pluginName) {
        FacilityPlugin plugin = facilityPluginMap.get(pluginName);
        if (plugin == null)
            plugin = defaultPlugin;
        return plugin;
    }

    public int getNumberOfPlugins() {
        return facilityPluginMap.size();
    }

    public ArrayList<String> getPluginNames() {
        return new ArrayList<String>(facilityPluginMap.keySet());
    }
}
