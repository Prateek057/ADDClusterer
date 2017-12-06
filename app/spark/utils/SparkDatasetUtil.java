package spark.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SparkDatasetUtil {

    public static JsonNode datasetToJson(Dataset<Row> dataset) {
        ArrayNode array = new ArrayNode(new JsonNodeFactory(true));
        List<String> datasetJson = dataset.toJSON().collectAsList();
        datasetJson.forEach((row) -> array.add(Json.toJson(Json.parse(row))));
        return Json.toJson(array);
    }

    public static ArrayNode datasetToJsonArray(Dataset<Row> dataset) {
        ArrayNode array = new ArrayNode(new JsonNodeFactory(true));
        List<String> datasetJson = dataset.toJSON().collectAsList();
        datasetJson.forEach((row) -> array.add(Json.toJson(Json.parse(row))));
        return array;
    }

    public static JsonNode clusterTableToJson(Dataset<Row> dataSet) {
        String dataset_count = dataSet.agg(functions.sum("count")).first().get(0).toString();
        ArrayNode clusterArrayNode = new ArrayNode(new JsonNodeFactory(true));
        JsonNode clusterNode = datasetToJson(dataSet);
        clusterNode.forEach(
                (cluster) ->
                {

                    if (!cluster.findPath("members").isMissingNode()) {
                        ArrayNode memberArrayNode = new ArrayNode(new JsonNodeFactory(true));
                        JsonNode members = cluster.path("members");
                        members.forEach((member) -> memberArrayNode.add(Json.parse(member.asText())));
                        ((ObjectNode) cluster).set("members", memberArrayNode);
                    }
                    clusterArrayNode.add(cluster);

                }
        );
        ObjectNode on = new ObjectNode(new JsonNodeFactory(true));
        on.set("member_count", Json.toJson(dataset_count));
        on.set("cluster_table", Json.toJson(clusterArrayNode));
        return Json.toJson(on);
    }


    /*
    * Input Dataset should already contain a column with cluster labels named "cluster_label"
    */
    public static Dataset<Row> extractClusterTablefromDataset(Dataset<Row> dataset) {

        //Combine Rows into Json String and Add id to result Dataset
        Dataset<Row> jsonResult = dataset.toJSON().withColumn("json_id", functions.monotonicallyIncreasingId());

        //add Id to inout dataset for join
        Dataset<Row> resultsWithId = dataset.withColumn("id", functions.monotonicallyIncreasingId());

        //Join on id, rename json and cluster_label column
        resultsWithId = resultsWithId.join(jsonResult, resultsWithId.col("id").equalTo(jsonResult.col("json_id")))
                .drop("json_id")
                .withColumnRenamed("value", "json")
                .withColumnRenamed("cluster_label", "result_cluster_label")
        ;
        Dataset<Row> clusterResults = resultsWithId
                .groupBy("result_cluster_label")
                .agg(functions.collect_list("json").alias("members"));


        Dataset<Row> cluster_count_table = dataset.groupBy(dataset.col("cluster_label")).count().sort(dataset.col("cluster_label"));

        Dataset<Row> cluster_table = cluster_count_table
                .join(clusterResults,
                        cluster_count_table.col("cluster_label").equalTo(clusterResults.col("result_cluster_label")),
                        "left")
                .drop("result_cluster_label");

        return cluster_table;
    }
}
