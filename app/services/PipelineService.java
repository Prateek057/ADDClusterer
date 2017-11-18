package services;


import com.fasterxml.jackson.databind.JsonNode;
import model.ClusterPipeline;
import model.PersistentEntity;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import spark.SparkSessionComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        SparkSession spark = sparkSessionComponent.getSparkSession();
        return spark.read().json("myresources/results/" + pipelineName);
        //SQLContext sqlContext = sparkSessionComponent.getSqlContext();
        //return sqlContext.sql("select * from default."+pipelineName);
    }

    public static void saveClusterPipelineSettings(JsonNode settings){
        ClusterPipeline clusterPipeline = new ClusterPipeline(
                settings.get("href").asText(),
                settings.get("name").asText(),
                settings.get("library").get("id").asText(),
                settings.get("algorithm").get("id").asText(),
                settings.get("transformer").get("id").asText(),
                settings.get("dataset").asText()
                //getPreprocessorList(settings.get("preprocessors"))
                );
        System.out.print(clusterPipeline);
        clusterPipeline.save();
    }

    public static List<? extends PersistentEntity> getAllClusterPipelines(){
        ClusterPipeline clusterPipeline = new ClusterPipeline();
        return clusterPipeline.getAll();
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
