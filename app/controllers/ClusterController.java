package controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.PersistentEntity;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.HelperService;
import services.PipelineService;
import spark.clusterers.BaseClusterPipeline;
import spark.dataloaders.CSVDataLoader;
import spark.examples.ExamplePredictPipeline1;
import spark.pipelines.SparkPipelineFactory;
import spark.pipelines.SparkPredictPipeline;
import util.StaticFunctions;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Integer.parseInt;
import static spark.utils.FileUtil.jsonToCSVConverter;
import static spark.utils.FileUtil.saveDataAsCSV;
import static spark.utils.SparkDatasetUtil.clusterTableToJson;
import static spark.utils.SparkDatasetUtil.datasetToJson;
import static spark.utils.SparkDatasetUtil.extractClusterTablefromDataset;


public class ClusterController extends Controller {

    @Inject
    WSClient ws;

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

    public Result getPipeline(String pipelineName){
        PersistentEntity pipeline = PipelineService.getClusterPipeline(pipelineName);
        return ok(Json.parse(StaticFunctions.deserializeToJSON(pipeline)));
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
        String filepath = data.get("dataset").asText();
        Logger.info(data.get("scLink").asText());
        if(data.get("scLink").asBoolean()){
            String filename = data.get("scData").get("filename").asText();
            filepath = "myresources/datasets/"+filename;
            String scTypeURL = data.get("scData").get("type").get("href").asText();
            ArrayNode attributesToMine = (ArrayNode) data.get("scData").get("miningAttributes");
            List<String> miningAttributes = new ArrayList<>();
            for(JsonNode attribute: attributesToMine){
                miningAttributes.add(attribute.asText());
            }

            HelperService hs = new HelperService(this.ws);
            ArrayNode scData = hs.getSCData(scTypeURL, miningAttributes);
            StringBuilder records = jsonToCSVConverter(scData, miningAttributes);
            try {
                saveDataAsCSV(filepath, records);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //return ok(Json.toJson(hs.getSCData(scTypeURL,miningAttributes)));
        }
        JsonNode results = Json.toJson(Json.parse("{}"));

        switch(parseInt(data.get("library").get("id").toString())){
            case 2:{
                Logger.info("....WEKA.....");
                //Do Nothing : In Progress
            }
            break;
            case 1:
            default:
            {
                Logger.info("....Spark.....");
                SparkPipelineFactory sparkPipelineFactory = new SparkPipelineFactory(data);
                Dataset<Row> spark_results = sparkPipelineFactory.trainPipeline(data.get("name").toString(), filepath, "csv");
                results = Json.toJson(datasetToJson(extractClusterTablefromDataset(spark_results)));
            }
            break;
        }
        JsonNode json_results = Json.toJson(Json.parse("{}"));
        return ok(((ObjectNode)json_results).set("results", results));
    }

    public Result getSimilarDocuments(){
        JsonNode pipeline = request().body().asJson().get("pipeline");
        String textToCluster = request().body().asJson().get("textToClassify").asText();
        String pipelineName = pipeline.get("name").asText();
        JsonNode results = Json.toJson(Json.parse("{}"));
        switch(pipeline.get("library").asInt()){
            case 2:{
                Logger.info("....Building in Progress.....");
                //Do Nothing : In Progress
                ((ObjectNode) results).set("results", Json.toJson("In Progress"));
            }
            break;
            case 1:
                Logger.info(".....Prediction Library: Spark..................");
                SparkPredictPipeline predictPipeline = new SparkPredictPipeline(pipelineName);
                Dataset<Row> result = predictPipeline.predict(textToCluster);
                results = Json.toJson(datasetToJson(result));
                break;
            default:
                ((ObjectNode) results).set("results", Json.toJson("Not Found"));
                Logger.info(".....Prediction Library: Not Found..................");
        }
        return ok(results);
    }

}

