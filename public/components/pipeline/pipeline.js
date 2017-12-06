'use strict';

var pipelineApp = angular.module('cApp.pipeline', ['ngRoute']);

pipelineApp.factory('PipelineDataService', ['$http', function PipelineDataService($http) {
    var getPipelines = function () {
        return $http.get('/pipeline/getAll')
            .then(function (response) {
                return response.data;
            });
    };
    var getPipeline = function (name) {
        return $http.get('/pipeline/get?name=' + name)
            .then(function (response) {
                return response.data;
            });
    };
    var getClassifiers = function () {
        return $http.get('/classifiers')
            .then(function (response) {
                return response.data;
            });
    };
    var getLibraries = function () {
        return $http.get('/clustering/libraries')
            .then(function (response) {
                return response.data.libraries;
            })
    };
    return {
        getPipelines: getPipelines,
        getPipeline: getPipeline,
        getClassifiers: getClassifiers,
        getLibraries: getLibraries
    };
}]);

pipelineApp.config(['$routeProvider', function ($routeProvider) {
    $routeProvider
        .when('/pipelines', {
            templateUrl: '/assets/components/pipeline/pipeline.html',
            controller: 'PipelineCtrl',
            controllerAs: 'vm',
            resolve: {
                pipelines: function (PipelineDataService) {
                    return PipelineDataService.getPipelines();
                },
                libraries: function (PipelineDataService) {
                    return PipelineDataService.getLibraries();
                }
            }
        })
        .when('/pipelines/run', {
            templateUrl: '/assets/components/pipeline/cluster-pipeline.html',
            controller: 'RunPipelineCtrl',
            controllerAs: 'vm',
            resolve: {
                pipelines: function (PipelineDataService) {
                    return PipelineDataService.getPipelines();
                }
            }
        })
        .when('/cluster/pipelines', {
            templateUrl: '/assets/components/pipeline/cluster-pipeline-create.html',
            controller: 'ClusterPipelineCtrl',
            controllerAs: 'vm1',
            resolve: {
                pipelines: function (PipelineDataService) {
                    return PipelineDataService.getPipelines();
                },
                libraries: function (PipelineDataService) {
                    return PipelineDataService.getLibraries();
                }
            }
        })
        .when('/configurePipeline/:param', {
            templateUrl: '/assets/components/pipeline/configurePipeline.html',
            controller: 'ConfigurePipelineCtrl',
            controllerAs: 'vm',
            resolve: {
                pipeline: function (PipelineDataService, $route) {
                    return PipelineDataService.getPipeline($route.current.params.param);
                }, classifiers: function (PipelineDataService) {
                    return PipelineDataService.getClassifiers();
                }
            }
        });
}]);

pipelineApp.controller('RunPipelineCtrl', ['$http', '$location', function (pipelines, $http) {
    var self = this;
    self.pipelines = pipelines;
}]);

pipelineApp.controller('ClusterPipelineCtrl', ['scAuth', 'scData', 'scModel', 'pipelines', 'libraries', '$http', '$location', function (scAuth, scData, scModel, pipelines, libraries, $http, $location) {
    var self = this;

    self.pipelines = pipelines;
    self.libraries = libraries;
    self.showRest = false;
    self.transformers = undefined;
    self.algorithms = undefined;
    self.workspace = undefined;
    self.selectedType = undefined;

    self.onLibrarySelect = function (id) {
        self.selectedLibrary = self.libraries[id - 1];
        self.selectedAlgorithm = undefined;
        self.algoOptions = undefined;
        self.showRest = true;
    };
    var AnimateRotate = function (angle, repeat) {
        var duration = 1000;
        self.rotateNumber = setTimeout(function () {
            if (repeat && repeat === "infinite") {
                AnimateRotate(angle, repeat);
            } else if (repeat && repeat > 1) {
                AnimateRotate(angle, repeat - 1);
            }
        }, duration);
        var $elem = $('#upload-icon');

        $({deg: 0}).animate({deg: angle}, {
            duration: duration,
            step: function (now) {
                $elem.css({
                    'transform': 'rotate(' + now + 'deg)'
                });
            }
        });
    };

    self.onSelectTransformer = function (id) {
        self.transformer = {
            id: id
        };
    };

    self.onSelectAlgorithm = function (id) {
        self.algorithm = self.selectedLibrary.options.algorithms.filter(function (algorithm) {
            return algorithm.id === id;
        })[0];
        self.algoOptions = self.algorithm.options !== undefined ? self.algorithm.options : undefined;
    };

    self.onAlgoOptionsChange = function (algoOptionName) {
        self.algoOptions.filter(function (algoOptions) {
            return algoOptions.name === algoOptionName;
        })[0].set("value", $('#' + algoOptionName).value());
        self.algorithm.options = self.algoOptions;
    };

    self.onFileChange = function (ele) {
        var files = ele.files;
        self.file = files[0];
    };
    self.uploadDataSetFile = function () {
        AnimateRotate(360, "infinite");
        var fd = new FormData();
        fd.append("file", self.file);
        $http.post("/clustering/dataset/upload", fd, {
            headers: {'Content-Type': undefined}
        }).then(function (response) {
            clearTimeout(self.rotateNumber);
            Materialize.toast('File Uploaded!', 5000, "bottom");
            self.dataset = response.data.results.path;
        });
    };

    self.linkSC = function () {
        scData.Workspace.query(function (workspaces) {
            self.workspaces = workspaces;
        });
    };

    self.getPages = function () {
        self.pages = [];
        self.attributeType = "Page";
        scData.Workspace.get({id: self.workspace}, function (workspace) {
            scData.Entity.get({id: workspace.rootEntity.id}, function (entity) {
                entity.children.forEach(function (subpage) {
                    if (subpage.name.indexOf(".") < 0) {
                        self.pages.push(subpage);
                    }
                });
            });
        });
        self.types = [];
        scData.Workspace.getEntityTypes({id: self.workspace}, function (types) {
            self.types = types;
        });
    };

    self.getAttributes = function (type) {
        self.attributes = [];
        self.selectedType = type;
        scModel.EntityType.getAttributeDefinitions({id: type.id}, function (attributes) {
            self.attributes = attributes;
        });
    };


    self.updateSelection = function (position, entities) {
        angular.forEach(entities, function (subscription, index) {
            if (position !== index)
                subscription.checked = false;
        });
    };

    self.resetSCLink = function () {
        self.selectedAttributesForMining = undefined;
        self.selectedType = undefined;
        self.typeCheckBox = undefined;
        self.types = undefined;
        self.workspaces = undefined;
        self.workspace = undefined;
        self.scFileName = undefined;
    };

    self.createClusteringPipeline = function () {
        var request_url = "";
        var pipelineName = self.pipelineName;
        switch (self.selectedLibrary.id) {
            case 1:
                request_url = "/spark/train/pipeline/" + pipelineName;
                break;
            case 2:
                request_url = "/weka/train/pipeline/" + pipelineName;
                break;
        }
        Materialize.toast('Creating Pipeline, Please Wait!', 5000);
        $("#progress").css({
            "visibility": "visible"
        });
        if (self.scFileName) {
            self.dataset = self.scFileName;
        }
        self.algorithm.options = self.algoOptions;
        var miningAttr = [];
        if (self.selectedAttributesForMining)
            self.selectedAttributesForMining.forEach(function (attr) {
                miningAttr.push(attr.name);
            });

        var data = {
            pipeline: {
                href: request_url,
                name: pipelineName,
                library: {
                    name: self.selectedLibrary.name,
                    id: self.selectedLibrary.id
                },
                scLink: !!self.selectedType,
                scData: {
                    filename: self.scFileName,
                    type: self.selectedType,
                    miningAttributes: miningAttr
                },
                dataset: self.dataset,
                algorithm: self.algorithm,
                preprocessors: self.preprocessors,
                transformer: self.transformer
            }
        };
        $http.post("/clustering/pipeline/create", data)
            .then(function (response) {
                Materialize.toast('Pipeline Create!', 4000);
                $("#progress").css({
                    "visibility": "hidden"
                });
                Materialize.toast('You will be redirect to visualize results', 5000); // 4000 is the duration of the toast
                if (response.data.results) {
                    $location.path("/clustering/clusters/" + pipelineName);
                }
            });
    };
}]);


pipelineApp.controller('PipelineCtrl', ['pipelines', 'libraries', '$http', '$location', function (pipelines, libraries, $http, $location) {
    var self = this;
    self.pipelines = JSON.parse(pipelines.result);
    self.libraries = libraries;
    console.log(self.pipelines);
    self.createClusteringPipeline = function () {
        console.log("...Hello.....");
        if (self.pipelineName === undefined || self.pipelineName.length === 0) {
            self.message = "Please provide a name!"
        } else {
            for (var i in self.pipelines) {
                if (self.pipelines[i].name === self.pipelineName) {
                    self.message = "Please use a different name! Pipeline already exists";
                    return;
                }
            }

            //create a new pipeline
            $http.get('/pipeline/create?name=' + self.pipelineName).then(function (response) {
                self.pipelines = JSON.parse(response.data.result);
            });
        }
    };

    self.createPipeline = function () {
        if (self.pipelineName === undefined || self.pipelineName.length === 0) {
            self.message = "Please provide a name!"
        }
        else if (self.dataFile) {

        }
        else {
            for (var i in self.pipelines) {
                if (self.pipelines[i].name === self.pipelineName) {
                    self.message = "Please use a different name! Pipeline already exists";
                    return;
                }
            }

            //create a new pipeline
            $http.get('/pipeline/create?name=' + self.pipelineName).then(function (response) {
                self.pipelines = JSON.parse(response.data.result);
            });
        }
    };

    self.removePipeline = function (pipelineName) {
        //remove a pipeline
        $http.get('/pipeline/remove?name=' + pipelineName).then(function (response) {
            self.pipelines = JSON.parse(response.data.result);
        });
    }

}]);

pipelineApp.controller('ConfigurePipelineCtrl', ['scAuth', 'scData', 'scModel', 'pipeline', 'classifiers', '$http', function (scAuth, scData, scModel, pipeline, classifiers, $http) {
    var self = this;
    self.pipeline = JSON.parse(pipeline.result);
    self.classifiers = JSON.parse(classifiers.result);
    self.createLabelFlag = false;
    self.workspace = "";
    self.classifier = "";
    self.isTraining = false;

    self.typeCheckBox = null;
    self.pageCheckBox = null;

    self.newLabel = function () {
        self.createLabelFlag = true;
    };

    self.cancelCreateLabel = function () {
        self.labelName = "";
        self.labelPath = "";
        self.createLabelFlag = false;
    };

    self.createLabel = function () {
        if (self.labelName === undefined || self.labelName.length == 0) {
            self.message = "Please provide a name!"
        } else if (self.labelPath === undefined || self.labelPath.length == 0) {
            self.message = "Please provide the path to a directory!"
        } else {
            for (var i in self.pipeline.labels) {
                if (self.pipeline.labels[i].name === self.labelName) {
                    self.message = "Please use a different name! Label already exists";
                    return;
                }
            }

            var data = {};
            data.pipelineName = self.pipeline.name;
            data.labelName = self.labelName;
            data.labelPath = self.labelPath;
            data.labelId = self.labelName;
            data.labelType = "customType";
            //add a new label
            $http.post('/label/create', data).then(function (response) {
                self.pipeline = JSON.parse(response.data.result);
                self.labelName = "";
                self.labelPath = "";
                self.createLabelFlag = false;
            });
        }
    };

    self.removeLabel = function (label) {
        //remove a label
        var data = {};
        data.pipelineName = self.pipeline.name;
        data.labelName = label.name;
        data.labelPath = label.path;
        data.labelId = label.labelId;
        data.labelType = label.type;

        $http.post('/label/remove', data).then(function (response) {
            self.pipeline = JSON.parse(response.data.result);
        });
    };

    self.trainDocuments = function () {
        self.isTraining = true;
        $http.get('/pipeline/train?name=' + self.pipeline.name).then(function (response) {
            self.result = response.data.result;
            self.showResults = true;
            self.isTraining = false;
        });
    };

    self.updateClassifier = function () {
        var data = {};
        data.pipelineName = self.pipeline.name;
        data.classifierName = self.classifier;
        $http.post('/pipeline/classifier', data);
    };

    self.linkSC = function () {
        $('#sc-modal').modal('open');
        scData.Workspace.query(function (workspaces) {
            self.workspaces = workspaces;
        });
    };

    self.getPages = function () {
        self.pages = [];
        self.attributeType = "Page";
        scData.Workspace.get({id: self.workspace}, function (workspace) {
            scData.Entity.get({id: workspace.rootEntity.id}, function (entity) {
                entity.children.forEach(function (subpage) {
                    if (subpage.name.indexOf(".") < 0) {
                        self.pages.push(subpage);
                    }
                });
            });
        });

        self.types = [];
        scData.Workspace.getEntityTypes({id: self.workspace}, function (types) {
            self.types = types;
        });
    };

    self.getAttributes = function (type) {
        self.attributes = [];
        scModel.EntityType.getAttributeDefinitions({id: type.id}, function (attributes) {
            self.attributes = attributes;
        });
    };

    self.getAttributeValues = function (attributeId) {
        self.values = [];
        self.attributeType = null;
        scModel.AttributeDefinition.get({id: attributeId}, function (attribute) {
            self.label = attribute.name;
            if (attribute.attributeType === "Boolean") {
                self.attributeType = "Boolean";
                self.values = [0, 1];
            } else if (attribute.attributeType === "Link") {
                self.attributeType = "Link";
                var linkedId = attribute.options.entityType.id;
                scModel.EntityType.getEntities({id: linkedId}, function (entities) {
                    entities.forEach(function (entity) {
                        self.values.push(entity);
                    });
                })
            }
        });
    }

    self.updateLabels = function () {
        if (self.attributeType === "Page") {
            self.selectedPages.forEach(function (page) {
                var data = {};
                data.pipelineName = self.pipeline.name;
                data.labelName = page.name;
                data.labelPath = page.href;
                data.labelId = page.id;
                data.labelType = "Page";

                //add a new label
                $http.post('/label/create', data).then(function (response) {
                    self.pipeline = JSON.parse(response.data.result);
                });
            });
        } else {
            self.selectedValues.forEach(function (value) {
                var data = {};
                data.pipelineName = self.pipeline.name;
                data.label = self.label;

                var miningAttr = [];
                self.selectedAttributesForMining.forEach(function (attr) {
                    miningAttr.push(attr.name);
                });
                data.miningAttributes = miningAttr;

                if (self.attributeType === "Boolean") {
                    data.labelName = value;
                    data.labelPath = self.selectedTypes[0].href;
                    data.labelId = self.selectedTypes[0].id;
                    data.labelType = "Boolean";
                } else {
                    data.labelName = value.name;
                    data.labelPath = self.selectedTypes[0].href;
                    data.labelId = self.selectedTypes[0].id;
                    data.labelType = "Link";
                }
                //add a new label
                $http.post('/label/create', data).then(function (response) {
                    self.pipeline = JSON.parse(response.data.result);
                });
            });
        }
    };

    self.updateSelection = function (position, entities) {
        angular.forEach(entities, function (subscription, index) {
            if (position != index)
                subscription.checked = false;
        });
    };
}]);