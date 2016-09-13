
require 'faker'
require 'rest-client'
require 'json'
require 'date'

$icat_url = "https://localhost:8181"
instrument_count = 20
investigation_types_count = 20
dataset_types_count = 20
proposals_count = 20
investigations_per_proposal_count = 5
datasets_per_investigation_count = 3
datafiles_per_dataset_count = 100


$sessionId = JSON.parse(RestClient::Request.execute({
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
	    	:sessionId => $sessionId,
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

instrument_ids = write(instrument_count.times.map do |i|
	{
		:Instrument => {
			:fullName => "Instrument #{i}",
			:name => "I#{i}",
			:facility => {:id => facility_id}
		}
	}
end)

investigation_type_ids = write(investigation_types_count.times.map do |i|
	{
		:InvestigationType => {
			:name => "InvestigationType #{i}",
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
	name = "proposal #{i}"
	investigations_per_proposal_count.times do |j|
		investigation_type_id = investigation_type_ids[(rand * investigation_types_count).floor]

		investigation_id = write([{
			:Investigation => {
				:name => name,
				:visitId => "#{name} - #{j}",
				:title => Faker::Lorem.words.join(' '),
				:startDate => (Time.now - (24 * 60 * 60)).strftime("%Y-%m-%dT%H:%M:%S.000Z"),
				:facility => {:id => facility_id},
				:type => {:id => investigation_type_id}
			}
		}]).first

		datasets_per_investigation_count.times.each do |k|
			dataset_type_id = dataset_type_ids[(rand * dataset_types_count).floor]

			dataset_id = write([{
				:Dataset => {
					:name => "Dataset #{k}",
					:type => {:id => dataset_type_id},
					:investigation => {:id => investigation_id}
				}
			}]).first

			write(datafiles_per_dataset_count.times.map do |l|
				{
					:Datafile => {
						:name => "Datafile #{l}",
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
