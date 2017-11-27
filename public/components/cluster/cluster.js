'use strict';

var clusterPipelineApp = angular.module('cApp.cluster', ['ngRoute']);
clusterPipelineApp.factory('ClusterPipelineDataService', ['$http', function PipelineDataService($http) {
    var getPipelineResults = function () {
        return $http.get('/clustering/results')
            .then(function (response) {
                return response.data;
            });
    };
    var getPipelineModels = function () {
        return $http.get('/clustering/models')
            .then(function (response) {
                return response.data;
            });
    };
    var getPipelineClusters = function (name) {
        return $http.get('/pipeline/clusters/' + name)
            .then(function (response) {
                return response.data;
            });
    };
    var getAllPipelines = function () {
        return $http.get('/clustering/pipelines/getAll')
            .then(function (response) {
                return response.data.pipelines;
            })
    };
    return {
        getPipelineResults: getPipelineResults,
        getPipelineClusters: getPipelineClusters,
        getPipelineModels: getPipelineModels,
        getAllPipelines: getAllPipelines
    };
}]);
clusterPipelineApp.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/visualize', {
            templateUrl: '/assets/components/cluster/visualize.html',
            controller: 'VisualizePipelineCtrl',
            controllerAs: 'vm2',
            resolve: {
                pipelines: function (ClusterPipelineDataService) {
                    return ClusterPipelineDataService.getAllPipelines();
                }
            }
        })
        .when('/clustering/clusters/:pipelineName', {
            templateUrl: '/assets/components/cluster/table.html',
            controller: 'VisualizePipelineClustersCtrl',
            controllerAs: 'vm1',
            resolve: {
                clusters: function (ClusterPipelineDataService, $route) {
                    return ClusterPipelineDataService.getPipelineClusters($route.current.params.pipelineName);
                }
            }
        });
}]);

clusterPipelineApp.controller('VisualizePipelineCtrl', ['pipelines', '$http', '$location', function (pipelines, $http, $location) {
    var self = this;
    console.log(pipelines);
    self.pipelines = pipelines;
    self.onVisualize = function(){
        console.log("called");
        $("#progress").css({
            "visibility": "visible"
        });
        Materialize.toast('You will be redirect to visualization page', 5000); // 4000 is the duration of the toast
    }
}]);

clusterPipelineApp.controller('VisualizePipelineClustersCtrl', ['clusters', '$http', '$location', function (clusters, $http, $location) {
    var self = this;
    self.clusters = clusters.cluster_table;
    self.member_count = clusters.member_count;
}]);