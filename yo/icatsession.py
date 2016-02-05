from suds.client import Client

import logging
import sys
import getpass

logging.basicConfig(level=logging.CRITICAL)

args = sys.argv
if len(args) < 3 or len(args) % 2 != 1:
    print >> sys.stderr, "\nThis must have two fixed arguments: url and plugin mnemonic\nfollowed by pairs of arguments to represent the credentials. For example\n\n    ", args[0], "https://example.com:8181 db username root password guess\n"
    sys.exit(1)

url = args[1]
plugin = args[2]

suffix = "/ICATService/ICAT?wsdl"
if not url.endswith(suffix): url = url + suffix
client = Client(url)
service = client.service
factory = client.factory

credentials = factory.create("credentials")
for i in range (3, len(args), 2):
    entry = factory.create("credentials.entry")
    entry.key = args[i]
    if args[i + 1] == "-":
        entry.value = getpass.getpass()
    else:
        entry.value = args[i + 1]
    credentials.entry.append(entry)

sessionId = service.login(plugin, credentials)
sys.stdout = open('app/data/icatapi-session.json', 'w')
print '{\n    "sessionId": "' + sessionId +'"\n}'

