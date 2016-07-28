

(function(){
	'use strict';

    var app = angular.module('angularApp');

    app.controller('MakeDataPublicController', function($uibModalInstance, tc){
       
    	this.state = 'release_date';
    	this.isReleaseDate = false;
    	this.releaseDate = null;
    	this.isReleaseDateOpen = false;
    	this.licence = null;
    	this.hasAcceptLegal = false;
    	this.dateFormat = 'yyyy-MM-dd';

    	var states = ['release_date', 'legal', 'confirmation'];

    	this.isPreviousDisabled = function(){
    		return this.state == 'release_date';
    	};

    	this.previous = function(){
    		if(this.state == 'confirm'){
    			this.state = 'legal';
    			this.hasAcceptLegal = false;
    		} else if(this.state == 'legal'){
    			this.state = 'release_date';
    		}
    	};

    	this.isNextDisabled = function(){
    		if(this.state == 'release_date' && (!this.isReleaseDate || this.releaseDate != null)){
    			return false;
    		}

    		if(this.state == 'legal' && this.acceptedLegal){
    			return false;
    		}

    		return true;
    	};

    	this.next = function(){
    		this.nextEnabled = false;

    		if(this.state == 'release_date' && (!this.isReleaseDate || this.releaseDate != null)){
    			this.isReleaseDateOpen = false;
    			this.state = 'legal';
	    	} else if(this.state == 'legal' && this.licence != null && this.hasAcceptLegal){
	    		this.state = 'confirm';
	    	}
    	};

    	this.confirm = function(){

    	};

    	this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });

})();
