
require 'rest_client'
require 'json'

sessionId = JSON.parse(RestClient.post 'https://localhost:8181/icat/session/', :json => JSON.generate({
	:plugin => "simple",
	:credentials => [
		{:username => "root"},
		{:password => "root"}
	]
}))['sessionId']


['Investigation', 'Dataset', 'Datafile'].each do |entityName|
	RestClient.post "https://localhost:8181/icat/lucene/db/#{entityName}/", :sessionId => sessionId
end
