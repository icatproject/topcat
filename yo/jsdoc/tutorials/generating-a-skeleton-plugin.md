
We'll now create a skeleton plugin:

	vagrant ssh
	topcat generate_plugin topcat_foo_plugin

Now we'll load our new plugin into Topcat:

Open yo/app/config/topcat_dev.json and modify the "plugins" section so it looks like the following:

	{
		...
		"plugins":[
	        "http://localhost:10080/topcat_foo_plugin"
	    ]
	}

Now refresh your browser and your new plugin should be now visable.

