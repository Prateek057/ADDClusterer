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
    var getPipeline = function (pipelineName) {
        return $http.get('/clustering/pipeline/' + pipelineName)
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
        getPipeline: getPipeline,
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
        .when('/clusterDocuments', {
            templateUrl: '/assets/components/classify/classify.html',
            controller: 'ClusterClassifyCtrl',
            controllerAs: 'vm',
            resolve: {
                pipelines: function (ClusterPipelineDataService) {
                    return ClusterPipelineDataService.getAllPipelines();
                }
            }
        })
        .when('/clustering/executePipeline/:pipelineName', {
            templateUrl: '/assets/components/cluster/executePipeline.html',
            controller: 'ExecutePipelineCtrl',
            controllerAs: 'vm3',
            resolve: {
                pipeline: function (ClusterPipelineDataService, $route) {
                    return ClusterPipelineDataService.getPipeline($route.current.params.pipelineName);
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
    self.onVisualize = function () {
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

classifyApp.controller('ClusterClassifyCtrl', ['pipelines', '$http', function (pipelines, $http) {
    var self = this;
    self.pipelines = pipelines;
}]);

classifyApp.controller('ExecutePipelineCtrl', ['scAuth', 'scData', 'scModel', 'pipeline', '$http', '$q', function (scAuth, scData, scModel, pipeline, $http, $q) {
    var self = this;
    self.pipeline = pipeline;
    self.showResults = false;
    self.textToClassify = "";
    self.isPredicting = false;
    self.sortType     = 'DOC_ID'; // set the default sort type
    self.sortReverse  = false;  // set the default sort order
    self.searchDocument   = '';     // set the default search/filter term

    self.clusterDocument = function () {

        if (self.textToClassify === "") {
            self.message = "Please provide the text to predict cluster label!"
        } else {
            $("#progress").css({
                "visibility": "visible"
            });
            self.isPredicting = true;
            var data = {};
            data.pipeline = self.pipeline;
            data.textToClassify = ""+self.textToClassify;
            $http.post('/clustering/pipeline/predict', data).then(function (response) {
                self.documents = response.data;
                self.showResults = true;
                self.isPredicting = false;
                $("#progress").css({
                    "visibility": "hidden"
                });
            });
        }
    };
}]);