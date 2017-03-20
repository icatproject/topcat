
After generating a plugin you will be presented with the following structure:

* src
	* main
		* java
			* RestApi.java - provides an interface to allow the front end talk to the server
		* resources
			* META-INF
				persistance.xml - provides the configuration neccessary to store stuff on the database
		* webapp
			* scripts
				* controllers - javascript files that control what gets sent to the view
				* plugin.js - the main entry point for the plugin, this is where dependencies get loaded in
			* views - html templates that have been extended with angular markup
			* WEB-INF - container configuration file
* pom.xml - maven configuration file

A Topcat plugin can actually be deployed by any web service e.g. Apache, the only requirement is that the "scripts/plugin.js" file needs to be present. However, in keeping with rest of the Icat family it is recommended you use the same stack as you'll get better support from the community.

