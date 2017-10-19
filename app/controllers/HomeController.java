package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import javax.inject.Inject;
//import dl4j.CSVDataHelper;

//import static dl4j.CSVDataHelper.*;
//import static dl4j.DataVecETL.runDataToVec;
//import static dl4j.ParagraphVectorsTextKMeansExample.runParVecAlgo;
import static dl4j.ExampleDL4JKmeans.clusterDocuments;
import static rm.CSVDataLoader.rmCSVLoader;

public class HomeController extends Controller {
    @Inject
    WebJarAssets webJarAssets;

    public Result index(){
        clusterDocuments();
        return ok(index.render(webJarAssets));
    }

    public Result readcsv(){
        rmCSVLoader();
        return ok(index.render(webJarAssets));
    }

    public Result any(String any) {
        //readCSV();
        return ok(views.html.index.render(webJarAssets));
    }
}
