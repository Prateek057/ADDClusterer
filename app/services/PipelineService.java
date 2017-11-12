package services;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.ClusterPipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import play.libs.Json;
import spark.SparkSessionComponent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static spark.utils.FileUtil.getFilesList;

public class PipelineService {



    public static List<String> getAllTrainedModels(){
        return getFilesList("myresources/models");
    }

    public static List<String> getAllClustersResults(){
        return getFilesList("myresources/results");
    }

    public static Dataset<Row> getPipelineClusters(String pipelineName){
        SparkSessionComponent sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
        SQLContext sqlContext = sparkSessionComponent.getSqlContext();
        return sqlContext.sql("select * from default."+pipelineName);
    }

    public static void saveClusterPipelineSettings(JsonNode settings){
        ClusterPipeline clusterPipeline = new ClusterPipeline(
                settings.get("href").toString(),
                settings.get("name").toString(),
                settings.get("library").toString(),
                settings.get("algorithm").get("id").toString(),
                settings.get("transformer").get("id").toString(),
                getPreprocessorList(settings.get("preprocessors"))
                );
        clusterPipeline.save();
    }

    private static ArrayList<String> getPreprocessorList(JsonNode preprocessors) {
        ArrayList<String> array = new ArrayList<>();
        for(Iterator<String> fieldNames = preprocessors.fieldNames(); fieldNames.hasNext();){
            String fieldName = fieldNames.next();
            if(preprocessors.get(fieldName).asBoolean()){
                array.add(fieldName);
            }
        }
        return array;
    }
}
