package spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static spark.SparkStringColumnUtil.concatStringTypeColumns;
import static spark.SparkStringColumnUtil.removePunctuation;
import static spark.SparkStringColumnUtil.toLowerCase;

public class SparkCommonPreprocessor {
    public static Dataset<Row> commonPreprocess(Dataset<Row> dataset, String[] listOfStringAttributeNames) {
        dataset = concatStringTypeColumns(listOfStringAttributeNames, dataset);
        dataset = removePunctuation("document", dataset);
        dataset = toLowerCase("document", dataset);
        return dataset;
    }
}
