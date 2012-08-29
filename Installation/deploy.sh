#!/bin/bash

# $Id$

#set -x
opts="setupDB, deleteDB, create, delete, deploy, undeploy, setupICAT, deleteICAT"

if [ $# = 0 ]; then
    echo Must set one parameter from $opts 
    exit 1
fi

props=./deploy.props
if [ ! -f $props ]; then
    echo There is no $props file
    exit 1
fi
. $props

icatlist=icat.servers/icat.list
if [ ! -f $icatlist ]; then
    echo There is no $icatlist file
exit 1
fi

if [ -z "$databaseName" ]; then
    if [ $databaseType = oracle ]; then
    databaseName="XE"
    elif [ $databaseType = mysql ]; then
    databaseName="topcat"
    fi
fi

if [ -z "$databasePort" ]; then
    if [ $databaseType = oracle ]; then
    databasePort="1521"
    elif [ $databaseType = mysql ]; then
    databasePort="3306"
    fi
fi

if [ -z "$databaseURL" ]; then
    if [ $databaseType = oracle ]; then
    databaseURL="jdbc:$databaseType:thin:@//$databaseServer:$databasePort/$databaseName"
    elif [ $databaseType = mysql ]; then
    databaseURL="jdbc:$databaseType://$databaseServer:$databasePort/$databaseName"
    fi
fi

TopCATDB=TopCATDB
topcatBase=`basename $topcat`
topcatWarStem=${topcatBase%%.war}
asadmin="$glassfish/bin/asadmin --port $port"

for key in databaseType sysPW databaseServer topcatName topcatPW topcat glassfish port; do
    eval val='$'$key
    if [ -z "$val" ]; then
        echo $key must be set in $props file
        exit 1
	fi
done


case $1 in
setupDB) 
    if [ $databaseType = oracle ]; then
        sqlplus sys/$sysPW@$databaseServer AS SYSDBA <<EOF
        CREATE USER $topcatName PROFILE "DEFAULT" IDENTIFIED BY "$topcatPW" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
        GRANT CREATE DATABASE LINK TO $topcatName;
        GRANT CREATE LIBRARY TO $topcatName;
        GRANT CREATE MATERIALIZED VIEW TO $topcatName;
        GRANT CREATE OPERATOR TO $topcatName;
        GRANT CREATE PROCEDURE TO $topcatName;
        GRANT CREATE PUBLIC DATABASE LINK TO $topcatName;
        GRANT CREATE PUBLIC SYNONYM TO $topcatName;
        GRANT CREATE SEQUENCE TO $topcatName;
        GRANT CREATE SESSION TO $topcatName;
        GRANT CREATE SYNONYM TO $topcatName;
        GRANT CREATE TABLE TO $topcatName;
        GRANT CREATE TRIGGER TO $topcatName;
        GRANT CREATE TYPE TO $topcatName;
        GRANT CREATE VIEW TO $topcatName;
        GRANT UNLIMITED TABLESPACE TO $topcatName;
        GRANT "CONNECT" TO $topcatName;
        GRANT "PLUSTRACE" TO $topcatName;
        GRANT "RESOURCE" TO $topcatName;
        GRANT CREATE JOB TO $topcatName;
        exit;
EOF
  
    elif [ $databaseType = mysql ]; then
        mysqladmin -u root -p$sysPW -h $databaseServer create $databaseName
        echo "Created database $databaseName"
        mysql -u root -p$sysPW -h $databaseServer<<EOF
        CREATE USER '$topcatName'@'$databaseServer' IDENTIFIED BY '$topcatPW';
        GRANT ALL PRIVILEGES ON $databaseName.* TO '$topcatName'@'%';
EOF
        echo "Created user $topcatName"
    fi
;;

deleteDB) 
    if [ $databaseType = oracle ]; then
        sqlplus sys/$sysPW@$databaseServer AS SYSDBA <<EOF
        DROP USER $topcatName CASCADE;
EOF
        echo "Dropped user $topcatName"

    elif [ $databaseType = mysql ]; then
        mysql -u root -p$sysPW -h $databaseServer<<EOF
        DROP DATABASE $databaseName;
        DROP USER '$topcatName'@'$databaseServer';
EOF
        echo "Dropped database $databaseName and user $topcatName"
    fi
;;

setupICAT) 
    if [ $databaseType = oracle ]; then
        ID=0
        while read icat_descripter_file
        do
        if [ "${icat_descripter_file:0:1}" != "#" ]; then
        . $icat_descripter_file
        ID=`expr $ID + 1`
        sqlplus $topcatName/$topcatPW@$databaseServer <<EOF
        INSERT INTO TOPCAT_ICAT_SERVER (ID, NAME, SERVER_URL, DEFAULT_USER, DEFAULT_PASSWORD, PLUGIN_NAME, DOWNLOAD_PLUGIN_NAME, VERSION) VALUES ('$ID', '$FacilityName', '$ICAT_url', '', '', '', '', '$ICAT_version');
EOF
        fi
        done < $icatlist

    elif [ $databaseType = mysql ]; then
        ID=0
        while read icat_descripter_file
        do
        if [ "${icat_descripter_file:0:1}" != "#" ]; then
        . $icat_descripter_file
        ID=`expr $ID + 1`
        mysql -u $topcatName -p$topcatPW -h $databaseServer $databaseName<<EOF
        INSERT INTO TOPCAT_ICAT_SERVER (ID, NAME, SERVER_URL, DEFAULT_USER, DEFAULT_PASSWORD, PLUGIN_NAME, DOWNLOAD_PLUGIN_NAME, VERSION) VALUES ('$ID', '$FacilityName', '$ICAT_url', '', '', '', '', '$ICAT_version');
EOF
        fi
        done < $icatlist
        echo "ICAT connection information added to $databaseName"
    fi
;;


deleteICAT)
    if [ $databaseType = oracle ]; then
        sqlplus $topcatName/$topcatPW@$databaseServer <<EOF
        DELETE FROM TOPCAT_ICAT_SERVER;
EOF
        echo "ICAT connection information removed"

    elif [ $databaseType = mysql ]; then
        mysql -u $topcatName -p$topcatPW -h $databaseServer $databaseName<<EOF
        DELETE FROM TOPCAT_ICAT_SERVER;
EOF
        echo "ICAT connection information removed"
    fi
;;


create)
        $asadmin <<EOF
        create-jdbc-connection-pool \
            --allownoncomponentcallers false \
            --associatewiththread false \
            --creationretryattempts  0 \
            --creationretryinterval 10 \
            --leakreclaim false \
            --leaktimeout 0 \
            --validationmethod auto-commit \
            --datasourceclassname oracle.jdbc.pool.OracleDataSource \
            --failconnection false \
            --idletimeout 300 \
            --isconnectvalidatereq false \
            --isisolationguaranteed true \
            --lazyconnectionassociation false \
            --lazyconnectionenlistment false \
            --matchconnections false \
            --maxconnectionusagecount 0 \
            --maxpoolsize 32 \
            --maxwait 60000 \
            --nontransactionalconnections false \
            --poolresize 2 \
            --restype javax.sql.DataSource \
            --statementtimeout -1 \
            --steadypoolsize 8 \
            --validateatmostonceperiod 0 \
            --wrapjdbcobjects false \
            --ping \
            --property User=${topcatName}:Password=${topcatPW}:URL="'"${databaseURL}"'" $TopCATDB

        create-jdbc-resource  \
            --connectionpoolid $TopCATDB jdbc/$TopCATDB
EOF
    if [ $databaseType = oracle ]; then
        fname=$glassfish/glassfish/domains/domain1/lib/ojdbc*.jar
        if [ ! -f $fname ]; then
            echo Warning $fname does not exist
        fi

    elif [ $databaseType = mysql ]; then
        fname=$glassfish/glassfish/domains/domain1/lib/mysql-connector-java-*.jar
        if [ ! -f $fname ]; then
            echo Warning $fname does not exist
        fi
    fi
;;

delete)
    $asadmin <<EOF
        delete-jdbc-resource jdbc/$TopCATDB
        delete-jdbc-connection-pool $TopCATDB
EOF
;;

deploy)
    if [ -n "$topcat" -a '(' -z "$2" -o "$2" = topcat ')' ]; then
        echo "*** Deploying topcat"
        echo $0 $1 $topcat
        $asadmin --interactive=false <<EOF
        deploy $topcat
EOF
    fi
;;

undeploy)
    if [ -n "$topcat" -a '(' -z "$2" -o "$2" = topcat ')' ]; then
        echo "*** Undeploying topcat"
        echo $0 $1 $topcatWarStem
        $asadmin --interactive=false <<EOF
        undeploy $topcatWarStem
EOF
    fi
;;

*)
    echo Must set one parameter to $opts
    exit 1
esac;

#
# - the end -
#
