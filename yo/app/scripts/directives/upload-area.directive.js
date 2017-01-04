

(function() {
    'use strict';

    var app = angular.module('topcat');

    app.directive('uploadArea', function(){
        return {
            restrict: 'A',
            templateUrl: 'views/upload-area.html',
            controller: "UploadAreaController as uploadAreaController",
            scope: {
                files: '=ngModel'
            }
        };
    });

    app.controller('UploadAreaController', function($scope, $element){
        $($element).addClass('upload-area');

        $($element).on('dragenter', function(e){
            $(this).addClass('enter');
            e.preventDefault();
            e.stopPropagation();
        });

        $($element).on('dragleave', function(e){
            $(this).removeClass('enter');
            e.preventDefault();
            e.stopPropagation();
        });

        $($element).on('dragover', function(e){
            e.preventDefault();
            e.stopPropagation();
        });

        $($element).on("drop", function(e){
            $(this).removeClass('enter');
            _.each(e.originalEvent.dataTransfer.files, function(file){
                var reader = new FileReader();

                reader.onload = function(e) {
                    $scope.files.push({
                        name: file.name,
                        size: file.size,
                        data: new Uint8Array(reader.result)
                    });
                };

                reader.readAsArrayBuffer(file);
            });
            e.preventDefault();
            e.stopPropagation();    
        });

        this.deleteFile = function(file){
            _.pull($scope.files, file);
        };

    });

})();

