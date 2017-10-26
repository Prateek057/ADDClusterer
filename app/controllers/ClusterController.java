package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.mvc.Controller;
import play.mvc.Result;
import spark.BaseClusterPipeline;
import spark.CSVDataLoader;
import spark.ExampleKMeansPipeline1;
import spark.ExampleKMeansPipeline2;
import views.html.index;

import javax.inject.Inject;

import static dl4j.ExampleDL4JKmeans.clusterDocuments;
import static spark.SparkDatasetUtil.datasetToJson;

public class ClusterController extends Controller {
    @Inject
    private WebJarAssets webJarAssets;

    @Inject
    private ExampleKMeansPipeline1 exampleKMeansPipeline1;

    @Inject
    private ExampleKMeansPipeline2 exampleKMeansPipeline2;

    //Run Spark KMean Example Pipeline1
    public Result runPipelineExample1(){
        Dataset<Row> results = exampleKMeansPipeline1.trainPipeline();
        JsonNode jsonResults = datasetToJson(results);
        //datasetToJson(results);
        return ok(jsonResults);
    }

    //Run Spark KMean Example Pipeline2
    public Result runPipelineExample2(){
        exampleKMeansPipeline2.trainPipeline();
        return ok();
    }

    //Run DL4J Pipeline1
    public Result runPipelineExample3(){
        clusterDocuments();
        return ok();
    }

    public Result runBasePipeline(){
        CSVDataLoader csvDataLoader = new CSVDataLoader();
        BaseClusterPipeline baseClusterPipeline = new BaseClusterPipeline(csvDataLoader);
        baseClusterPipeline.trainPipeline("../myresources/datasets/tasksNoHeader.csv", new String[] { "_c0", "_c1" });
        return ok();
    }

}
