'use strict';

var AddParameterModalController = ['$scope', '$modal', '$log', function($scope, $modal, $log) {
            // open a model window
            $scope.open = function(type, size, formData) {
                //var modalInstance = null;

                //determine which modal form to display
                //switch(type) {
                //    case "parameter":
                        var modalInstance = $modal.open({
                            templateUrl : 'views/search/modal/add-parameter-modal.html',
                            controller : 'ModalInstanceCtrl',
                            size : size,
                            resolve : {
                                formData: function () {
                                    var params = $scope.parameters;
                                    //clear params
                                    $scope.parameters = {};

                                    return params;
                                }
                            }
                        });

                //       break;
                //}

                modalInstance.result.then(function(formParam) {
                    $log.info(formParam);

                    //$scope.formData = formData;

                    //var params = [];

                    //params.push(formData);

                    //$log.info(params);

                    $scope.formData.push(formParam);
                }, function() {
                    $log.info('Modal dismissed at: ' + new Date());
                });
            };
        } ];

angular.module('angularApp').controller('AddParameterModalController', AddParameterModalController);

angular.module('angularApp').controller('ModalInstanceCtrl',
        function($scope, $modalInstance, $log, formData) {
            //modal

            $scope.parameterTypes = [
                 {"name" : "Text", "id" : "text"},
                 {"name" : "Number", "id" : "number"},
                 {"name" : "Date", "id" : "date"},
            ];

            $scope.ok = function() {
                $modalInstance.close($scope.formParam);
            };

            $scope.cancel = function() {
                $modalInstance.dismiss('cancel');
            };
        });
