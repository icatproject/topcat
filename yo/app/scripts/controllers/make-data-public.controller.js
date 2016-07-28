

(function(){
	'use strict';

    var app = angular.module('angularApp');

    app.controller('MakeDataPublicController', function($uibModalInstance, tc){
       
    	this.state = 'release_date';
    	this.isReleaseDate = false;
    	this.releaseDate = null;
    	this.isReleaseDateOpen = false;
    	this.licence = null;
    	this.acceptLegal = false;
    	this.dateFormat = 'yyyy-MM-dd';

    	var states = ['release_date', 'legal', 'confirmation'];

    	this.next = function(){
    		if(this.state == 'release_date' && (!this.isReleaseDate || this.releaseDate != null)){
    			this.isReleaseDateOpen = false;
    			this.state = 'legal';
	    	} else if(this.state == 'legal' && this.licence != null && this.acceptLegal){
	    		this.state = 'confirm';
	    	}
    	};

    	this.previous = function(){
    		if(this.state == 'confirm'){
    			this.state = 'legal';
    			this.acceptLegal = false;
    		} else if(this.state == 'legal'){
    			this.state = 'release_date';
    		}
    	};

    	this.confirm = function(){
    		
    	};

    	this.cancel = function() {
            $uibModalInstance.dismiss('cancel');
        };

    });

})();
