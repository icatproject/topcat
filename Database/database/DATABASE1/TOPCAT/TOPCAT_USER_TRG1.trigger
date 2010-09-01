<?xml version = '1.0' encoding = 'UTF-8'?>
<trigger xmlns="http://xmlns.oracle.com/jdeveloper/1111/offlinedb">
  <ID class="oracle.javatools.db.IdentifierBasedID">
    <name>TOPCAT_USER_TRG1</name>
    <identifier class="java.lang.String">1fc5f015-45f1-4c1c-a0ce-d3040a63e746</identifier>
    <schemaName>TOPCAT</schemaName>
    <type>TRIGGER</type>
  </ID>
  <name>TOPCAT_USER_TRG1</name>
  <baseType>TABLE</baseType>
  <code>BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    SELECT TOPCAT_USER_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
  END COLUMN_SEQUENCES;
END;</code>
  <enabled>true</enabled>
  <events>
    <event>INSERT</event>
  </events>
  <schema>
    <ID class="oracle.javatools.db.IdentifierBasedID">
      <name>TOPCAT</name>
      <identifier class="java.lang.String">85623786-6607-4f0e-b2ab-4a079ee58967</identifier>
      <schemaName>SCHEMA1</schemaName>
      <type>SCHEMA</type>
    </ID>
    <name>TOPCAT</name>
  </schema>
  <source>CREATE TRIGGER TOPCAT_USER_TRG1
BEFORE INSERT ON TOPCAT_USER
FOR EACH ROW 
BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    SELECT TOPCAT_USER_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
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
