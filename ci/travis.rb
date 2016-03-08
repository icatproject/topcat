#!/usr/bin/env ruby

exec %{
  echo "create database icat;" | mysql -u root
  echo "create database topcat;" | mysql -u root
  echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '' WITH GRANT OPTION" | mysql -u root
  mkdir "install"
  cd install
  wget download.java.net/glassfish/4.0/release/glassfish-4.0.zip
  sudo unzip glassfish-4.0.zip -d ./

  wget http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.37.zip
  unzip mysql-connector-java-5.1.37.zip
  cp ./mysql-connector-java-5.1.37/mysql-connector-java-5.1.37-bin.jar ./glassfish4/glassfish/domains/domain1/lib/ext
}.strip.split(/\s*\n\s*/).join(' && ')

