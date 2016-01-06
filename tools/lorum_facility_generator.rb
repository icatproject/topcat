
require 'mysql2'
require 'date'
require 'faker'
require 'json'

client = Mysql2::Client.new({
	:host => "127.0.0.1",
	:port => 13306,
	:username => "root",
	:password => "secret",
	:database => "icat"
})

tables = {
	'DATAFILE' => {
		'ID' => Integer,
		'CHECKSUM' => String,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DATAFILECREATETIME' => DateTime,
		'DATAFILEMODTIME' => DateTime,
		'DESCRIPTION' => String,
		'DOI' => String,
		'FILESIZE' => Integer,
		'LOCATION' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'DATAFILEFORMAT_ID' => Integer,
		'DATASET_ID' => Integer
	},
	'DATAFILEFORMAT' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DESCRIPTION' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'TYPE' => String,
		'VERSION' => String,
		'FACILITY_ID' => Integer
	},
	'DATAFILEPARAMETER' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DATETIME_VALUE' => DateTime,
		'ERROR' => Numeric,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NUMERIC_VALUE' => Numeric,
		'RANGEBOTTOM' => Numeric,
		'RANGETOP' => Numeric,
		'STRING_VALUE' => String,
		'DATAFILE_ID' => Integer,
		'PARAMETER_TYPE_ID' => Integer
	},
	'DATASET' => {
		'ID' => Integer,
		'COMPLETE' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DESCRIPTION' => String,
		'DOI' => String,
		'END_DATE' => DateTime,
		'LOCATION' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'STARTDATE' => DateTime,
		'INVESTIGATION_ID' => Integer,
		'SAMPLE_ID' => Integer,
		'TYPE_ID' => Integer
	},
	'DATASETPARAMETER' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DATETIME_VALUE' => DateTime,
		'ERROR' => Numeric,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NUMERIC_VALUE' => Numeric,
		'RANGEBOTTOM' => Numeric,
		'RANGETOP' => Numeric,
		'STRING_VALUE' => String,
		'DATASET_ID' => Integer,
		'PARAMETER_TYPE_ID' => Integer
	},
	'DATASETTYPE' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DESCRIPTION' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'FACILITY_ID' => Integer
	},
	'FACILITY' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DAYSUNTILRELEASE' => Integer,
		'DESCRIPTION' => String,
		'FULLNAME' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'URL' => String
	},
	'FACILITYCYCLE' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DESCRIPTION' => String,
		'ENDDATE' => DateTime,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'STARTDATE' => DateTime,
		'FACILITY_ID' => Integer
	},
	'INSTRUMENT' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DESCRIPTION' => String,
		'FULLNAME' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'TYPE' => String,
		'URL' => String,
		'FACILITY_ID' => Integer
	},
	'INVESTIGATION' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DOI' => String,
		'ENDDATE' => DateTime,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'RELEASEDATE' => DateTime,
		'STARTDATE' => DateTime,
		'SUMMARY' => String,
		'TITLE' => String,
		'VISIT_ID' => String,
		'FACILITY_ID' => Integer,
		'TYPE_ID' => Integer
	},
	'INVESTIGATIONINSTRUMENT' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'INSTRUMENT_ID' => Integer,
		'INVESTIGATION_ID' => Integer
	},
	'INVESTIGATIONPARAMETER' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DATETIME_VALUE' => DateTime,
		'ERROR' => Numeric,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NUMERIC_VALUE' => Numeric,
		'RANGEBOTTOM' => Numeric,
		'RANGETOP' => Numeric,
		'STRING_VALUE' => String,
		'INVESTIGATION_ID' => Integer,
		'PARAMETER_TYPE_ID' => Integer
	},
	'INVESTIGATIONTYPE' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DESCRIPTION' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'FACILITY_ID' => Integer
	},
	'INVESTIGATIONUSER' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'ROLE' => String,
		'INVESTIGATION_ID' => Integer,
		'USER_ID' => Integer
	},
	'PARAMETERTYPE' => {
		'ID' => Integer,
		'APPLICABLETODATACOLLECTION' => Integer,
		'APPLICABLETODATAFILE' => Integer,
		'APPLICABLETODATASET' => Integer,
		'APPLICABLETOINVESTIGATION' => Integer,
		'APPLICABLETOSAMPLE' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'DESCRIPTION' => String,
		'ENFORCED' => Integer,
		'MAXIMUMNUMERICVALUE' => Numeric,
		'MINIMUMNUMERICVALUE' => Numeric,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'UNITS' => String,
		'UNITSFULLNAME' => String,
		'VALUETYPE' => Integer,
		'VERIFIED' => Integer,
		'FACILITY_ID' => Integer
	},
	'SAMPLE' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String,
		'INVESTIGATION_ID' => Integer,
		'SAMPLETYPE_ID' => Integer
	},
	'SAMPLETYPE' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'MOLECULARFORMULA' => String,
		'NAME' => String,
		'SAFETYINFORMATION' => String,
		'FACILITY_ID' => Integer
	},
	'USER_' => {
		'ID' => Integer,
		'CREATE_ID' => String,
		'CREATE_TIME' => DateTime,
		'FULLNAME' => String,
		'MOD_ID' => String,
		'MOD_TIME' => DateTime,
		'NAME' => String
	}
}


class Entity

	class << self

		attr_reader :client, :table, :schema

		def all
			client.query("select * from #{table}").map{|row| new(row)}
		end

		def normalize_hash(hash)
			out = {}
			hash.each do |k,v|
				out[k.to_s] = v
			end
			out
		end

		def create_fake(specific_attributes = {})
			attributes = {}
			schema.each do |name, type|
				name = name.downcase
				next if name.match(/(\A|_)id\z/) && type == Integer
				if name == 'url'
					value = Faker::Internet.url
				elsif name.match(/_id\z/)
					value = "#{Faker::Lorem.word}/#{Faker::Lorem.word}"
				else
					if type == String
						value = Faker::Lorem.words.join(' ')
					elsif type == Integer
						value = Faker::Number.between(1, 10)
					elsif type == Numeric
						value = Faker::Number.decimal(0, 2)
					elsif type == DateTime
						from = DateTime.parse('2012-01-01').to_time.to_i
						to = Time.now.to_i
						range = to - from
						value = Time.at(from + (rand * range)).to_datetime
					end
				end
				attributes[name] = value
			end
			new(attributes.merge(normalize_hash(specific_attributes)))
		end

	end

	def initialize(attributes = {})
		@attributes = {}
		self.class.normalize_hash(attributes).each{ |k, v| @attributes[k.downcase] = v}
	end

	def method_missing(name, *args)
		name = name.to_s
		if matches = name.match(/\A(\w+)=\z/)
			@attributes[matches[1]] = args.first
		else
			@attributes[name]
		end
	end

	def sanitize(value)
		client = self.class.client
		if value.kind_of?(Numeric)
			value
		elsif value.kind_of?(Date) || value.kind_of?(Time)
			"'#{value.strftime("%Y-%m-%d %H:%M:%S")}'"
		else
			"'#{client.escape(value.to_s)}'"
		end
	end

	def to_s
		JSON.generate(@attributes)
	end

	def save
		client = self.class.client
		table = self.class.table
		if id.nil?
			client.query("insert into #{table}(#{@attributes.keys.map{|k| k.upcase}.join(', ')}) values(#{@attributes.values.map{|v| sanitize(v)}.join(', ')})")
			self.id = client.last_id
		else
			client.query("update #{table} set #{@attributes.map{|a| "#{a[0].upcase} = #{sanitize(a[1])}"}.join(', ')} where ID = '#{id}'")
		end
		self
	end

end

entity_map = {
	'Datafile' => "DATAFILE",
	'DatafileFormat' => 'DATAFILEFORMAT',
	'DatafileParameter' => 'DATAFILEPARAMETER',
	'Dataset' => 'DATASET',
	'DatasetType' => 'DATASETTYPE',
	'Facility' => 'FACILITY',
	'FacilityCycle' => 'FACILITYCYCLE',
	'Instrument' => 'INSTRUMENT',
	'Investigation' => 'INVESTIGATION',
	'InvestigationInstrument' => 'INVESTIGATIONINSTRUMENT',
	'InvestigationParameter' => 'INVESTIGATIONPARAMETER',
	'InvestigationType' => 'INVESTIGATIONTYPE',
	'InvestigationUser' => 'INVESTIGATIONUSER',
	'ParameterType' => 'PARAMETERTYPE',
	'Sample' => 'SAMPLE',
	'SampleType' => 'SAMPLETYPE',
	'User' => 'USER_'
}


entity_map.each do |name, table|
	_class = Class.new(Entity)
	_class.instance_eval do
		@client = client
		@table = table
		schema = {}
		tables[table].each{|k, v| schema[k.downcase] = v}
		@schema = schema
	end
	Object.const_set(name, _class)
end


facility = Facility.create_fake.save

facility_cycles = []
(2012..2015).each do |year|
	(1..12).each do |month|
		startdate = DateTime.parse("#{year}-#{month}-01").to_time.to_i
		enddate = startdate + (2 * 7 * 24 * 60 * 60)
		startdate = Time.at(startdate).to_datetime
		enddate = Time.at(enddate).to_datetime
		facility_cycles << FacilityCycle.create_fake({
			:facility_id => facility.id,
			:startdate => startdate,
			:enddate => enddate
		}).save
	end
end

instruments = []
17.times do
	instruments << Instrument.create_fake(:facility_id => facility.id).save
end

investigation_types = []
17.times do
	investigation_types << InvestigationType.create_fake(:facility_id => facility.id).save
end

dataset_types = []
17.times do
	dataset_types << DatasetType.create_fake(:facility_id => facility.id).save
end

sample_types = []
17.times do
	sample_types << SampleType.create_fake(:facility_id => facility.id).save
end

datafile_formats = []
7.times do
	datafile_formats << DatafileFormat.create_fake(:facility_id => facility.id).save
end

visit_id_counter = 1

7.times do
	instrument = Instrument.create_fake(:facility_id => facility.id).save
	7.times do
		proposal = "#{Faker::Lorem.word} #{facility.id}"
		7.times do
			type_id = investigation_types[(rand * investigation_types.count).to_i  - 1].id
			visit_id = visit_id_counter
			visit_id_counter += 1
			facility_cycle = facility_cycles[(rand * facility_cycles.count).to_i  - 1]

			startdate = facility_cycle.startdate.to_time.to_i
			enddate = facility_cycle.enddate.to_time.to_i
			startdate = Time.at(startdate + (rand * (enddate - startdate)).to_i).to_datetime


			investigation = Investigation.create_fake({
				:facility_id => facility.id,
				:name => proposal,
				:type_id => type_id,
				:visit_id => visit_id,
				:startdate => startdate
			}).save

			instrument_ids = instruments.map{|instrument| instrument.id}

			(1..3).each do
				instrument_ids.shuffle!
				instrument_id = instrument_ids.pop
				InvestigationInstrument.create_fake(:investigation_id => investigation.id, :instrument_id => instrument_id).save
			end


			samples = []
			7.times do
				type_id = sample_types[(rand * sample_types.count).to_i  - 1].id
				samples << Sample.create_fake(:investigation_id => investigation.id, :sampletype_id => type_id).save
			end

			dataset_name_counter = 1

			7.times do
				type_id = dataset_types[(rand * dataset_types.count).to_i  - 1].id
				sample_id = samples[(rand * samples.count).to_i  - 1].id
				name = "#{Faker::Lorem.word} #{dataset_name_counter}"
				dataset_name_counter += 1

				dataset = Dataset.create_fake({
					:investigation_id => investigation.id,
					:type_id => type_id,
					:sample_id => sample_id,
					:name => name
				}).save

				datafile_name_counter = 1

				17.times do
					datafileformat_id = datafile_formats[(rand * datafile_formats.count).to_i  - 1].id
					name = "#{Faker::Lorem.word} #{datafile_name_counter}"
					datafile_name_counter += 1
					datafile = Datafile.create_fake({
						:dataset_id => dataset.id, 
						:datafileformat_id => datafileformat_id,
						:name => name
					}).save
				end

			end

		end
	end
end


