package spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

public class SparkStringColumnUtil {

    /*
    * Concats row wise all values of passed string type columns
    * Returns Dataset containing a new column called "document" containing concatenated string values
    */
    public static Dataset<Row> concatStringTypeColumns(String[] columns, Dataset<Row> inputDataset) {
        int noofcolumns = columns.length;
        Dataset<Row> newDataset = inputDataset;
        newDataset.withColumn("document", newDataset.col(columns[0]));
        for (int i = 1; i <= noofcolumns; i++) {
            newDataset = newDataset.withColumn("document", functions.concat_ws(" ", inputDataset.col("documents"), inputDataset.col(columns[i])));
        }
        return newDataset;
    }

    /*
    * Remove all punctuations from the text corpus
    * Returns Dataset containing a new or renames column called "document"
    * */
    public static Dataset<Row> removePunctuation(String columnName, Dataset<Row> inputDataset) {
        return inputDataset.withColumn("document", functions.regexp_replace(inputDataset.col(columnName), "[^a-zA-Z0-9]", ""));
    }

    public static Dataset<Row> toLowerCase(String columnName, Dataset<Row> inputDataset){
        return inputDataset.withColumn("document", functions.lower(inputDataset.col(columnName)));
    }
}