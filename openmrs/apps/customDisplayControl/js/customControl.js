'use strict';

angular.module('bahmni.common.displaycontrol.custom')
    .directive('birthCertificate', ['observationsService', 'appService', 'spinner', function (observationsService, appService, spinner) {
        var link = function ($scope) {
            console.log("inside birth certificate");
            var conceptNames = ["HEIGHT"];
            $scope.contentUrl = appService.configBaseUrl() + "/customDisplayControl/views/birthCertificate.html";
            spinner.forPromise(observationsService.fetch($scope.patient.uuid, conceptNames, "latest", undefined, $scope.visitUuid, undefined).then(function (response) {
                $scope.observations = response.data;
            }));
        };

        return {
            restrict: 'E',
            template: '<ng-include src="contentUrl"/>',
            link: link
        }
    }]).directive('referralForm', ['$q', 'observationsService', 'visitService', 'bedService', 'appService', 'spinner', '$sce', function ($q, observationsService, visitService, bedService, appService, spinner, $sce) {
        var link = function ($scope) {

            var conceptNames = ["Referral Form Template"];
            spinner.forPromise(observationsService.fetch($scope.patient.uuid, conceptNames, "latest", undefined, $scope.visitUuid, undefined).then(function (response) {
                $scope.observations = response.data[0];
                $scope.referralForm = [];
                function createForm(obs) {
                    if (obs.groupMembers.length == 0) {
                        if ($scope.referralForm[obs.conceptNameToDisplay] == undefined) {
                            $scope.referralForm[obs.conceptNameToDisplay] = obs.valueAsString;
                        }
                        else {
                            $scope.referralForm[obs.conceptNameToDisplay] = $scope.referralForm[obs.conceptNameToDisplay] + ' ' + obs.valueAsString;
                        }

                        if (obs.comment != null) {
                            $scope.referralForm[obs.conceptNameToDisplay] = $scope.referralForm[obs.conceptNameToDisplay] + ' ' + obs.comment;
                        }
                    }
                    else {
                        for (var i = 0; i < obs.groupMembers.length; i++) {
                            createForm(obs.groupMembers[i]);
                        }

                    }

                }
                createForm(response.data[0]);

            }));
            $scope.contentUrl = appService.configBaseUrl() + "/customDisplayControl/views/referralform.html";


        };
        var controller = function ($scope) {
            $scope.htmlLabel = function (label) {
                return $sce.trustAsHtml(label)
            }
            $scope.date = new Date();
        }
        return {
            restrict: 'E',
            link: link,
            controller: controller,
            template: '<ng-include src="contentUrl"/>'
        }
    }]).directive('deathCertificate', ['observationsService', 'appService', 'spinner', function (observationsService, appService, spinner) {
        var link = function ($scope) {
            var conceptNames = ["WEIGHT"];
            $scope.contentUrl = appService.configBaseUrl() + "/customDisplayControl/views/deathCertificate.html";
            spinner.forPromise(observationsService.fetch($scope.patient.uuid, conceptNames, "latest", undefined, $scope.visitUuid, undefined).then(function (response) {
                $scope.observations = response.data;
            }));
        };

        return {
            restrict: 'E',
            link: link,
            template: '<ng-include src="contentUrl"/>'
        }
    }]);