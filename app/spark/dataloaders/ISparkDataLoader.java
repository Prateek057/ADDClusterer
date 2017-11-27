package spark.dataloaders;

import interfaces.IDataLoader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface ISparkDataLoader extends IDataLoader {
    Dataset<Row> loadData(String path);
}
