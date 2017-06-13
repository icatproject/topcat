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

  sudo apt-get --assume-yes install apache2 git software-properties-common python-software-properties unzip build-essential dos2unix

  echo "USE mysql; UPDATE user SET password=PASSWORD('secret') WHERE user='root'; FLUSH PRIVILEGES; " | mysql -u root
  echo "create database icat;" | mysql -u root --password=secret
  echo "create database topcat;" | mysql -u root --password=secret
  echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '' WITH GRANT OPTION" | mysql -u root --password=secret

  sudo cp provision/000-default.conf /etc/apache2/sites-available
  sudo a2enmod headers
  sudo a2enmod rewrite
  sudo /etc/init.d/apache2 restart

  wget --quiet https://s3-eu-west-1.amazonaws.com/payara.fish/Payara+Downloads/Payara+4.1.2.172/payara-4.1.2.172.zip
  unzip payara-4.1.2.172.zip
  mv payara41 glassfish4

  echo 'export PATH="$PATH:$HOME/glassfish4/glassfish/bin"' >> .profile
  source .profile

  asadmin start-domain
  asadmin set server.http-service.access-log.format="common"
  asadmin set server.http-service.access-logging-enabled=true
  asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=128
  asadmin set server.ejb-container.property.disable-nonportable-jndi-names="true"
  asadmin delete-ssl --type http-listener http-listener-2
  asadmin delete-network-listener http-listener-2
  asadmin create-network-listener --listenerport 8181 --protocol http-listener-2 http-listener-2
  asadmin create-ssl --type http-listener --certname s1as --ssl3enabled=false --ssl3tlsciphers +TLS_RSA_WITH_AES_256_CBC_SHA,+TLS_RSA_WITH_AES_128_CBC_SHA http-listener-2

  wget --quiet http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.37.zip
  unzip -q mysql-connector-java-5.1.37.zip
  cp ./mysql-connector-java-5.1.37/mysql-connector-java-5.1.37-bin.jar glassfish4/glassfish/domains/domain1/lib/ext

  wget --quiet https://repo.icatproject.org/repo/org/icatproject/ids.storage_file/1.4.0-SNAPSHOT/ids.storage_file-1.4.0-20170606.173228-2-distro.zip
  unzip -q ids.storage_file-1.4.0-20170606.173228-2-distro.zip
  cp ./provision/ids.storage_file-setup.properties ids.storage_file/setup.properties
  mkdir -p data/ids/cache
  cd ids.storage_file
  ./setup install
  cd ../




}.strip.split(/\s*\n\s*/).join(' && ')



  # wget --quiet https://www.icatproject.org/mvn/repo/org/icatproject/ids.storage_file/1.3.3/ids.storage_file-1.3.3.jar
  # cp ./ids.storage_file-1.3.3.jar glassfish4/glassfish/domains/domain1/lib/applibs

  # wget --quiet https://www.icatproject.org/mvn/repo/org/icatproject/authn.simple/1.1.0/authn.simple-1.1.0-distro.zip
  # unzip -q authn.simple-1.1.0-distro.zip
  # cp ./provision/authn_simple.properties ./authn.simple/authn_simple.properties
  # cp ./provision/authn_simple-setup.properties ./authn.simple/authn_simple-setup.properties
  # cd ./authn.simple
  # ./setup configure
  # ./setup install
  # cd ../
  # asadmin -t set applications.application.authn.simple-1.1.0.deployment-order=80

  # wget --quiet https://repo.icatproject.org/repo/org/icatproject/icat.server/4.8.0/icat.server-4.8.0-distro.zip
  # unzip -q icat.server-4.8.0-distro.zip
  # cp ./provision/icat.properties ./icat.server/icat.properties
  # cp ./provision/icat-setup.properties ./icat.server/icat-setup.properties
  # cd ./icat.server
  # sudo ./setup configure
  # sudo ./setup install
  # cd ../
  # asadmin -t set applications.application.icat.server-4.8.0.deployment-order=100

  # wget --quiet https://www.icatproject.org/mvn/repo/org/icatproject/ids.server/1.6.0/ids.server-1.6.0-distro.zip
  # unzip -q ids.server-1.6.0-distro.zip
  # cp provision/ids.properties ./ids.server/ids.properties
  # cp provision/ids-setup.properties ./ids.server/ids-setup.properties
  # cp provision/ids.storage_file.main.properties glassfish4/glassfish/domains/domain1/config/ids.storage_file.main.properties
  # cp provision/ids.storage_file-setup.properties glassfish4/glassfish/domains/domain1/config/ids.storage_file-setup.properties
  # mkdir data
  # mkdir data/ids
  # mkdir data/ids/cache
  # mkdir data/preparedfiles
  # cd ./ids.server
  # sudo ./setup configure
  # sudo ./setup install
  # cd ../
  # asadmin -t set applications.application.ids.server-1.6.0.deployment-order=120
  

  # cd ../tools
  # gem install rest-client
  # gem install faker
  # ruby lorum_facility_generator.rb
  # ruby #{install_provision_dir}/populate_lucene.rb
  # cd ../

  # curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
  # sudo apt-get --assume-yes install nodejs maven phantomjs
  # sudo npm install -g bower
  # sudo npm install -g grunt-cli

  # mvn clean install
  # cp ./target/topcat-*.zip ./install
  # cd install
  # unzip -o topcat-*.zip
  # cp provision/topcat.properties ./topcat
  # cp provision/topcat-setup.properties ./topcat
  # cp ../yo/app/config/topcat_dev.json.example ./topcat/topcat.json
  # cp ../yo/app/languages/lang.json ./topcat
  # cp ../yo/app/styles/topcat.css ./topcat
  # cd topcat
  # dos2unix ./setup
  # chmod 0755 ./setup
  # sudo ./setup install
  # cd ../

  # asadmin -t set applications.application.topcat-2.3.0-SNAPSHOT.deployment-order=140

  # cd ../yo

  # cp app/config/topcat_dev.json.example app/config/topcat_dev.json

  # grunt test
