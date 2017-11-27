package weka.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterFactory;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.SimpleKMeansClusterer;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.core.json.JSONInstances;
import weka.filters.WekaStringToWordVector;
import weka.filters.unsupervised.attribute.NominalToString;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static weka.utils.WekaStringAttributeUtils.concatStringTypeAttributes;

public class ExampleWekaClusterer1 {
    public JsonNode loadData(){
        try {
            CSVLoader loader = new CSVLoader();
            loader.setNoHeaderRowPresent(true);
            loader.setSource(new File("myresources/datasets/tasksNoHeader.csv"));
            Instances input_data = loader.getDataSet();
            Instances data = input_data;

            NominalToString filter1 = new NominalToString();
            filter1.setInputFormat(data);
            data = Filter.useFilter(data, filter1);
            filter1.setInputFormat(data);
            data = Filter.useFilter(data, filter1);

            data = concatStringTypeAttributes(data);
            input_data = data;



            StringToWordVector filter = new WekaStringToWordVector().get();
            filter.setInputFormat(data);
            data = Filter.useFilter(data, filter);

            //data.deleteAttributeAt(0);
            //data.deleteAttributeAt(1);

            // save ARFF
            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File("myresources/datasets/tasksNoHeader.arff"));
            saver.writeBatch();

            String dataSummary = data.toSummaryString();
            System.out.println("\n\nImported data:\n\n" + dataSummary);

            AbstractClusterer kmeans = new ClusterFactory().get("KMeans");
            kmeans.buildClusterer(data);
            int member_count = data.numInstances();



            int[] assignments = SimpleKMeansClusterer.getClusterAssignments((SimpleKMeans) kmeans);
            int i=0;


            ArrayNode cluster_array = new ArrayNode(new JsonNodeFactory(true));
            ObjectNode cluster_data = new ObjectNode(new JsonNodeFactory(true));
            cluster_data.set("member_count", Json.toJson(member_count));

            data.insertAttributeAt(new Attribute("cluster_label"), data.numAttributes());
            
            List<ArrayNode> cluster_members = new ArrayList<>();

            for(int clusterNum : assignments) {
                data.instance(i).setValue(data.numAttributes(), clusterNum);
                ObjectNode cluster = new ObjectNode(new JsonNodeFactory(true));
                cluster.set("instance", Json.toJson(i));
                cluster.set("cluster_label", Json.toJson(clusterNum));
                cluster_array.add(cluster);
                i++;
            }

            data.sort(data.attribute("cluster_label"));
            weka.core.json.JSONNode json_data = JSONInstances.toJSON(data);
            System.out.print(json_data.toString());
            cluster_data.set("cluster_table", cluster_array);
            ObjectNode results = new ObjectNode(new JsonNodeFactory(true));
            results.set("results", cluster_data);
            return Json.toJson(results);
        } catch (Exception e) {
            e.printStackTrace();
            ObjectNode results = new ObjectNode(new JsonNodeFactory(true));
            results.set("results", Json.toJson(e.getMessage()));
            //return null;
            return Json.toJson(results);
        }

    }

}
