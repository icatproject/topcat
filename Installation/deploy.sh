#!/bin/bash

#set -x
opts="setupDB, deleteDB, create, delete, deploy, undeploy, setupICAT, deleteICAT"

if [ $# = 0 ]; then
    echo Must set one parameter from $opts 
    exit 1
fi

props=./deploy.conf
if [ ! -f $props ]; then
    echo There is no $props file
    exit 1
fi
. $props

for key in DB_TYPE DB_ROOT_PASSWORD DB_HOSTNAME TOPCAT_DB_USER_NAME TOPCAT_DB_PASSWORD WAR_LOCATION GLASSFISH_HOME GLASSFISH_ADMIN_PORT; do
    eval val='$'$key
    if [ -z "$val" ]; then
        echo $key must be set in $props file
        exit 1
	fi
done

icatlist=icats.d/icat.list
if [ ! -f $icatlist ]; then
    echo There is no $icatlist file
exit 1
fi

if [ -z "$databaseName" ]; then
    if [ $DB_TYPE = oracle ]; then
    databaseName="XE"
    elif [ $DB_TYPE = mysql ]; then
    databaseName="topcat"
    fi
fi

if [ -z "$databasePort" ]; then
    if [ $DB_TYPE = oracle ]; then
    databasePort="1521"
    elif [ $DB_TYPE = mysql ]; then
    databasePort="3306"
    fi
fi

if [ -z "$databaseURL" ]; then
    if [ $DB_TYPE = oracle ]; then
    databaseURL="jdbc:$DB_TYPE:thin:@//$DB_HOSTNAME:$databasePort/$databaseName"
    elif [ $DB_TYPE = mysql ]; then
    databaseURL="jdbc:$DB_TYPE://$DB_HOSTNAME:$databasePort/$databaseName"
    fi
fi

TopCATDB=TopCATDB
topcatBase=`basename $WAR_LOCATION`
topcatWarStem=${topcatBase%%.war}
asadmin="$GLASSFISH_HOME/bin/asadmin --port $GLASSFISH_ADMIN_PORT"

case $1 in
setupDB) 
    if [ $DB_TYPE = oracle ]; then
        sqlplus sys/$DB_ROOT_PASSWORD@$DB_HOSTNAME AS SYSDBA <<EOF
        CREATE USER $TOPCAT_DB_USER_NAME PROFILE "DEFAULT" IDENTIFIED BY "$TOPCAT_DB_PASSWORD" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
        GRANT CREATE DATABASE LINK TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE LIBRARY TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE MATERIALIZED VIEW TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE OPERATOR TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE PROCEDURE TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE PUBLIC DATABASE LINK TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE PUBLIC SYNONYM TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE SEQUENCE TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE SESSION TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE SYNONYM TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE TABLE TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE TRIGGER TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE TYPE TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE VIEW TO $TOPCAT_DB_USER_NAME;
        GRANT UNLIMITED TABLESPACE TO $TOPCAT_DB_USER_NAME;
        GRANT "CONNECT" TO $TOPCAT_DB_USER_NAME;
        GRANT "PLUSTRACE" TO $TOPCAT_DB_USER_NAME;
        GRANT "RESOURCE" TO $TOPCAT_DB_USER_NAME;
        GRANT CREATE JOB TO $TOPCAT_DB_USER_NAME;
        exit;
EOF
  
    elif [ $DB_TYPE = mysql ]; then
        mysqladmin -u root -p$DB_ROOT_PASSWORD -h $DB_HOSTNAME create $databaseName
        echo "Created database $databaseName"
        mysql -u root -p$DB_ROOT_PASSWORD -h $DB_HOSTNAME<<EOF
        CREATE USER '$TOPCAT_DB_USER_NAME'@'$DB_HOSTNAME' IDENTIFIED BY '$TOPCAT_DB_PASSWORD';
        GRANT ALL PRIVILEGES ON $databaseName.* TO '$TOPCAT_DB_USER_NAME'@'%';
EOF
        echo "Created user $TOPCAT_DB_USER_NAME"
    fi
;;

deleteDB) 
    if [ $DB_TYPE = oracle ]; then
        sqlplus sys/$DB_ROOT_PASSWORD@$DB_HOSTNAME AS SYSDBA <<EOF
        DROP USER $TOPCAT_DB_USER_NAME CASCADE;
EOF
        echo "Dropped user $TOPCAT_DB_USER_NAME"

    elif [ $DB_TYPE = mysql ]; then
        mysql -u root -p$DB_ROOT_PASSWORD -h $DB_HOSTNAME<<EOF
        DROP DATABASE $databaseName;
        DROP USER '$TOPCAT_DB_USER_NAME'@'$DB_HOSTNAME';
EOF
        echo "Dropped database $databaseName and user $TOPCAT_DB_USER_NAME"
    fi
;;

setupICAT) 
    if [ $DB_TYPE = oracle ]; then
        ID=0
        while read icat_descripter_file
        do
        if [ "${icat_descripter_file:0:1}" != "#" ]; then
        . $icat_descripter_file
        ID=`expr $ID + 1`
        sqlplus $TOPCAT_DB_USER_NAME/$TOPCAT_DB_PASSWORD@$DB_HOSTNAME <<EOF
        INSERT INTO TOPCAT_ICAT_SERVER (ID, NAME, SERVER_URL, DEFAULT_USER, DEFAULT_PASSWORD, PLUGIN_NAME, DOWNLOAD_PLUGIN_NAME, VERSION) VALUES ('$ID', '$FACILITY_NAME', '$ICAT_URL', '', '', '', '', '$ICAT_VERSION');
EOF
        fi
        done < $icatlist

    elif [ $DB_TYPE = mysql ]; then
        ID=0
        while read icat_descripter_file
        do
        if [ "${icat_descripter_file:0:1}" != "#" ]; then
        . $icat_descripter_file
        ID=`expr $ID + 1`
        mysql -u $TOPCAT_DB_USER_NAME -p$TOPCAT_DB_PASSWORD -h $DB_HOSTNAME $databaseName<<EOF
        INSERT INTO TOPCAT_ICAT_SERVER (ID, NAME, SERVER_URL, DEFAULT_USER, DEFAULT_PASSWORD, PLUGIN_NAME, DOWNLOAD_PLUGIN_NAME, VERSION) VALUES ('$ID', '$FACILITY_NAME', '$ICAT_URL', '', '', '', '', '$ICAT_VERSION');
EOF
        fi
        done < $icatlist
        echo "ICAT connection information added to $databaseName"
    fi
;;


deleteICAT)
    if [ $DB_TYPE = oracle ]; then
        sqlplus $TOPCAT_DB_USER_NAME/$TOPCAT_DB_PASSWORD@$DB_HOSTNAME <<EOF
        DELETE FROM TOPCAT_ICAT_SERVER;
EOF
        echo "ICAT connection information removed"

    elif [ $DB_TYPE = mysql ]; then
        mysql -u $TOPCAT_DB_USER_NAME -p$TOPCAT_DB_PASSWORD -h $DB_HOSTNAME $databaseName<<EOF
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
            --property User=${TOPCAT_DB_USER_NAME}:Password=${TOPCAT_DB_PASSWORD}:URL="'"${databaseURL}"'" $TopCATDB

        create-jdbc-resource  \
            --connectionpoolid $TopCATDB jdbc/$TopCATDB
EOF
    if [ $DB_TYPE = oracle ]; then
        fname=$GLASSFISH_HOME/glassfish/domains/domain1/lib/ojdbc*.jar
        if [ ! -f $fname ]; then
            echo Warning $fname does not exist
        fi

    elif [ $DB_TYPE = mysql ]; then
        fname=$GLASSFISH_HOME/glassfish/domains/domain1/lib/mysql-connector-java-*.jar
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
    if [ -n "$WAR_LOCATION" -a '(' -z "$2" -o "$2" = topcat ')' ]; then
        echo "*** Deploying topcat"
        echo $0 $1 $WAR_LOCATION
        $asadmin --interactive=false <<EOF
        deploy $WAR_LOCATION
EOF
    fi
;;

undeploy)
    if [ -n "$WAR_LOCATION" -a '(' -z "$2" -o "$2" = topcat ')' ]; then
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

