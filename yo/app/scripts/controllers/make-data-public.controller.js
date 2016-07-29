

(function(){
	'use strict';

    var app = angular.module('angularApp');

    app.controller('MakeDataPublicController', function($uibModalInstance, tc, inform){
        var that = this;
    	this.state = 'release_date';
    	this.isReleaseDate = false;
    	this.releaseDate = null;
    	this.isReleaseDateOpen = false;
    	this.licence = null;
    	this.hasAcceptedLegal = false;
    	this.dateFormat = 'yyyy-MM-dd';
    	this.licences = [
    		{
    			name: 'mit',
    			title: 'MIT'
    		},
    		{
    			name: 'cc',
    			title: 'Creative Commons'
    		}
    	];
        this.termsAndConditions = "line 1\n line 2\nline 3\nline 4\nline 5\nline 6\nline 7\nline 8\nline 9\n"
    	this.users = _.map(tc.userFacilities(), function(facility){
            var session = facility.icat().session();

            return {
                name: (session.fullName ? session.fullName : session.username) + " (" + facility.config().title + ")",
                facilityName: facility.config().name
            };
        });

        this.facilityName = this.users[0].facilityName;
        this.password = "";

    	this.isPreviousDisabled = function(){
    		return this.state == 'release_date';
    	};

    	this.previous = function(){
    		if(this.state == 'confirm'){
    			this.state = 'legal';
    			this.password = '';
    		} else if(this.state == 'legal'){
    			this.state = 'release_date';
    		}
    	};

    	this.isNextDisabled = function(){
    		if(this.state == 'release_date' && (!this.isReleaseDate || this.releaseDate != null)){
    			return false;
    		}

    		if(this.state == 'legal' && this.licence != null && this.hasAcceptedLegal){
    			return false;
    		}

    		return true;
    	};

    	this.next = function(){
    		this.nextEnabled = false;

    		if(this.state == 'release_date' && (!this.isReleaseDate || this.releaseDate != null)){
    			this.isReleaseDateOpen = false;
    			this.state = 'legal';
	    	} else if(this.state == 'legal' && this.licence != null && this.hasAcceptedLegal){
	    		this.state = 'confirm';
	    	}
    	};

    	this.isConfirmDisabled = function(){
    		return this.password == '';
    	};

    	this.confirm = function(){
            tc.icat(this.facilityName).verifyPassword(this.password).then(function(isValid){
                if(isValid){

                } else {
                    that.password = "";
                    inform.add("Password is invalid - please try again", {
                        'ttl': 1500,
                        'type': 'danger'
                    });
                }
            });
    	};

    	this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });

})();
