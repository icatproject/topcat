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
package uk.ac.stfc.topcat.gwt.client.authentication;

/**
 * Imports
 */
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is Authentication Plugin Factory which holds all the plugins that are
 * available in TopCAT.
 * 
 */
public class AuthenticationPluginFactory {
    private static AuthenticationPluginFactory pluginFactory = new AuthenticationPluginFactory();
    // With out these GWT will not compile the plugins and add them to AJAX code
    private static DefaultAuthenticationPlugin defaultPlugin = DefaultAuthenticationPlugin.getInstance();
    @SuppressWarnings("unused")
    private static CASAuthenticationPlugin casPlugin = CASAuthenticationPlugin.getInstance();
    @SuppressWarnings("unused")
    private static AnonymousAuthenticationPlugin anonPlugin = AnonymousAuthenticationPlugin.getInstance();    
    @SuppressWarnings("unused")
    private static ExternalRedirectAuthenticationPlugin extenalRedirectPlugin = ExternalRedirectAuthenticationPlugin.getInstance();

    private HashMap<String, AuthenticationPlugin> authenticationPluginMap;

    private AuthenticationPluginFactory() {
        authenticationPluginMap = new HashMap<String, AuthenticationPlugin>();
    }

    public static AuthenticationPluginFactory getInstance() {
        return pluginFactory;
    }

    public void registerPlugin(String pluginName, AuthenticationPlugin plugin) {
        authenticationPluginMap.put(pluginName, plugin);
    }

    public AuthenticationPlugin getPlugin(String pluginName) {
        AuthenticationPlugin plugin = authenticationPluginMap.get(pluginName);

        if (plugin == null)
            plugin = defaultPlugin;
        return plugin;
    }

    public int getNumberOfPlugins() {
        return authenticationPluginMap.size();
    }

    public ArrayList<String> getPluginNames() {
        return new ArrayList<String>(authenticationPluginMap.keySet());
    }
}
