
import requests
import faker
import json
import time

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
	json.loads(requests.post(icat_url + "/icat/session", {
		"icatUrl": icat_url,
		"sessionId": session_id,
		"entities": json.dumps(entities)
	}).text)

def get(query):
	json.loads(requests.get(icat_url + "/icat/entityManager", {
		"icatUrl": icat_url,
		"sessionId": session_id,
		"entities": json.dumps(entities)
	}).text)


facility_id = write([{
	"Facility": {
		"fullName": "Lorum Ipsum Light Source",
		"name": "LILS"
	}
}])[0]

# user_ids = write(users_count.times.map do |i|
# 	{
# 		:User => {
# 			:fullName => Faker::Name.name,
# 			:name => "db/user#{i + 1}",
# 			:email => Faker::Internet.email
# 		}
# 	}
# end)

# instrument_ids = write(instruments_count.times.map do |i|
# 	{
# 		:Instrument => {
# 			:fullName => "Instrument #{i + 1}",
# 			:name => "I#{i + 1}",
# 			:facility => {:id => facility_id}
# 		}
# 	}
# end)

# investigation_type_ids = write(investigation_types_count.times.map do |i|
# 	{
# 		:InvestigationType => {
# 			:name => "InvestigationType #{i + 1}",
# 			:description => Faker::Lorem.words.join(' '),
# 			:facility => {:id => facility_id}
# 		}
# 	}
# end)

# dataset_type_ids = write(dataset_types_count.times.map do |i|
# 	{
# 		:DatasetType => {
# 			:name => "DatasetType #{i + 1}",
# 			:description => Faker::Lorem.words.join(' '),
# 			:facility => {:id => facility_id}
# 		}
# 	}
# end)


# investigation_parameter_type_ids = write(parameter_type_count.times.map { |i|
# 	{
# 		:ParameterType => {
# 			:name => "Investigation ParameterType #{i + 1}",
# 			:valueType => "STRING",
# 			:facility => {:id => facility_id},
# 			:units => "foo",
# 			:permissibleStringValues => permissible_string_value_count.times.map{ |j|
# 				{
# 					:value => "#{i} - #{j}"
# 				}
# 			},
# 			:applicableToInvestigation => true,
# 			:applicableToDataset => true,
# 			:applicableToDatafile => true
# 		}
# 	}
# })



# proposals_count.times do |i|
# 	name = "Proposal #{i + 1}"
# 	investigations_per_proposal_count.times do |j|
# 		investigation_type_id = investigation_type_ids[(rand * investigation_types_count).floor]
# 		instrument_id = instrument_ids[(rand * instruments_count).floor]
		
# 		investigation_user_ids = []
# 		while (investigation_user_ids.count < co_investigators_per_investigation_count + 1) && (investigation_user_ids.count <= users_count)
# 			user_id = user_ids[(rand * users_count).floor]
# 			investigation_user_ids << user_id if !investigation_user_ids.include?(user_id)
# 		end

# 		investigation_users = investigation_user_ids.map do |user_id|
# 			{
# 				:user => {:id => user_id},
# 				:role => "CO_INVESTIGATOR"
# 			}
# 		end

# 		investigation_users[0][:role] = "PRINCIPAL_INVESTIGATOR"

# 		investigation_id = write([{
# 			:Investigation => {
# 				:name => name,
# 				:visitId => "#{name} - #{j + 1}",
# 				:title => Faker::Lorem.words.join(' '),
# 				:startDate => one_day_ago,
# 				:endDate => seven_days_from_now,
# 				:facility => {:id => facility_id},
# 				:type => {:id => investigation_type_id},
# 				:investigationInstruments => [
# 					{:instrument => {:id => instrument_id}}
# 				],
# 				:investigationUsers => investigation_users
# 			}
# 		}]).first

# 		datasets_per_investigation_count.times.each do |k|
# 			dataset_type_id = dataset_type_ids[(rand * dataset_types_count).floor]

# 			dataset_id = write([{
# 				:Dataset => {
# 					:name => "Dataset #{k + 1}",
# 					:type => {:id => dataset_type_id},
# 					:investigation => {:id => investigation_id}
# 				}
# 			}]).first

# 			write(datafiles_per_dataset_count.times.map do |l|
# 				{
# 					:Datafile => {
# 						:name => "Datafile #{l + 1}",
# 						:description => Faker::Lorem.words.join(' '),
# 						:location => Faker::Lorem.words.join('/'),
# 						:fileSize => (rand * 1000000).floor,
# 						:dataset => {:id => dataset_id}
# 					}
# 				}
# 			end)

# 		end

# 	end
# end

# root_user_id = write([
# 	{
# 		:User => {
# 			:fullName => Faker::Name.name,
# 			:name => "simple/root",
# 			:email => "root@example.com"
# 		}
# 	}
# ]).first

# investigation_ids = get("select investigation.id from Investigation investigation limit 0, 31");

# investigation_ids.each do |investigation_id|
# 	write([{
# 		:InvestigationUser => {
# 			:investigation => {:id => investigation_id},
# 			:user => {:id => root_user_id},
# 			:role => "CO_INVESTIGATOR"
# 		}
# 	}])
# end

# write([
# 	{
# 		:ParameterType => {
# 			:name => "title",
# 			:valueType => "STRING",
# 			:units => "title",
# 			:applicableToDataCollection => true,
# 			:facility => {:id => facility_id}
# 		},

# 	},
# 	{
# 		:ParameterType => {
# 			:name => "releaseDate",
# 			:valueType => "DATE_AND_TIME",
# 			:units => "releaseDate",
# 			:applicableToDataCollection => true,
# 			:facility => {:id => facility_id}
# 		},
		
# 	},
# 	{
# 		:ParameterType => {
# 			:name => "createdBy",
# 			:valueType => "STRING",
# 			:units => "createdBy",
# 			:applicableToDataCollection => true,
# 			:facility => {:id => facility_id}
# 		},

# 	}
# ])

# write([
# 	{
# 		:DatafileFormat => {
# 			:facility => {:id => facility_id},
# 			:name => "upload",
# 			:type => "misc",
# 			:description => "Uploads by the Topcat's users",
# 			:version => "1"
# 		},

# 	}
# ])


# datafile_ids = get("select datafile.id from Datafile datafile limit 0, 300");

# colours = ["red", "blue", "green", "yellow", "cyan", "white"]

# parameter_type_id = write([
# 	{
# 		:ParameterType => {
# 			:name => "colour",
# 			:valueType => "STRING",
# 			:units => "colour",
# 			:applicableToDatafile => true,
# 			:facility => {:id => facility_id},
# 			:permissibleStringValues => colours.map{|colour| {:value => colour} }
# 		}
# 	}
# ]).first

# write(datafile_ids.map{ |datafile_id|
# 	{
# 		:DatafileParameter => {
# 			:datafile => {:id => datafile_id},
# 			:type => {:id => parameter_type_id},
# 			:stringValue => colours[(rand * (colours.length - 1)).floor]
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
# 		:ParameterType => {
# 			:name => "longitude",
# 			:valueType => "NUMERIC",
# 			:units => "longitude",
# 			:applicableToDatafile => true,
# 			:facility => {:id => facility_id}
# 		}
# 	}
# ]).first

# latitude_parameter_type_id = write([
# 	{
# 		:ParameterType => {
# 			:name => "latitude",
# 			:valueType => "NUMERIC",
# 			:units => "latitude",
# 			:applicableToDatafile => true,
# 			:facility => {:id => facility_id}
# 		}
# 	}
# ]).first


# write(datafile_ids.map{ |datafile_id|
# 	{
# 		:DatafileParameter => {
# 			:datafile => {:id => datafile_id},
# 			:type => {:id => longitude_parameter_type_id},
# 			:numericValue => random_longitude
# 		}
# 	}
# })

# write(datafile_ids.map{ |datafile_id|
# 	{
# 		:DatafileParameter => {
# 			:datafile => {:id => datafile_id},
# 			:type => {:id => latitude_parameter_type_id},
# 			:numericValue => random_latitude
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
# 		:ParameterType => {
# 			:name => "climate",
# 			:valueType => "STRING",
# 			:units => "climate",
# 			:applicableToDatafile => true,
# 			:facility => {:id => facility_id}
# 		}
# 	}
# ]).first


# write(datafile_ids.map{ |datafile_id|
# 	{
# 		:DatafileParameter => {
# 			:datafile => {:id => datafile_id},
# 			:type => {:id => parameter_type_id},
# 			:stringValue => random_climate
# 		}
# 	}
# })

# def random_start_date
# 	Time.at(Time.now - (365 * 24 * 60 * 60 * rand).to_i).strftime("%Y-%m-%dT%H:%M:%S.000Z")
# end

# parameter_type_id = write([
# 	{
# 		:ParameterType => {
# 			:name => "start_date",
# 			:valueType => "DATE_AND_TIME",
# 			:units => "start_date",
# 			:applicableToDatafile => true,
# 			:facility => {:id => facility_id}
# 		}
# 	}
# ]).first


# write(datafile_ids.map{ |datafile_id|
# 	{
# 		:DatafileParameter => {
# 			:datafile => {:id => datafile_id},
# 			:type => {:id => parameter_type_id},
# 			:dateTimeValue => random_start_date
# 		}
# 	}
# })

