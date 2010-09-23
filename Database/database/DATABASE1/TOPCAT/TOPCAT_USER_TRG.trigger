<?xml version = '1.0' encoding = 'UTF-8'?>
<trigger xmlns="http://xmlns.oracle.com/jdeveloper/1111/offlinedb">
  <ID class="oracle.javatools.db.IdentifierBasedID">
    <identifier class="java.lang.String">e16024c7-4c7c-4525-a5e2-b49fdcdd8293</identifier>
  </ID>
  <name>TOPCAT_USER_TRG</name>
  <baseType>TABLE</baseType>
  <code>BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    IF :NEW.ID IS NULL THEN
      SELECT TOPCAT_USER_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;</code>
  <enabled>true</enabled>
  <events>
    <event>INSERT</event>
  </events>
  <schema>
    <name>TOPCAT</name>
  </schema>
  <source>CREATE TRIGGER TOPCAT_USER_TRG
BEFORE INSERT ON TOPCAT_USER
FOR EACH ROW 
BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    IF :NEW.ID IS NULL THEN
      SELECT TOPCAT_USER_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;</source>
  <statementLevel>false</statementLevel>
  <tableID class="oracle.javatools.db.IdentifierBasedID">
    <name>TOPCAT_USER</name>
    <identifier class="java.lang.String">a93a3eda-5d62-4a4f-b600-d56b791b28cf</identifier>
    <schemaName>TOPCAT</schemaName>
    <type>TABLE</type>
  </tableID>
  <timing>BEFORE</timing>
</trigger>
