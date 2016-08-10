# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.network "forwarded_port", guest: 10080, host: 10080
  config.vm.network "forwarded_port", guest: 4848, host: 4848
  config.vm.network "forwarded_port", guest: 8181, host: 8181
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
    sudo apt-get --assume-yes install mysql-server apache2 git software-properties-common python-software-properties unzip -q build-essential openjdk-8-jdk dos2unix ruby-dev
    echo "create database icat;" | mysql -u root --password=secret
    echo "create database topcat;" | mysql -u root --password=secret
    echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'secret' WITH GRANT OPTION" | mysql -u root --password=secret
    sudo cp /vagrant/provision/my.cnf /etc/mysql/my.cnf
    sudo /etc/init.d/mysql restart
    sudo rm /usr/bin/java
    sudo rm /usr/bin/javac
    sudo ln -s /usr/lib/jvm/java-8-openjdk-i386/bin/java /usr/bin/java
    sudo ln -s /usr/lib/jvm/java-8-openjdk-i386/bin/javac /usr/bin/javac


    wget --quiet download.java.net/glassfish/4.0/release/glassfish-4.0.zip
    unzip -q glassfish-4.0.zip
 
    wget --quiet http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.37.zip
    unzip -q mysql-connector-java-5.1.37.zip
    cp /home/vagrant/mysql-connector-java-5.1.37/mysql-connector-java-5.1.37-bin.jar ./glassfish4/glassfish/domains/domain1/lib/ext

    wget --quiet https://www.icatproject.org/mvn/repo/org/icatproject/ids.storage_file/1.3.3/ids.storage_file-1.3.3.jar
    cp /home/vagrant/ids.storage_file-1.3.3.jar glassfish4/glassfish/domains/domain1/lib/applibs

    echo 'export PATH="$PATH:$HOME/glassfish4/glassfish/bin"' >> .profile
    source .profile

    asadmin start-domain
    asadmin set server.http-service.access-log.format="common"
    asadmin set server.http-service.access-logging-enabled=true
    asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=128
    asadmin set configs.config.server-config.cdi-service.enable-implicit-cdi=false
    asadmin set server.ejb-container.property.disable-nonportable-jndi-names="true"
    asadmin delete-ssl --type http-listener http-listener-2
    asadmin delete-network-listener http-listener-2
    asadmin create-network-listener --listenerport 8181 --protocol http-listener-2 http-listener-2
    asadmin create-ssl --type http-listener --certname s1as --ssl3enabled=false --ssl3tlsciphers +TLS_RSA_WITH_AES_256_CBC_SHA,+TLS_RSA_WITH_AES_128_CBC_SHA http-listener-2
    asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=-1
  

    mkdir /home/vagrant/bin

    wget --quiet https://www.icatproject.org/mvn/repo/org/icatproject/authn.simple/1.1.0/authn.simple-1.1.0-distro.zip

    unzip -q authn.simple-1.1.0-distro.zip
    cp /vagrant/provision/authn_simple.properties authn.simple/authn_simple.properties
    cp /vagrant/provision/authn_simple-setup.properties authn.simple/authn_simple-setup.properties
    cd authn.simple
    ./setup configure
    ./setup install
    cd ../
    asadmin -t set applications.application.authn.simple-1.1.0.deployment-order=80

    wget --quiet https://repo.icatproject.org/repo/org/icatproject/icat.server/4.8.0-SNAPSHOT/icat.server-4.8.0-20160729.140441-1-distro.zip
    unzip -q icat.server-4.8.0-20160729.140441-1-distro.zip
    cp /vagrant/provision/icat.properties icat.server/icat.properties
    cp /vagrant/provision/icat-setup.properties icat.server/icat-setup.properties
    cd icat.server
    ./setup configure
    ./setup install
    cd ../
    asadmin -t set applications.application.icat.server-4.8.0-SNAPSHOT.deployment-order=100


    wget --quiet https://www.icatproject.org/mvn/repo/org/icatproject/ids.server/1.6.0/ids.server-1.6.0-distro.zip
    unzip -q ids.server-1.6.0-distro.zip
    cp /vagrant/provision/ids.properties ids.server/ids.properties
    cp /vagrant/provision/ids-setup.properties ids.server/ids-setup.properties
    cp /vagrant/provision/ids.storage_file.main.properties glassfish4/glassfish/domains/domain1/config/ids.storage_file.main.properties
    cp /vagrant/provision/ids.storage_file-setup.properties glassfish4/glassfish/domains/domain1/config/ids.storage_file-setup.properties
    mkdir data
    mkdir data/ids
    mkdir data/ids/cache
    cd ids.server
    ./setup configure
    ./setup install
    asadmin -t set applications.application.ids.server-1.6.0.deployment-order=120

    cd ../

    sudo cp /vagrant/provision/glassfish /etc/init.d/
    sudo chmod 0755 /etc/init.d/glassfish
    sudo update-rc.d glassfish defaults

    rm -rf /home/vagrant/*.zip mysql-connector-java-5.1.37

    sudo cp /vagrant/provision/000-default.conf /etc/apache2/sites-available
    sudo a2enmod headers
    sudo a2enmod rewrite
    sudo /etc/init.d/apache2 restart

    curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
    sudo apt-get --assume-yes install nodejs maven phantomjs
    sudo update-ca-certificates -f
    sudo npm install -g bower
    sudo npm install -g grunt-cli
    cd /vagrant/yo
    bower install
    sudo chown -R vagrant:vagrant /home/vagrant/.npm
    sudo cp /vagrant/provision/phantomjs_bin.sh /etc/profile.d
    sudo dos2unix /etc/profile.d/phantomjs_bin.sh
    source /etc/profile.d/phantomjs_bin.sh

    curl -sSL https://get.rvm.io | bash
    source /home/vagrant/.rvm/scripts/rvm
    rvm install 2.3.1
    rvm use 2.3.1 --default
    gem install rest-client

    sudo debconf-set-selections <<< "iptables-persistent iptables-persistent/autosave_v4 boolean true"
    sudo debconf-set-selections <<< "iptables-persistent iptables-persistent/autosave_v6 boolean true"
    sudo apt-get --assume-yes install iptables-persistent
    sudo iptables -t nat -I OUTPUT -p tcp -o lo --dport 18181 -j REDIRECT --to-ports 8181
    sudo sh -c "iptables-save > /etc/iptables/rules.v4"
    sudo sh -c "ip6tables-save > /etc/iptables/rules.v6"

    sudo cp /vagrant/provision/topcat /usr/bin/topcat
    sudo chmod 755 /usr/bin/topcat
    sudo dos2unix /usr/bin/topcat
    topcat build_install_all
    asadmin -t set applications.application.topcat-2.2.0-SNAPSHOT.deployment-order=140

    mysql -u root --password=secret --host=127.0.0.1 icat < /vagrant/provision/icat.sql

    ruby /vagrant/provision/populate_lucene.rb

  }
end

