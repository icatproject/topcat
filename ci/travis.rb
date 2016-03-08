#!/usr/bin/env ruby

commands = (dirs.map do |dir|
  %{
    echo "create database icat;" | mysql -u root --password=secret
    echo "create database topcat;" | mysql -u root --password=secret
    echo "GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'secret' WITH GRANT OPTION" | mysql -u
    
  }
end).map{ |lines| lines.strip.split(/\n/).join(' && ') }.join(" && ")

exec commands
