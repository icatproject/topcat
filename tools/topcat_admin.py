

import requests
import getpass
import json

topcat_url = raw_input("Topcat url: ")

topcatJson = json.loads(requests.get(topcat_url + "/config/topcat.json").text)

facilities = {}
for current_facility in topcatJson["facilities"]:
	facilities[current_facility["name"]] = current_facility

facility_names = facilities.keys()

if len(facility_names) > 1:
	print "Available facilities: "
	for facility_name in facility_names:
		print(" * " + facility_name)

	facility_name = raw_input("Which facility?: ")
else:
	facility_name = facility_names[0]

facility = facilities[facility_name]

ids_url = facility["idsUrl"]

if "icat_url" in facility:
	icat_url = facility["icatUrl"]
else:
	icat_url = requests.get(ids_url + "/ids/getIcatUrl").text

icat_properties = json.loads(requests.get(icat_url + "/icat/properties").text)


authentication_plugins = []
for authenticator in icat_properties["authenticators"]:
	authentication_plugins.append(authenticator["mnemonic"]) 

if len(authentication_plugins) > 1:
	print "Available authentication plugins: "
	for authentication_plugin in authentication_plugins:
		print(" * " + authentication_plugin)

	authentication_plugin = raw_input("Which authentication plugin?: ")
else:
	authentication_plugin = authentication_plugins[0]

username = raw_input("Username: ")
password = getpass.getpass("Password: ")

auth = json.dumps({
	"plugin": authentication_plugin,
	"credentials": [
		{"username": username},
		{"password": password}
	]
})

session_id = json.loads(requests.post(icat_url + "/icat/session", {"json": auth}).text)["sessionId"]

def show_download():
	download_id = raw_input("Enter download id: ")
	print requests.get(topcat_url + "/topcat/admin/downloads", params={
		"facilityName": facility_name,
		"sessionId": session_id,
		"queryOffset": "where download.id = " + download_id
	}).text


def list_file_locations():
	download_id = raw_input("Enter download id: ")
	output_file_name = raw_input("Output file name (optional): ")
	download = json.loads(requests.get(topcat_url + "/topcat/admin/downloads", params={
		"facilityName": facility_name,
		"sessionId": session_id,
		"queryOffset": "where download.id = " + download_id
	}).text)[0]
	download_items = download["downloadItems"]
	datafile_locations = []
	for download_item in download_items:
		if download_item["entityType"] == "investigation":
			datafile_locations.extend(json.loads(requests.get(icat_url + "/icat/entityManager", params={
				"sessionId": session_id,
				"query": "select datafile.location from Datafile datafile, datafile.dataset as dataset, dataset.investigation as investigation where investigation.id = " + str(download_item["entityId"])
			}).text))
		elif download_item["entityType"] == "dataset":
			datafile_locations.extend(json.loads(requests.get(icat_url + "/icat/entityManager", params={
				"sessionId": session_id,
				"query": "select datafile.location from Datafile datafile, datafile.dataset as dataset where dataset.id = " + str(download_item["entityId"])
			}).text))
		elif download_item["entityType"] == "datafile":
			datafile_locations.extend(json.loads(requests.get(icat_url + "/icat/entityManager", params={
				"sessionId": session_id,
				"query": "select datafile.location from Datafile datafile where datafile.id = " + str(download_item["entityId"])
			}).text))
	datafile_locations.sort()
	if output_file_name != "":
		file  = open(output_file_name, "w")
		for datafile_location in datafile_locations:
			file.write(datafile_location + "\n")
		file.close()
	else:
		for datafile_location in datafile_locations:
			print datafile_location


def prepare_download():
	download_id = raw_input("Enter download id: ")
	investigation_ids = []
	dataset_ids = []
	datafile_ids = []
	download = json.loads(requests.get(topcat_url + "/topcat/admin/downloads", params={
		"facilityName": facility_name,
		"sessionId": session_id,
		"queryOffset": "where download.id = " + download_id
	}).text)[0]
	download_items = download["downloadItems"]
	for download_item in download_items:
		if download_item["entityType"] == "investigation":
			investigation_ids.append(download_item["entityId"])
		elif download_item["entityType"] == "dataset":
			dataset_ids.append(download_item["entityId"])
		elif download_item["entityType"] == "datafile":
			datafile_ids.append(download_item["entityId"])
	params = {
		"zip": "true",
		"sessionId": session_id
	}
	if (len(investigation_ids) > 0):
		params["investigationIds"] = ",".join(map(str, investigation_ids))
	if (len(dataset_ids) > 0):
		params["datasetIds"] = ",".join(map(str, dataset_ids))
	if (len(datafile_ids) > 0):
		params["datafileIds"] = ",".join(map(str, datafile_ids))
	prepared_id = requests.post(ids_url + "/ids/prepareData", data=params).text
	print ""
	print "UPDATE DOWNLOAD set PREPARED_ID = '" + prepared_id + "', STATUS = 'RESTORING' WHERE ID = " + download_id




def expire_download():
	download_id = raw_input("Enter download id: ")
	requests.put(topcat_url + "/topcat/admin/download/" + download_id +  "/status", data={
		"facilityName": facility_name,
		"sessionId": session_id,
		"value": "EXPIRED"
	})

def expire_all_pending_downloads():
	query = "(download.status like 'PREPARING' or download.status like 'RESTORING') and download.isDeleted = false"

	facility_name = raw_input("Facility name (optional): ")

	if facility_name != "":
		query += " and download.facilityName = '" + facility_name + "'"

	downloads = json.loads(requests.get(topcat_url + "/topcat/admin/downloads", params={
		"facilityName": facility_name,
		"sessionId": session_id,
		"queryOffset": query
	}).text)
	for download in  downloads:
		requests.put(topcat_url + "/topcat/admin/download/" + str(download["id"]) +  "/status", data={
			"facilityName": facility_name,
			"sessionId": session_id,
			"value": "EXPIRED"
		})


while True:
	print ""
	print "What do you want to do?"
	print " * 1: Show download."
	print " * 2: Get a list of all the file locations for a download."
	print " * 3: Create preparedId for a download and generate update SQL."
	print " * 4: Set a download status to 'EXPIRED'."
	print " * 5: Expire all pending downloads."
	print " * 6: Exit"

	option_number = raw_input("Enter option number: ");


	if option_number == "1":
		show_download()
	elif option_number == "2":
		list_file_locations()
	elif option_number == "3":
		prepare_download()
	elif option_number == "4":
		expire_download()
	elif option_number == "5":
		expire_all_pending_downloads()
	elif option_number == "6":
		break
	else:
		print ""
		print "Unknown option"

