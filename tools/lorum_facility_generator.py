
import requests
import json
import time
import random




from faker import Faker

fake = Faker()

icat_url = "http://localhost:8080"
users_count = 100
instruments_count = 20
investigation_types_count = 20
dataset_types_count = 20
proposals_count = 20
investigations_per_proposal_count = 5
datasets_per_investigation_count = 3
co_investigators_per_investigation_count = 3
datafiles_per_dataset_count = 100

parameter_type_count = 3
permissible_string_value_count = 10

one_day_ago = time.strftime("%Y-%m-%dT%H:%M:%S.000Z", time.gmtime(int(time.time()) - (24 * 60 * 60)))
seven_days_from_now = time.strftime("%Y-%m-%dT%H:%M:%S.000Z", time.gmtime(int(time.time()) + (7 * 24 * 60 * 60)))

auth = json.dumps({
	"plugin": "simple",
	"credentials": [
		{"username": "root"},
		{"password": "root"}
	]
})

session_id = json.loads(requests.post(icat_url + "/icat/session", {"json": auth}).text)["sessionId"]

def write(entities):
	return json.loads(requests.post(icat_url + "/icat/entityManager", {
		"icatUrl": icat_url,
		"sessionId": session_id,
		"entities": json.dumps(entities)
	}).text)

def get(query):
	return json.loads(requests.get(icat_url + "/icat/entityManager", {
		"icatUrl": icat_url,
		"sessionId": session_id,
		"query": query
	}).text)


facility_id = write([{
	"Facility": {
		"fullName": "Lorum Ipsum Light Source",
		"name": "LILS"
	}
}])[0]


user_entities = []
for i in range(0, users_count):
	user_entities.append({
		"User": {
			"fullName": fake.name(),
			"name": "db/user" + str(i),
			"email":  fake.email()
		}
	})
user_ids = write(user_entities)

print user_ids

instrument_entities = []
for i in range(0, instruments_count):
	instrument_entities.append({
		"Instrument": {
			"fullName": "Instrument " + str(i),
			"name": "I" + str(i),
			"facility": {"id": facility_id}
		}
	})
instrument_ids = write(instrument_entities)
	

investigation_types_entities = []
for i in range(0, investigation_types_count):
	investigation_types_entities.append({
		"InvestigationType":  {
			"name":  "InvestigationType " + str(i),
			"description":  " ".join(fake.words()),
			"facility":  {"id":  facility_id}
		}
	})
investigation_type_ids = write(investigation_types_entities)
	


dataset_types_entities = []
for i in range(0, dataset_types_count):
	dataset_types_entities.append({
		"DatasetType":  {
			"name":  "DatasetType #{i + 1}",
			"description":  " ".join(fake.words()),
			"facility":  {"id":  facility_id}
		}
	})
dataset_type_ids = write(dataset_types_entities)
print dataset_type_ids


parameter_type_entities = []
for i in range(0, parameter_type_count):
	parameter_type_entities.append({
		"ParameterType":  {
			"name":  "Investigation ParameterType " + str(i),
			"valueType":  "STRING",
			"facility":  {"id":  facility_id},
			"units":  "foo",
			"permissibleStringValues":  map(lambda j: {"value": str(i) + ", " + str(j)}, range(0, permissible_string_value_count)),
			"applicableToInvestigation":  True,
			"applicableToDataset":  True,
			"applicableToDatafile":  True
		}
	})
investigation_parameter_type_ids = write(parameter_type_entities)
	

for i in range(0, proposals_count):
	name = "Proposal " + str(i)
	for j in range(0, investigations_per_proposal_count):

		investigation_type_id = random.choice(investigation_type_ids)

		instrument_id = random.choice(instrument_ids)
		investigation_user_ids = []
		while len(investigation_user_ids) < co_investigators_per_investigation_count + 1 and len(investigation_user_ids) <= users_count:
			user_id = random.choice(user_ids)
			if user_id not in investigation_user_ids:
				investigation_user_ids.append(user_id)
		investigation_user_entities = []
		for user_id in investigation_user_ids:
			investigation_user_entities.append({
				"user":  {"id":  user_id},
				"role":  "CO_INVESTIGATOR"
			})
		investigation_user_entities[0]["role"] = "PRINCIPAL_INVESTIGATOR"

		investigation_id = write([{
			"Investigation":  {
				"name":  name,
				"visitId":  name + " - " + str(i) + " " + str(j),
				"title":   " ".join(fake.words()),
				"startDate":  one_day_ago,
				"endDate":  seven_days_from_now,
				"facility":  {"id":  facility_id},
				"type":  {"id":  investigation_type_id},
				"investigationInstruments":  [
					{"instrument":  {"id":  instrument_id}}
				],
				"investigationUsers":  investigation_user_entities
			}
		}])[0]

		for k in range(0, datasets_per_investigation_count):

			dataset_type_id = random.choice(dataset_type_ids)

			dataset_id = write([{
				"Dataset":  {
					"name":  "Dataset " + str(k + 1),
					"type":  {"id":  dataset_type_id},
					"investigation":  {"id":  investigation_id}
				}
			}])[0]
			datafile_entities = []
			for l in range(0, datafiles_per_dataset_count):
				datafile_entities.append({
					"Datafile":  {
						"name":  "Datafile " + str(l + 1),
						"description":  " ".join(fake.words()),
						"location":  "/".join(fake.words()),
						"fileSize":  0,
						"dataset":  {"id":  dataset_id}
					}
				})
			write(datafile_entities)


# root_user_id = write([
# 	{
# 		"User":  {
# 			"fullName":  Faker::Name.name,
# 			"name":  "simple/root",
# 			"email":  "root@example.com"
# 		}
# 	}
# ]).first

# investigation_ids = get("select investigation.id from Investigation investigation limit 0, 31");

# investigation_ids.each do |investigation_id|
# 	write([{
# 		"InvestigationUser":  {
# 			"investigation":  {"id":  investigation_id},
# 			"user":  {"id":  root_user_id},
# 			"role":  "CO_INVESTIGATOR"
# 		}
# 	}])
# end

# write([
# 	{
# 		"ParameterType":  {
# 			"name":  "title",
# 			"valueType":  "STRING",
# 			"units":  "title",
# 			"applicableToDataCollection":  true,
# 			"facility":  {"id":  facility_id}
# 		},

# 	},
# 	{
# 		"ParameterType":  {
# 			"name":  "releaseDate",
# 			"valueType":  "DATE_AND_TIME",
# 			"units":  "releaseDate",
# 			"applicableToDataCollection":  true,
# 			"facility":  {"id":  facility_id}
# 		},
		
# 	},
# 	{
# 		"ParameterType":  {
# 			"name":  "createdBy",
# 			"valueType":  "STRING",
# 			"units":  "createdBy",
# 			"applicableToDataCollection":  true,
# 			"facility":  {"id":  facility_id}
# 		},

# 	}
# ])

# write([
# 	{
# 		"DatafileFormat":  {
# 			"facility":  {"id":  facility_id},
# 			"name":  "upload",
# 			"type":  "misc",
# 			"description":  "Uploads by the Topcat's users",
# 			"version":  "1"
# 		},

# 	}
# ])


# datafile_ids = get("select datafile.id from Datafile datafile limit 0, 300");

# colours = ["red", "blue", "green", "yellow", "cyan", "white"]

# parameter_type_id = write([
# 	{
# 		"ParameterType":  {
# 			"name":  "colour",
# 			"valueType":  "STRING",
# 			"units":  "colour",
# 			"applicableToDatafile":  true,
# 			"facility":  {"id":  facility_id},
# 			"permissibleStringValues":  colours.map{|colour| {:value":  colour} }
# 		}
# 	}
# ]).first

# write(datafile_ids.map{ |datafile_id|
# 	{
# 		"DatafileParameter":  {
# 			"datafile":  {"id":  datafile_id},
# 			"type":  {"id":  parameter_type_id},
# 			"stringValue":  colours[(rand * (colours.length - 1)).floor]
# 		}
# 	}
# })


# def random_longitude
# 	(180 * 2 * rand) - 180
# end

# def random_latitude
# 	(90 * 2 * rand) - 90
# end


# longitude_parameter_type_id = write([
# 	{
# 		"ParameterType":  {
# 			"name":  "longitude",
# 			"valueType":  "NUMERIC",
# 			"units":  "longitude",
# 			"applicableToDatafile":  true,
# 			"facility":  {"id":  facility_id}
# 		}
# 	}
# ]).first

# latitude_parameter_type_id = write([
# 	{
# 		"ParameterType":  {
# 			"name":  "latitude",
# 			"valueType":  "NUMERIC",
# 			"units":  "latitude",
# 			"applicableToDatafile":  true,
# 			"facility":  {"id":  facility_id}
# 		}
# 	}
# ]).first


# write(datafile_ids.map{ |datafile_id|
# 	{
# 		"DatafileParameter":  {
# 			"datafile":  {"id":  datafile_id},
# 			"type":  {"id":  longitude_parameter_type_id},
# 			"numericValue":  random_longitude
# 		}
# 	}
# })

# write(datafile_ids.map{ |datafile_id|
# 	{
# 		"DatafileParameter":  {
# 			"datafile":  {"id":  datafile_id},
# 			"type":  {"id":  latitude_parameter_type_id},
# 			"numericValue":  random_latitude
# 		}
# 	}
# })


# def random_climate
# 	climates = ['warm', 'hot', 'cold', 'windy', 'rainy']
# 	climate_count = (climates.count * rand).ceil

# 	out = []

# 	while out.count != climate_count
# 		current_climate = climates[(rand * climates.count).to_i -  1]
# 		out << current_climate if !out.include?(current_climate)
# 	end

# 	out.join(', ')
# end

# parameter_type_id = write([
# 	{
# 		"ParameterType":  {
# 			"name":  "climate",
# 			"valueType":  "STRING",
# 			"units":  "climate",
# 			"applicableToDatafile":  true,
# 			"facility":  {"id":  facility_id}
# 		}
# 	}
# ]).first


# write(datafile_ids.map{ |datafile_id|
# 	{
# 		"DatafileParameter":  {
# 			"datafile":  {"id":  datafile_id},
# 			"type":  {"id":  parameter_type_id},
# 			"stringValue":  random_climate
# 		}
# 	}
# })

# def random_start_date
# 	Time.at(Time.now - (365 * 24 * 60 * 60 * rand).to_i).strftime("%Y-%m-%dT%H:%M:%S.000Z")
# end

# parameter_type_id = write([
# 	{
# 		"ParameterType":  {
# 			"name":  "start_date",
# 			"valueType":  "DATE_AND_TIME",
# 			"units":  "start_date",
# 			"applicableToDatafile":  true,
# 			"facility":  {"id":  facility_id}
# 		}
# 	}
# ]).first


# write(datafile_ids.map{ |datafile_id|
# 	{
# 		"DatafileParameter":  {
# 			"datafile":  {"id":  datafile_id},
# 			"type":  {"id":  parameter_type_id},
# 			"dateTimeValue":  random_start_date
# 		}
# 	}
# })

