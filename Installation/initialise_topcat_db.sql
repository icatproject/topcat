-- Initialise the ICAT SERVER , values in < > should be filled, 
INSERT INTO TOPCAT_ICAT_SERVER (ID, NAME, SERVER_URL, DEFAULT_USER, DEFAULT_PASSWORD, PLUGIN_NAME, DOWNLOAD_PLUGIN_NAME, VERSION) VALUES (1, <facility name>, <icat url with ?wsdl>, <default username>, <default password>, <facility search plugin>, <download plugin name>, <ICAT version number>);

-- Initialises the Anonymous User
INSERT INTO TOPCAT_USER_INFO (ID , DISPLAY_NAME, HOME_SERVER) VALUES (1, 'Anonymous', NULL);

-- Initialises the Sequence 
INSERT INTO SEQUENCE (SEQ_NAME , SEQ_COUNT) VALUES ('SEQ_GEN', 100);


