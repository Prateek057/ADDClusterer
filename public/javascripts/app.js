'use strict';
$(document).ready(function() {
    console.log("Select initializer called");
    $('select').material_select();
});
angular.module('cApp', [
    'sociocortex',
    'ngRoute',
    'ngStorage',
    'ui.bootstrap',
    'cApp.pipeline',
    'cApp.classify',
    'cApp.cluster',
    'checklist-model'
])
.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider
        .when('/', {
            templateUrl: '/assets/components/home.html'
        });
        // Enable html5Mode in order to disable hashbanging
        $locationProvider.html5Mode({
            enabled: true,
            requireBase: false
        });
}]);