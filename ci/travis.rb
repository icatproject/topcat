#!/usr/bin/env ruby

exec %{
  echo "create database icat;" | mysql -u root --password=secret
  echo "create database topcat;" | mysql -u root --password=secret
  echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'secret' WITH GRANT OPTION" | mysql -u
}.strip.split(/\n/).join(' && ')

