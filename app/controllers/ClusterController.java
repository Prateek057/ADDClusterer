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
import static spark.utils.SparkDatasetUtil.clusterTableToJson;
import static spark.utils.SparkDatasetUtil.datasetToJson;
import static spark.utils.SparkDatasetUtil.extractClusterTablefromDataset;

public class ClusterController extends Controller {

    @Inject
    private ExampleKMeansPipeline1 exampleKMeansPipeline1;

    @Inject
    private ExampleKMeansPipeline2 exampleKMeansPipeline2;

    public JsonNode getSortedClusterResults(Dataset<Row> dataset){
        Dataset<Row> sortedResults = dataset.sort("cluster_label");
        return clusterTableToJson(sortedResults);
    }
    //Run Spark KMean Example Pipeline1
    public Result runPipelineExample1(){
        Dataset<Row> results = exampleKMeansPipeline1.trainPipeline();
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    //Run Spark KMean Example Pipeline2
    public Result runPipelineExample2(){
        Dataset<Row> results = exampleKMeansPipeline2.trainPipeline();
        JsonNode jsonResults = getSortedClusterResults(extractClusterTablefromDataset(results));
        return ok(jsonResults);
    }

    //Run DL4J Pipeline1
    public Result runDL4JPipelineExample(){
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
