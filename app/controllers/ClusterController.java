package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import spark.clustering.BaseClusterPipeline;
import spark.dataloader.CSVDataLoader;
import spark.examples.ExampleKMeansPipeline1;
import spark.examples.ExampleKMeansPipeline2;

import javax.inject.Inject;

import static dl4j.ExampleDL4JKmeans.clusterDocuments;
import static spark.utils.SparkDatasetUtil.datasetToJson;

public class ClusterController extends Controller {

    @Inject
    private ExampleKMeansPipeline1 exampleKMeansPipeline1;

    @Inject
    private ExampleKMeansPipeline2 exampleKMeansPipeline2;

    //Run Spark KMean Example Pipeline1
    public Result runPipelineExample1(){
        Dataset<Row> results = exampleKMeansPipeline1.trainPipeline();
        JsonNode jsonResults = datasetToJson(results);
        return ok(jsonResults);
    }

    //Run Spark KMean Example Pipeline2
    public Result runPipelineExample2(){
        Dataset<Row> results = exampleKMeansPipeline2.trainPipeline();
        JsonNode jsonResults = datasetToJson(results);
        return ok(jsonResults);
    }

    //Run DL4J Pipeline1
    public Result runPipelineExample3(){
        clusterDocuments();
        return ok();
    }


    //Returns unique id of pipeline model
    public Result runBasePipeline(){
        CSVDataLoader csvDataLoader = new CSVDataLoader();
        BaseClusterPipeline baseClusterPipeline = new BaseClusterPipeline(csvDataLoader);
        PipelineModel pipelineModel = baseClusterPipeline.trainPipeline("myresources/datasets/tasksNoHeader.csv");
        return ok(Json.toJson(pipelineModel.uid()));
    }

}
