
require 'faker'
require 'rest-client'
require 'json'

$icat_url = "https://localhost:8181"
users_count = 100
instruments_count = 20
investigation_types_count = 20
dataset_types_count = 20
proposals_count = 20
investigations_per_proposal_count = 5
datasets_per_investigation_count = 3
co_investigators_per_investigation_count = 3
datafiles_per_dataset_count = 100

one_day_ago = (Time.now - (24 * 60 * 60)).strftime("%Y-%m-%dT%H:%M:%S.000Z")
seven_days_from_now = (Time.now + (7 * 24 * 60 * 60)).strftime("%Y-%m-%dT%H:%M:%S.000Z")


$session_id = JSON.parse(RestClient::Request.execute({
	:method => :post, 
	:url => "#{$icat_url}/icat/session",
    :payload => {
    	:json => {
			:plugin => "simple",
			:credentials => [
				{:username => "root"},
				{:password => "root"}
			]
		}.to_json
    },
	:verify_ssl => OpenSSL::SSL::VERIFY_NONE
}).body)['sessionId']

def write(entities)
	JSON.parse(RestClient::Request.execute({
		:method => :post, 
		:url => "#{$icat_url}/icat/entityManager",
	    :payload => {
	    	:sessionId => $session_id,
	    	:entities => entities.to_json
	    },
		:verify_ssl => OpenSSL::SSL::VERIFY_NONE
	}).body)
end

facility_id = write([{
	:Facility => {
		:fullName => "Lorum Ipsum Light Source",
		:name => "LILS"
	}
}]).first

user_ids = write(users_count.times.map do |i|
	{
		:User => {
			:fullName => Faker::Name.name,
			:name => "db/user#{i + 1}",
			:email => Faker::Internet.email
		}
	}
end)

instrument_ids = write(instruments_count.times.map do |i|
	{
		:Instrument => {
			:fullName => "Instrument #{i + 1}",
			:name => "I#{i + 1}",
			:facility => {:id => facility_id}
		}
	}
end)

investigation_type_ids = write(investigation_types_count.times.map do |i|
	{
		:InvestigationType => {
			:name => "InvestigationType #{i + 1}",
			:description => Faker::Lorem.words.join(' '),
			:facility => {:id => facility_id}
		}
	}
end)

dataset_type_ids = write(dataset_types_count.times.map do |i|
	{
		:DatasetType => {
			:name => "DatasetType #{i}",
			:description => Faker::Lorem.words.join(' '),
			:facility => {:id => facility_id}
		}
	}
end)

proposals_count.times do |i|
	name = "Proposal #{i + 1}"
	investigations_per_proposal_count.times do |j|
		investigation_type_id = investigation_type_ids[(rand * investigation_types_count).floor]
		instrument_id = instrument_ids[(rand * instruments_count).floor]
		
		investigation_user_ids = []
		while (investigation_user_ids.count < co_investigators_per_investigation_count + 1) && (investigation_user_ids.count <= users_count)
			user_id = user_ids[(rand * users_count).floor]
			investigation_user_ids << user_id if !investigation_user_ids.include?(user_id)
		end

		investigation_users = investigation_user_ids.map do |user_id|
			{
				:user => {:id => user_id},
				:role => "CO_INVESTIGATOR"
			}
		end

		investigation_users[0][:role] = "PRINCIPAL_INVESTIGATOR"

		investigation_id = write([{
			:Investigation => {
				:name => name,
				:visitId => "#{name} - #{j + 1}",
				:title => Faker::Lorem.words.join(' '),
				:startDate => one_day_ago,
				:endDate => seven_days_from_now,
				:facility => {:id => facility_id},
				:type => {:id => investigation_type_id},
				:investigationInstruments => [
					{:instrument => {:id => instrument_id}}
				],
				:investigationUsers => investigation_users
			}
		}]).first

		datasets_per_investigation_count.times.each do |k|
			dataset_type_id = dataset_type_ids[(rand * dataset_types_count).floor]

			dataset_id = write([{
				:Dataset => {
					:name => "Dataset #{k + 1}",
					:type => {:id => dataset_type_id},
					:investigation => {:id => investigation_id}
				}
			}]).first

			write(datafiles_per_dataset_count.times.map do |l|
				{
					:Datafile => {
						:name => "Datafile #{l + 1}",
						:description => Faker::Lorem.words.join(' '),
						:location => Faker::Lorem.words.join('/'),
						:fileSize => (rand * 1000000).floor,
						:dataset => {:id => dataset_id}
					}
				}
			end)

		end

	end
end
