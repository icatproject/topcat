REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.



PROMPT
PROMPT TOPCAT  I N S T A L L
PROMPT
PROMPT This script will create TOPCAT schema objects in a named schema in a
PROMPT specified database.  
PROMPT


undefine database_name
undefine sys_password
undefine topcat_password
undefine externaltable_location
set define ON

ACCEPT database_name CHAR prompt          'Enter Database Name             : '
ACCEPT sys_password CHAR hide prompt      'Enter SYS password              : '
ACCEPT topcat_password CHAR prompt      'Enter topcat password       : '
ACCEPT externaltables_location CHAR prompt 'Enter External tables location : '

prompt
prompt ====================================================================
prompt creating user topcat
prompt
connect sys/&sys_password@&database_name as sysdba
CREATE USER topcat PROFILE "DEFAULT" IDENTIFIED BY "&topcat_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
GRANT CREATE DATABASE LINK TO topcat;
GRANT CREATE LIBRARY TO topcat;
GRANT CREATE MATERIALIZED VIEW TO topcat;
GRANT CREATE OPERATOR TO topcat;
GRANT CREATE PROCEDURE TO topcat;
GRANT CREATE PUBLIC DATABASE LINK TO topcat;
GRANT CREATE PUBLIC SYNONYM TO topcat;
GRANT CREATE SEQUENCE TO topcat;
GRANT CREATE SESSION TO topcat;
GRANT CREATE SYNONYM TO topcat;
GRANT CREATE TABLE TO topcat;
GRANT CREATE TRIGGER TO topcat;
GRANT CREATE TYPE TO topcat;
GRANT CREATE VIEW TO topcat;
GRANT UNLIMITED TABLESPACE TO topcat;
GRANT "CONNECT" TO topcat;
GRANT "PLUSTRACE" TO topcat;
GRANT "RESOURCE" TO topcat;
GRANT CREATE JOB TO topcat;



prompt
prompt Testing connection...
connect topcat/&topcat_password@&database_name


prompt
prompt
prompt If there are any error messages above then please CLOSE SQL*PLus NOW
prompt and report the error...
pause ...or press <Return> to continue
prompt

prompt
prompt ====================================================================
prompt Installation complete. 
prompt
exit;


