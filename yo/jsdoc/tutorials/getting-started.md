
The first thing we need to do is create a development environment. In order to do you must first:

* install [VirtualBox](https://www.virtualbox.org/)
* install [Vagrant](https://www.vagrantup.com)
* install [Git](https://git-scm.com/)

Next we need to clone the Topcat repository:

	git clone https://github.com/icatproject/topcat.git

We'll now build out vagrant box:

	cd topcat
	vagrant up


Once the above step is complete you should be able view it by going to:

* https://localhost:8181/topcat/ping and making security exception and then
* http://localhost:10080

You can log into "Lorum Ipsum Light Source" with the following credentials:

* username: root
* password: root

You can connect to the MySQL database with the following details:

* host: localhost
* port: 13306
* username: root
* password: secret

Next we'll [generate a skelton plugin](tutorial-generating-a-skeleton-plugin.html)