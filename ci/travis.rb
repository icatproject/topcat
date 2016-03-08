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


}.strip.split(/\s*\n\s*/).join(' && ')

