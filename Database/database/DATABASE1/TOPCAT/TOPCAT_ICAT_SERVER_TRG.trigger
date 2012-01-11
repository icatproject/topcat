<?xml version = '1.0' encoding = 'UTF-8'?>
<trigger xmlns="http://xmlns.oracle.com/jdeveloper/1111/offlinedb">
  <ID class="oracle.javatools.db.IdentifierBasedID">
    <identifier class="java.lang.String">50702501-7cf5-4e27-bc30-819f9925f1fa</identifier>
  </ID>
  <name>TOPCAT_ICAT_SERVER_TRG</name>
  <baseType>TABLE</baseType>
  <code>BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    IF :NEW.ID IS NULL THEN
      SELECT TOPCAT_ICAT_SERVER_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
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
  <source>CREATE TRIGGER TOPCAT_ICAT_SERVER_TRG
BEFORE INSERT ON TOPCAT_ICAT_SERVER
FOR EACH ROW 
BEGIN
  &lt;&lt;COLUMN_SEQUENCES&gt;&gt;
  BEGIN
    IF :NEW.ID IS NULL THEN
      SELECT TOPCAT_ICAT_SERVER_SEQ.NEXTVAL INTO :NEW.ID FROM DUAL;
    END IF;
  END COLUMN_SEQUENCES;
END;</source>
  <statementLevel>false</statementLevel>
  <tableID class="oracle.javatools.db.IdentifierBasedID">
    <name>TOPCAT_ICAT_SERVER</name>
    <identifier class="java.lang.String">6fa1d426-43b2-4a9f-a4f5-536ecad176da</identifier>
    <schemaName>TOPCAT</schemaName>
    <type>TABLE</type>
  </tableID>
  <timing>BEFORE</timing>
</trigger>
