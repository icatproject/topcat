#!/usr/bin/env ruby

travis_build_dir = ENV['TRAVIS_BUILD_DIR']
provision_dir = "#{travis_build_dir}/provision"
install_dir = "#{travis_build_dir}/install"
install_provision_dir = "#{install_dir}/provision"

Dir.mkdir(install_dir)
Dir.mkdir(install_provision_dir)

Dir.open(provision_dir).each do |name|
  current_file = "#{provision_dir}/#{name}"
  next if !File.file?(current_file)
  data = File.read(current_file)
  data.gsub!(/\/home\/vagrant/, install_dir)
  data.gsub!(/\/vagrant\/provision/, install_provision_dir)
  data.gsub!(/\/vagrant/, travis_build_dir)
  File.write("#{install_provision_dir}/#{name}", data)
end


exec %{

  cd install

  echo "mysql-server mysql-server/root_password password secret" | sudo debconf-set-selections 
  echo "mysql-server mysql-server/root_password_again password secret" | sudo debconf-set-selections
  sudo apt-get --assume-yes install mysql-server apache2 git software-properties-common python-software-properties unzip build-essential 

  echo "create database icat;" | mysql -u root --password=secret
  echo "create database topcat;" | mysql -u root --password=secret
  echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '' WITH GRANT OPTION" | mysql -u root --password=secret

  sudo cp ./provision/000-default.conf /etc/apache2/sites-available
  sudo a2enmod headers
  sudo /etc/init.d/apache2 restart

  wget download.java.net/glassfish/4.0/release/glassfish-4.0.zip
  sudo unzip -q glassfish-4.0.zip -d /opt

  wget http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.37.zip
  unzip -q mysql-connector-java-5.1.37.zip
  sudo cp ./mysql-connector-java-5.1.37/mysql-connector-java-5.1.37-bin.jar /opt/glassfish4/glassfish/domains/domain1/lib/ext

  wget https://www.icatproject.org/mvn/repo/org/icatproject/ids.plugin/1.3.0/ids.plugin-1.3.0.jar
  sudo cp ./ids.plugin-1.3.0.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs
  wget https://www.icatproject.org/mvn/repo/org/icatproject/icat.client/4.5.0/icat.client-4.5.0.jar
  sudo cp ./icat.client-4.5.0.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs
  wget https://www.icatproject.org/mvn/repo/org/icatproject/icat.utils/4.11.0/icat.utils-4.11.0.jar
  sudo cp ./icat.utils-4.11.0.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs
  wget https://www.icatproject.org/mvn/repo/org/icatproject/ids.storage_file/1.3.2/ids.storage_file-1.3.2.jar
  sudo cp ./ids.storage_file-1.3.2.jar /opt/glassfish4/glassfish/domains/domain1/lib/applibs

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

  wget https://www.icatproject.org/mvn/repo/org/icatproject/authn_simple/1.0.1/authn_simple-1.0.1-distro.zip
  unzip -q authn_simple-1.0.1-distro.zip
  cp ./provision/authn_simple.properties ./authn_simple/authn_simple.properties
  cp ./provision/authn_simple-setup.properties ./authn_simple/authn_simple-setup.properties
  cd ./authn_simple
  sudo ./setup configure
  sudo ./setup install
  cd ../
  sudo /opt/glassfish4/bin/asadmin -t set applications.application.authn_simple-1.0.1.deployment-order=80

  wget https://www.icatproject.org/mvn/repo/org/icatproject/icat.server/4.6.1/icat.server-4.6.1-distro.zip
  unzip -q icat.server-4.6.1-distro.zip
  cp ./provision/icat.properties ./icat.server/icat.properties
  cp ./provision/icat-setup.properties ./icat.server/icat-setup.properties
  cd ./icat.server
  sudo ./setup configure
  sudo ./setup install
  cd ../
  sudo /opt/glassfish4/bin/asadmin -t set applications.application.icat.server-4.6.1.deployment-order=100

  wget https://www.icatproject.org/mvn/repo/org/icatproject/ids.server/1.5.0/ids.server-1.5.0-distro.zip
  unzip -q ids.server-1.5.0-distro.zip
  cp ./provision/ids.properties ./ids.server/ids.properties
  cp ./provision/ids-setup.properties ./ids.server/ids-setup.properties
  sudo cp ./provision/ids.storage_file.main.properties /opt/glassfish4/glassfish/domains/domain1/config/ids.storage_file.main.properties
  sudo cp ./provision/ids.storage_file-setup.properties /opt/glassfish4/glassfish/domains/domain1/config/ids.storage_file-setup.properties
  mkdir data
  mkdir data/ids
  mkdir data/ids/cache
  mkdir data/preparedfiles
  cd ./ids.server
  sudo ./setup configure
  sudo ./setup install
  cd ../
  sudo /opt/glassfish4/bin/asadmin -t set applications.application.ids.server-1.5.0.deployment-order=120

  chmod 0755 ./provision/topcat_build_install
  
  curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
  sudo apt-get --assume-yes install nodejs maven phantomjs
  sudo npm install -g bower
  sudo npm install -g grunt-cli

  ./provision/topcat_build_install

  sudo /opt/glassfish4/bin/asadmin -t set applications.application.topcat-2.0.0-SNAPSHOT.deployment-order=140

  mysql -u root --password=secret --host=127.0.0.1 icat < ./provision/icat.sql
  sudo apt-get --assume-yes install ruby-dev
  sudo gem install rest-client
  sudo ruby ./provision/populate_lucene.rb

  cd ../yo

  grunt test


}.strip.split(/\s*\n\s*/).join(' && ')


