package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.PipelineService;
import spark.clustering.BaseClusterPipeline;
import spark.dataloader.CSVDataLoader;
import spark.examples.ExampleKMeansPipeline1;
import spark.examples.ExampleKMeansPipeline2;
import spark.pipelines.SparkPipelineFactory;

import javax.inject.Inject;

import java.io.*;
import java.util.List;

import static dl4j.ExampleDL4JKmeans.clusterDocuments;
import static java.lang.Integer.parseInt;
import static rm.CSVDataLoader.rmCSVLoader;
import static spark.examples.ExamplePredictPipeline1.predictLables;
import static spark.utils.SparkDatasetUtil.clusterTableToJson;
import static spark.utils.SparkDatasetUtil.datasetToJson;
import static spark.utils.SparkDatasetUtil.extractClusterTablefromDataset;

public class ClusterController extends Controller {

    @Inject
    private ExampleKMeansPipeline1 exampleKMeansPipeline1;

    @Inject
    private ExampleKMeansPipeline2 exampleKMeansPipeline2;


    public JsonNode getSortedClusterResults(Dataset<Row> dataset) {
        Dataset<Row> sortedResults = dataset.sort("cluster_label");
        return clusterTableToJson(sortedResults);
    }

    //Run Spark KMean Example Pipeline1
    public Result runPipelineExample1() {
        Dataset<Row> results = exampleKMeansPipeline1.trainPipeline();
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    //Run Spark KMean Example Pipeline2
    public Result runPipelineExample2() {
        Dataset<Row> results = exampleKMeansPipeline2.trainPipeline();
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    //Run DL4J Pipeline1
    public Result runDL4JPipelineExample() {
        try {
            clusterDocuments();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return ok();
    }

    public Result runPipeline(String pipelineName) {
        CSVDataLoader csvDataLoader = new CSVDataLoader();
        BaseClusterPipeline baseClusterPipeline = new BaseClusterPipeline(csvDataLoader);
        Dataset<Row> pipelineResults = baseClusterPipeline.trainPipeline("myresources/datasets/tasksNoHeader.csv", pipelineName);
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(pipelineResults));
        return ok(jsonResults);
    }

    //Run RM Pipeline1
    public Result runRm() {
        try {
            rmCSVLoader();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ok();
    }

    public Result runPredictPipelineExample1(String modelName) {
        Dataset<Row> results = predictLables(modelName);
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    public Result getTrainedPipelines() {
        List<String> results = PipelineService.getAllTrainedModels();
        return ok(Json.toJson(results));
    }

    public Result getClusterResults() {
        List<String> results = PipelineService.getAllClustersResults();
        return ok(Json.toJson(results));
    }

    public Result getAllClustersFromPipeline(String pipelineName){
        Dataset<Row> results = PipelineService.getPipelineClusters(pipelineName);
        results = results.drop("features");
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(Json.toJson(jsonResults));
    }

    public Result getLibraries() {
        try {
            FileInputStream conf = new FileInputStream("conf/libraries.json");
            JsonNode libraries = Json.parse(conf);
            return ok(libraries);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return ok(Json.parse("{results: null}"));
        }
    }

    public Result createClusterPipeline(){
        JsonNode data = request().body().asJson().get("pipeline");
        JsonNode results = Json.toJson(Json.parse("{}"));
        switch(parseInt(data.get("library").get("id").toString())){
            case 1: {
                SparkPipelineFactory sparkPipelineFactory = new SparkPipelineFactory(data);
                String path = "myresources/datasets/tasksNoHeader.csv";
                Dataset<Row> spark_results = sparkPipelineFactory.trainPipeline(data.get("name").toString(), path, "csv");
                results = Json.toJson(datasetToJson(extractClusterTablefromDataset(spark_results)));
            }
            break;
            case 2:{
                //Do nothing for now
            }
            break;
            default: {
                SparkPipelineFactory sparkPipelineFactory = new SparkPipelineFactory(data);
                String path = "myresources/datasets/tasksNoHeader.csv";
                Dataset<Row> spark_results = sparkPipelineFactory.trainPipeline(data.get("name").toString(), path, "csv");
                results = datasetToJson(extractClusterTablefromDataset(spark_results));
            }
        }
        JsonNode json_results = Json.toJson(Json.parse("{}"));
        return ok(((ObjectNode)json_results).set("results", results));
    }

}
