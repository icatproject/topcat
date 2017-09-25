
(function() {
    'use strict';

    var app = angular.module('topcat');

    app.controller('SearchController', function($sessionStorage, $state, $filter, $uibModal, tc){
        var that = this;
        this.text = $state.params.text || '';
        this.type = $state.params.type || '';
        var facilities = [];
        _.each(tc.userFacilities(), function(facility){
            facilities.push({
                name: facility.config().name,
                title: facility.config().title,
                selected: $state.params.facilities ? _.include(JSON.parse($state.params.facilities), facility.config().name) : true
            });
        });
        this.facilities = facilities;
        this.startDate = $state.params.startDate;
        this.endDate = $state.params.endDate;
        this.isStartDateOpen = false;
        this.isEndDateOpen = false;
        this.dateFormat = 'yyyy-MM-dd';
        this.parameters = $state.params.parameters ? JSON.parse($state.params.parameters) : [];
        this.samples = $state.params.samples ? JSON.parse($state.params.samples) : [];
        this.enableTextBox = tc.config().search.enableTextBox !== false;
        this.enableDateRange = tc.config().search.enableDateRange !== false;
        this.enableParameters = tc.config().search.enableParameters !== false;
        this.enableSamples = tc.config().search.enableSamples !== false;
        this.enableInvestigation = tc.config().search.enableInvestigation !== false;
        this.enableDataset = tc.config().search.enableDataset !== false;
        this.enableDatafile = tc.config().search.enableDatafile !== false;
        this.investigation = $state.params.investigation != 'false' && this.enableInvestigation;
        this.dataset = $state.params.dataset != 'false' && this.enableDataset;
        this.datafile = $state.params.datafile != 'false' && this.enableDatafile;
        this.entityCount = 0;
        if(this.enableInvestigation) this.entityCount++;
        if(this.enableDataset) this.entityCount++;
        if(this.enableDatafile) this.entityCount++;

        this.openStartDate = function(){
            this.isStartDateOpen = true;
            this.isEndDateOpen = false;
        };

        this.openEndDate = function(){
            this.isStartDateOpen = false;
            this.isEndDateOpen = true;
        };

        this.openParameterModal = function(){
            var modal = $uibModal.open({
                templateUrl : 'views/search-parameter.html',
                size : 'md',
                controller: 'SearchParameterController as searchParameterController'
            }).result.then(function(parameter){
                console.log('openParameterModal parameter', parameter)
                that.parameters.push(parameter);
            });
        };

        this.removeParameter = function(parameter){
            _.remove(that.parameters, parameter);
        };

        this.openSampleModal = function(){
            var modal = $uibModal.open({
                templateUrl : 'views/search-sample.html',
                size : 'sm',
                controller: 'SearchSampleController as searchSampleController'
            }).result.then(function(sample){
                that.samples.push(sample);
            });
        };

        this.removeSample = function(sample){
            _.remove(that.samples, function(i){ return i == sample; });
        };

        this.search = function(){
            var params = {
                text: null,
                type: this.type,
                startDate: null,
                endDate: null,
                facilities: null,
                parameters: null,
                samples: null,
                investigation: this.investigation,
                dataset: this.dataset,
                datafile: this.datafile
            };

            if(this.text !== '') params.text = this.text;
            if(this.startDate) params.startDate = $filter('date')(this.startDate, this.dateFormat);
            if(this.endDate) params.endDate = $filter('date')(this.endDate, this.dateFormat);

            var _facilities = _.select(facilities, function(facility){ return facility.selected; });
            _facilities = _.map(_facilities, function(facility){ return facility.name; });
            if(_facilities.length > 0) params.facilities = JSON.stringify(_facilities);
            if(this.parameters.length > 0) params.parameters = JSON.stringify(this.parameters);
            if(this.samples.length > 0) params.samples = JSON.stringify(this.samples);

            $state.go('home.search.results', params);
        };


    });


})();

