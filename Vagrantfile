# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/trusty32"
  config.vm.provider "virtualbox" do |v|
      v.memory = 4096
    end
  config.vm.network "forwarded_port", guest: 80, host: 10080
  config.vm.network "forwarded_port", guest: 4848, host: 14848
  config.vm.network "forwarded_port", guest: 8181, host: 18181
  config.vm.network "forwarded_port", guest: 3306, host: 13306

  config.vm.provision "shell", inline: %{

    sudo apt-get update

    sudo debconf-set-selections <<< "mysql-server mysql-server/root_password password secret"
    sudo debconf-set-selections <<< "mysql-server mysql-server/root_password_again password secret"
    sudo apt-get --assume-yes install mysql-server apache2 git software-properties-common python-software-properties unzip
    mysql -u root --password=secret < /vagrant/provision/icat.sql

    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo debconf-set-selections <<< "debconf shared/accepted-oracle-license-v1-1 select true"
    sudo debconf-set-selections <<< "debconf shared/accepted-oracle-license-v1-1 seen true"
    sudo apt-get --assume-yes install oracle-java7-installer

    wget download.java.net/glassfish/4.0/release/glassfish-4.0.zip
    sudo unzip glassfish-4.0.zip -d /opt
 
    wget http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.37.zip
    unzip mysql-connector-java-5.1.37.zip
    sudo cp /home/vagrant/mysql-connector-java-5.1.37/mysql-connector-java-5.1.37-bin.jar /opt/glassfish4/glassfish/domains/domain1/lib/ext

    wget http://www.icatproject.org/mvn/repo/org/icatproject/ids.plugin/1.3.0/ids.plugin-1.3.0.jar
    sudo cp /home/vagrant/ids.plugin-1.3.0.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs
    wget http://www.icatproject.org/mvn/repo/org/icatproject/icat.client/4.5.0/icat.client-4.5.0.jar
    sudo cp /home/vagrant/icat.client-4.5.0.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs
    wget http://www.icatproject.org/mvn/repo/org/icatproject/icat.utils/4.11.0/icat.utils-4.11.0.jar
    sudo cp /home/vagrant/icat.utils-4.11.0.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs
    wget http://www.icatproject.org/mvn/repo/org/icatproject/ids.storage_file/1.3.2/ids.storage_file-1.3.2.jar
    sudo cp /home/vagrant/ids.storage_file-1.3.2.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs

    sudo /opt/glassfish4/bin/asadmin start-domain
    mkdir /home/vagrant/bin

    wget http://www.icatproject.org/mvn/repo/org/icatproject/authn_simple/1.0.1/authn_simple-1.0.1-distro.zip
    unzip authn_simple-1.0.1-distro.zip
    sudo cp /vagrant/provision/authn_simple.properties /home/vagrant/authn_simple/authn_simple.properties
    sudo cp /vagrant/provision/authn_simple-setup.properties /home/vagrant/authn_simple/authn_simple-setup.properties
    cd /home/vagrant/authn_simple
    sudo ./setup configure
    sudo ./setup install
    cd /home/vagrant

    sudo /opt/glassfish4/bin/asadmin -t set applications.application.authn_simple-1.0.1.deployment-order=60

    wget http://www.icatproject.org/mvn/repo/org/icatproject/icat.server/4.5.1/icat.server-4.5.1-distro.zip
    unzip icat.server-4.5.1-distro.zip
    sudo cp /vagrant/provision/icat.properties /home/vagrant/icat.server/icat.properties
    sudo cp /vagrant/provision/icat-setup.properties /home/vagrant/icat.server/icat-setup.properties
    cd /home/vagrant/icat.server
    sudo ./setup configure
    sudo ./setup install
    cd /home/vagrant

    sudo /opt/glassfish4/bin/asadmin -t set applications.application.icat.server-4.5.1.deployment-order=80


    wget http://www.icatproject.org/mvn/repo/org/icatproject/ids.server/1.5.0/ids.server-1.5.0-distro.zip
    unzip ids.server-1.5.0-distro.zip
    sudo cp /vagrant/provision/ids.properties /home/vagrant/ids.server/ids.properties
    sudo cp /vagrant/provision/ids-setup.properties /home/vagrant/ids.server/ids-setup.properties
    sudo cp /vagrant/provision/ids.storage_file.main.properties /opt/glassfish4/glassfish/domains/domain1/config/ids.storage_file.main.properties
    sudo cp /vagrant/provision/ids.storage_file-setup.properties /opt/glassfish4/glassfish/domains/domain1/config/ids.storage_file-setup.properties
    mkdir data
    mkdir data/ids
    mkdir data/ids/cache
    cd /home/vagrant/ids.server
    sudo ./setup configure
    sudo ./setup install
    sudo /opt/glassfish4/bin/asadmin -t set applications.application.ids.server-1.5.0.deployment-order=100
    cd /home/vagrant

    sudo cp /vagrant/provision/glassfish /etc/init.d/
    sudo chmod 0755 /etc/init.d/glassfish
    sudo update-rc.d glassfish defaults

    sudo rm -rf /home/vagrant/*.zip /home/vagrant/mysql-connector-java-5.1.37

    sudo cp /vagrant/provision/000-default.conf /etc/apache2/sites-available
    sudo /etc/init.d/apache2 restart

    sudo apt-get --assume-yes install nodejs npm maven 

  }
end
