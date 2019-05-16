

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

def manage_download_types():
	download_types = facility["downloadTransportTypes"]
	download_statuses = []
	for download_type in download_types:
		response = requests.get(topcat_url + "/topcat/user/downloadType/" + str(download_type["type"]) + "/status", params={
				"facilityName": facility_name,
				"sessionId": session_id
			})
		if response:
			download_status = json.loads(response.text)
			download_statuses.append(download_status)
		else:
			print "Response for '" + str(download_type["type"]) + "' not OK: " + str(response.status_code) + ", " + response.text
			print "Treating this download type as enabled"
			download_statuses.append({"disabled":False,"message":""})
	while True:
		print "Current download type statuses:"
		for i in range(len(download_types)):
			report = download_types[i]["type"]
			if download_statuses[i]["disabled"]:
				report += " (disabled, '" + download_statuses[i]["message"] + "')"
			else:
				report += " (enabled)"
			print str(i+1) + ": " + report
		print
		# Will a Topcat administrator ever be dumb enough to input a non-number here?
		try:
			option_number = int(raw_input("Choose a number to toggle that download type's status, or 0 to exit: "))
		except ValueError:
			# this will trigger the Invalid Input response below
			option_number = -1
		if option_number == 0: break
		option_number = option_number - 1
		if option_number not in range(len(download_types)):
			print "Invalid input"
			# We break here rather than continue, so if correct input is impossible at least we escape
			break
		download_type = download_types[option_number]
		download_status = download_statuses[option_number]
		if download_status["disabled"]:
			# Note: we don't reset the message - possible option to reuse it in future?
			response = requests.put(topcat_url + "/topcat/admin/downloadType/" + str(download_type["type"]) + "/status", data={
				"facilityName": facility_name,
				"sessionId": session_id,
				"disabled": False,
				"message": download_status["message"]
			})
			if response:
				print "Enabled download type " + download_type["type"]
			else:
				print "Request failed: " + str(response.status_code) + ", " + response.text
		else:
			message = raw_input("Set a message for the disabled download type: ")
			if not len(message) > 0:
				print "Message cannot be empty"
				continue
			response = requests.put(topcat_url + "/topcat/admin/downloadType/" + str(download_type["type"]) + "/status", data={
				"facilityName": facility_name,
				"sessionId": session_id,
				"disabled": True,
				"message": message
			})
			if response:
				print "Disabled download type " + download_type["type"]
			else:
				print "Request failed: " + str(response.status_code) + ", " + response.text
		# Update the local copy of the download status
		# We ASSUME the request is OK
		download_status = json.loads(requests.get(topcat_url + "/topcat/user/downloadType/" + str(download_type["type"]) + "/status", params={
				"facilityName": facility_name,
				"sessionId": session_id
			}).text)
		download_statuses[option_number] = download_status

while True:
	print ""
	print "What do you want to do?"
	print " * 1: Show download."
	print " * 2: Get a list of all the file locations for a download."
	print " * 3: Create preparedId for a download and generate update SQL."
	print " * 4: Set a download status to 'EXPIRED'."
	print " * 5: Expire all pending downloads."
	print " * 6: Enable or disable download types."
	print " * 7: Exit"

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
		manage_download_types()
	elif option_number == "7":
		break
	else:
		print ""
		print "Unknown option"

