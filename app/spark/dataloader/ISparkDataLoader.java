package spark.dataloader;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import spark.SparkSessionComponent;

public interface ISparkDataLoader {
    Dataset<Row> loadData(String path);
}
