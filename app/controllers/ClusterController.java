package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.PersistentEntity;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.PipelineService;
import spark.clusterers.BaseClusterPipeline;
import spark.dataloaders.CSVDataLoader;
import spark.pipelines.SparkPipelineFactory;
import util.StaticFunctions;

import java.io.*;
import java.util.List;
import static java.lang.Integer.parseInt;
import static spark.utils.SparkDatasetUtil.clusterTableToJson;
import static spark.utils.SparkDatasetUtil.datasetToJson;
import static spark.utils.SparkDatasetUtil.extractClusterTablefromDataset;

public class ClusterController extends Controller {




    public static JsonNode getSortedClusterResults(Dataset<Row> dataset) {
        Dataset<Row> sortedResults = dataset.sort("cluster_label");
        return clusterTableToJson(sortedResults);
    }


    public Result runPipeline(String pipelineName) {
        CSVDataLoader csvDataLoader = new CSVDataLoader();
        BaseClusterPipeline baseClusterPipeline = new BaseClusterPipeline(csvDataLoader);
        Dataset<Row> pipelineResults = baseClusterPipeline.trainPipeline("myresources/datasets/tasksNoHeader.csv", pipelineName);
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(pipelineResults));
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

    public Result getAllClusterPipelines(){
        JsonNode pipelines_json = Json.toJson(Json.parse("{}"));
        List<? extends PersistentEntity> pipelines = PipelineService.getAllClusterPipelines();
        JsonNode deserializedPipelines = Json.parse(StaticFunctions.deserializeToJSON(pipelines));
        return ok(((ObjectNode) pipelines_json).set("pipelines", deserializedPipelines));
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
    public Result datasetUpload(){
        Http.MultipartFormData<File> body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> dataset = body.getFile("file");
        if (dataset != null) {
            String fileName = dataset.getFilename();
            String contentType = dataset.getContentType();
            File file = dataset.getFile();
            String newPath = "myresources/datasets/"+fileName;
            file.renameTo(new File(newPath));
            return ok(Json.parse("{ \"results\": { \"path\": \""+newPath+ " \"}}"));

        } else{
            return ok();
        }
    }
    public Result createClusterPipeline(){
        JsonNode data = request().body().asJson().get("pipeline");
        System.out.print(data);
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
                String path = data.get("dataset").asText();
                Dataset<Row> spark_results = sparkPipelineFactory.trainPipeline(data.get("name").toString(), path, "csv");
                results = datasetToJson(extractClusterTablefromDataset(spark_results));
            }
        }
        JsonNode json_results = Json.toJson(Json.parse("{}"));
        return ok(((ObjectNode)json_results).set("results", results));
    }

}
