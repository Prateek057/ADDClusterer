package spark.utils;

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
        newDataset = newDataset.withColumn("document", newDataset.col(columns[0]));
        newDataset.show();
        for (int i = 1; i < noofcolumns; i++) {
            newDataset = newDataset.withColumn("document", functions.concat_ws(" ", newDataset.col("document"), newDataset.col(columns[i])));
        }
        return newDataset;
    }

    /*
    * Remove all punctuations from the text corpus
    * Returns Dataset containing a new or renames column called "document"
    * */
    public static Dataset<Row> removePunctuation(String columnName, Dataset<Row> inputDataset) {
        inputDataset = inputDataset.withColumn("document", functions.regexp_replace(inputDataset.col(columnName), "^\\w", " "));
        return inputDataset.withColumn("document", functions.regexp_replace(inputDataset.col(columnName), "[{,.!?:;}]", " "));
    }

    public static Dataset<Row> toLowerCase(String columnName, Dataset<Row> inputDataset){
        return inputDataset.withColumn("document", functions.lower(inputDataset.col(columnName)));
    }

    public static Dataset<Row> addIDColumn(Dataset<Row> inputDataset){
        return inputDataset.withColumn("DOC_ID", functions.monotonically_increasing_id());
    }
}
