# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.network "forwarded_port", guest: 80, host: 10080
  config.vm.network "forwarded_port", guest: 4848, host: 14848
  config.vm.network "forwarded_port", guest: 8181, host: 18181
  config.vm.network "forwarded_port", guest: 3306, host: 13306
  config.vm.box = "ubuntu/trusty32"
  config.vm.provider("virtualbox") { |v| v.memory = 1024 * 4 }
  config.vm.network :private_network, ip: '192.168.50.50'
  config.vm.synced_folder '.', '/vagrant', nfs: true

  config.vm.provision "shell", privileged: false, inline: %{
  
    sudo add-apt-repository ppa:openjdk-r/ppa
    sudo apt-get update

    sudo debconf-set-selections <<< "mysql-server mysql-server/root_password password secret"
    sudo debconf-set-selections <<< "mysql-server mysql-server/root_password_again password secret"
    sudo apt-get --assume-yes install mysql-server apache2 git software-properties-common python-software-properties unzip build-essential openjdk-8-jdk
    echo "create database icat;" | mysql -u root --password=secret
    echo "create database topcat;" | mysql -u root --password=secret
    echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'secret' WITH GRANT OPTION" | mysql -u root --password=secret
    sudo cp /vagrant/provision/my.cnf /etc/mysql/my.cnf
    sudo /etc/init.d/mysql restart

    #sudo add-apt-repository ppa:webupd8team/java -y
    #sudo apt-get update
    #sudo debconf-set-selections <<< "debconf shared/accepted-oracle-license-v1-1 select true"
    #sudo debconf-set-selections <<< "debconf shared/accepted-oracle-license-v1-1 seen true"
    #sudo apt-get --assume-yes install oracle-java8-installer

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
    sudo /opt/glassfish4/bin/asadmin set server.http-service.access-log.format="common"
    sudo /opt/glassfish4/bin/asadmin set server.http-service.access-logging-enabled=true
    sudo /opt/glassfish4/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=128
    sudo /opt/glassfish4/bin/asadmin set configs.config.server-config.cdi-service.enable-implicit-cdi=false
    sudo /opt/glassfish4/bin/asadmin set server.ejb-container.property.disable-nonportable-jndi-names="true"
    sudo /opt/glassfish4/bin/asadmin delete-ssl --type http-listener http-listener-2
    sudo /opt/glassfish4/bin/asadmin delete-network-listener http-listener-2
    sudo /opt/glassfish4/bin/asadmin create-network-listener --listenerport 8181 --protocol http-listener-2 http-listener-2
    sudo /opt/glassfish4/bin/asadmin create-ssl --type http-listener --certname s1as --ssl3enabled=false --ssl3tlsciphers +TLS_RSA_WITH_AES_256_CBC_SHA,+TLS_RSA_WITH_AES_128_CBC_SHA http-listener-2
    sudo /opt/glassfish4/bin/asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=-1
  

    mkdir /home/vagrant/bin

    wget http://www.icatproject.org/mvn/repo/org/icatproject/authn_simple/1.0.1/authn_simple-1.0.1-distro.zip
    unzip authn_simple-1.0.1-distro.zip
    sudo cp /vagrant/provision/authn_simple.properties /home/vagrant/authn_simple/authn_simple.properties
    sudo cp /vagrant/provision/authn_simple-setup.properties /home/vagrant/authn_simple/authn_simple-setup.properties
    cd /home/vagrant/authn_simple
    sudo ./setup configure
    sudo ./setup install
    cd /home/vagrant
    sudo /opt/glassfish4/bin/asadmin -t set applications.application.authn_simple-1.0.1.deployment-order=80

    wget http://icatproject.org/mvn/repo/org/icatproject/icat.server/4.6.0-SNAPSHOT/icat.server-4.6.0-20160108.125340-9-distro.zip
    unzip icat.server-4.6.0-20160108.125340-9-distro.zip
    sudo cp /vagrant/provision/icat.properties /home/vagrant/icat.server/icat.properties
    sudo cp /vagrant/provision/icat-setup.properties /home/vagrant/icat.server/icat-setup.properties
    cd /home/vagrant/icat.server
    sudo ./setup configure
    sudo ./setup install
    cd /home/vagrant

    sudo /opt/glassfish4/bin/asadmin -t set applications.application.icat.server-4.6.0-SNAPSHOT.deployment-order=100


    wget http://www.icatproject.org/mvn/repo/org/icatproject/ids.server/1.5.0/ids.server-1.5.0-distro.zip
    unzip ids.server-1.5.0-distro.zip
    sudo cp /vagrant/provision/ids.properties /home/vagrant/ids.server/ids.properties
    sudo cp /vagrant/provision/ids-setup.properties /home/vagrant/ids.server/ids-setup.properties
    sudo cp /vagrant/provision/ids.storage_file.main.properties /opt/glassfish4/glassfish/domains/domain1/config/ids.storage_file.main.properties
    sudo cp /vagrant/provision/ids.storage_file-setup.properties /opt/glassfish4/glassfish/domains/domain1/config/ids.storage_file-setup.properties
    mkdir data
    mkdir data/ids
    mkdir data/ids/cache
    mkdir data/preparedfiles
    cd /home/vagrant/ids.server
    sudo ./setup configure
    sudo ./setup install
    sudo /opt/glassfish4/bin/asadmin -t set applications.application.ids.server-1.5.0.deployment-order=120

    cd /home/vagrant

    sudo cp /vagrant/provision/glassfish /etc/init.d/
    sudo chmod 0755 /etc/init.d/glassfish
    sudo update-rc.d glassfish defaults

    sudo rm -rf /home/vagrant/*.zip /home/vagrant/mysql-connector-java-5.1.37

    sudo cp /vagrant/provision/000-default.conf /etc/apache2/sites-available
    sudo a2enmod headers
    sudo /etc/init.d/apache2 restart

    curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
    sudo apt-get --assume-yes install nodejs maven phantomjs
    sudo npm install -g bower
    sudo npm install -g grunt-cli
    sudo chown -R vagrant:vagrant /home/vagrant/.npm
    sudo cp /vagrant/provision/phantomjs_bin.sh /etc/profile.d
    source /vagrant/provision/phantomjs_bin.sh

    sudo debconf-set-selections <<< "iptables-persistent iptables-persistent/autosave_v4 boolean true"
    sudo debconf-set-selections <<< "iptables-persistent iptables-persistent/autosave_v6 boolean true"
    sudo apt-get --assume-yes install iptables-persistent
    sudo iptables -t nat -I OUTPUT -p tcp -o lo --dport 18181 -j REDIRECT --to-ports 8181
    sudo sh -c "iptables-save > /etc/iptables/rules.v4"
    sudo sh -c "ip6tables-save > /etc/iptables/rules.v6"

    sudo cp /vagrant/provision/topcat_build_install /usr/bin/topcat_build_install
    sudo chmod 755 /usr/bin/topcat_build_install
    topcat_build_install
    sudo /opt/glassfish4/bin/asadmin -t set applications.application.topcat-2.0.0-SNAPSHOT.deployment-order=140

    #/vagrant/provision/addContents https://localhost:8181 /vagrant/provision/import.txt simple username root password root

    mysql -u root --password=secret --host=127.0.0.1 icat < /vagrant/provision/icat.sql

    sudo apt-get --assume-yes  install python-pip
    sudo pip install suds
    cd /home/vagrant/icat.server
    ./icatadmin https://localhost:8181 simple username root password root -- populate

  }
end

