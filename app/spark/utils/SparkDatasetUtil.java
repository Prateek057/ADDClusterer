package spark.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import play.libs.Json;

import java.util.Iterator;
import java.util.List;

public class SparkDatasetUtil {

    public static JsonNode datasetToJson(Dataset<Row> dataset){
        ArrayNode array = new ArrayNode(new JsonNodeFactory(true));
        List<String> datasetJson = dataset.toJSON().collectAsList();
        datasetJson.forEach((row) ->
            array.add(Json.toJson(Json.parse(row)))
        );
        return Json.toJson(array);
    }
}
