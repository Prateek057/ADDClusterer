package spark.dataloaders;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface ISparkDataLoader {
    Dataset<Row> loadData(String path);
}
