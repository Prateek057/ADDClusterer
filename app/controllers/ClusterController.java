package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import spark.ExampleKMeansPipeline1;
import spark.ExampleKMeansPipeline2;
import views.html.index;

import javax.inject.Inject;

import static dl4j.ExampleDL4JKmeans.clusterDocuments;

public class ClusterController extends Controller {
    @Inject
    private WebJarAssets webJarAssets;

    @Inject
    private ExampleKMeansPipeline1 exampleKMeansPipeline1;

    @Inject
    private ExampleKMeansPipeline2 exampleKMeansPipeline2;

    public Result index(){

        return ok(index.render(webJarAssets));
    }

    //Run Spark KMean Example Pipeline1
    public Result runPipelineExample1(){
        Object[] results = exampleKMeansPipeline1.trainPipeline();
        return ok();
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

}
