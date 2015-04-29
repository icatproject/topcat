import requests
import urllib
import json
from pprint import pprint
import getpass

requests.packages.urllib3.disable_warnings()

myDetails = {
    'dls' : {
        'username' : '',
        'plugin' : 'db'
    },
    'isis' : {
        'username' : '',
        'plugin' : 'uows'
    }
}

jsonConfigFile = 'app/data/config-multi.json'
jsonSessionFile = 'app/data/icatapi-session-multi.json'
loginUrlPath = 'session'
sessionJson = {}

#open config file
with open(jsonConfigFile) as file:
    data = json.load(file)

#do for each server in config
for key in data['facilities']:
    host = data['facilities'][key]['icatUrl'] + '/icat/'

    #get password
    print "please enter password for %s" % key
    password = getpass.getpass()

    dataValue = '{"plugin":"%s","credentials":[{"username":"%s"},{"password": "%s"}]}' % (myDetails[key]['plugin'], myDetails[key]['username'], password)
    payload = { 'json' : dataValue }
    payload = urllib.urlencode(payload)
    url = host + loginUrlPath

    print url

    headers = {'Content-Type' : 'application/x-www-form-urlencoded'}

    r = None

    try:
        r = requests.post(url, data=payload, headers=headers, verify=False)
    except requests.exceptions.RequestException as e:    # This is the correct syntax
        print e
        continue

    if (r.status_code != 200):
        print "Failed to get session for %s!!!\n" % key

    jsonResult = r.json()

    if 'sessionId' in jsonResult:
        session = { 'sessionId' : jsonResult['sessionId'] }
        sessionJson[key] = session


with open(jsonSessionFile, "w") as out:
    json.dump(sessionJson, out, sort_keys=False, indent=4, separators=(',', ': '))

print "File dumped to %s" % jsonSessionFile
