<?xml version = '1.0' encoding = 'UTF-8'?>
<trigger xmlns="http://xmlns.oracle.com/jdeveloper/1111/offlinedb">
  <ID class="oracle.javatools.db.IdentifierBasedID">
    <name>TOPCAT_USER_INFO_TRG</name>
    <identifier class="java.lang.String">cd65275a-4d0b-41cc-b69a-3f1d5551aa33</identifier>
    <schemaName>TOPCAT</schemaName>
    <type>TRIGGER</type>
  </ID>
  <name>TOPCAT_USER_INFO_TRG</name>
  <baseType>TABLE</baseType>
  <code>BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    IF :NEW.ID IS NULL THEN
      SELECT TOPCAT_USER_INFO_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
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
  <source>CREATE TRIGGER TOPCAT_USER_INFO_TRG
BEFORE INSERT ON TOPCAT_USER_INFO
FOR EACH ROW 
BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    IF :NEW.ID IS NULL THEN
      SELECT TOPCAT_USER_INFO_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;</source>
  <statementLevel>false</statementLevel>
  <tableID class="oracle.javatools.db.IdentifierBasedID">
    <name>TOPCAT_USER_INFO</name>
    <identifier class="java.lang.String">097bd792-2e32-4c92-8e38-d3c46038b150</identifier>
    <schemaName>TOPCAT</schemaName>
    <type>TABLE</type>
  </tableID>
  <timing>BEFORE</timing>
</trigger>
