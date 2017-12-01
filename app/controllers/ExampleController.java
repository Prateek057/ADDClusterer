package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.spark.annotation.Experimental;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import spark.examples.ExampleKMeansPipeline1;
import spark.examples.ExampleKMeansPipeline2;
import weka.core.json.JSONNode;
import weka.examples.ExampleWekaClusterer1;

import static controllers.ClusterController.getSortedClusterResults;
//import static dl4j.ExampleDL4JKmeans.clusterDocuments;
//import static rm.CSVDataLoader.rmCSVLoader;
import static spark.examples.ExamplePredictPipeline1.predictLables;
import static spark.utils.SparkDatasetUtil.extractClusterTablefromDataset;

public class ExampleController extends Controller{


    //Run Spark KMean Example Pipeline1
    public Result runPipelineExample1() {
        ExampleKMeansPipeline1 exampleKMeansPipeline1 = new ExampleKMeansPipeline1();
        Dataset<Row> results = exampleKMeansPipeline1.trainPipeline();
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    //Run Spark KMean Example Pipeline2
    public Result runPipelineExample2() {
        ExampleKMeansPipeline2 exampleKMeansPipeline2 = new ExampleKMeansPipeline2();
        Dataset<Row> results = exampleKMeansPipeline2.trainPipeline();
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    //Spark Predict Pipeline Example
    public Result runPredictPipelineExample1(String modelName) {
        Dataset<Row> results = predictLables(modelName, "Spark KMeans Prediction Example");
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    //WEKA Data Source Example
    public Result runDataSourceExample(){
        ExampleWekaClusterer1 exampleWekaClusterer1 = new ExampleWekaClusterer1();
        return ok((exampleWekaClusterer1.loadData().toString()));
    }

}
