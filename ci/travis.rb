#!/usr/bin/env ruby

exec %{
  echo "create database icat;" | mysql -u root
  echo "create database topcat;" | mysql -u root
  echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '' WITH GRANT OPTION" | mysql -u root
  mkdir "install"
  cd install
  wget download.java.net/glassfish/4.0/release/glassfish-4.0.zip
  unzip glassfish-4.0.zip -d ./

  wget http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.37.zip
  unzip mysql-connector-java-5.1.37.zip
  cp ./mysql-connector-java-5.1.37/mysql-connector-java-5.1.37-bin.jar ./glassfish4/glassfish/domains/domain1/lib/ext

  wget https://www.icatproject.org/mvn/repo/org/icatproject/ids.plugin/1.3.0/ids.plugin-1.3.0.jar
  cp ./ids.plugin-1.3.0.jar ./glassfish4/glassfish/domains/domain1/lib/applibs
  wget https://www.icatproject.org/mvn/repo/org/icatproject/icat.client/4.5.0/icat.client-4.5.0.jar
  cp ./icat.client-4.5.0.jar ./glassfish4/glassfish/domains/domain1/lib/applibs
  wget https://www.icatproject.org/mvn/repo/org/icatproject/icat.utils/4.11.0/icat.utils-4.11.0.jar
  cp ./icat.utils-4.11.0.jar ./glassfish4/glassfish/domains/domain1/lib/applibs
  wget https://www.icatproject.org/mvn/repo/org/icatproject/ids.storage_file/1.3.2/ids.storage_file-1.3.2.jar
  cp ./ids.storage_file-1.3.2.jar ./glassfish4/glassfish/domains/domain1/lib/applibs

  ./glassfish4/bin/asadmin start-domain
  ./opt/glassfish4/bin/asadmin set server.http-service.access-log.format="common"
  ./opt/glassfish4/bin/asadmin set server.http-service.access-logging-enabled=true
  ./opt/glassfish4/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=128
  ./opt/glassfish4/bin/asadmin set configs.config.server-config.cdi-service.enable-implicit-cdi=false
  ./opt/glassfish4/bin/asadmin set server.ejb-container.property.disable-nonportable-jndi-names="true"
  ./opt/glassfish4/bin/asadmin delete-ssl --type http-listener http-listener-2
  ./opt/glassfish4/bin/asadmin delete-network-listener http-listener-2
  ./opt/glassfish4/bin/asadmin create-network-listener --listenerport 8181 --protocol http-listener-2 http-listener-2
  ./opt/glassfish4/bin/asadmin create-ssl --type http-listener --certname s1as --ssl3enabled=false --ssl3tlsciphers +TLS_RSA_WITH_AES_256_CBC_SHA,+TLS_RSA_WITH_AES_128_CBC_SHA http-listener-2
  ./opt/glassfish4/bin/asadmin set configs.config.server-config.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=-1

}.strip.split(/\s*\n\s*/).join(' && ')

