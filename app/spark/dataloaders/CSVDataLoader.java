package spark.dataloaders;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import spark.SparkSessionComponent;

import java.io.File;

public class CSVDataLoader implements ISparkDataLoader {
    private SparkSessionComponent sparkSessionComponent;

    public CSVDataLoader() {
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
    }

    public Dataset<Row> loadData(String path) {
        SparkSession spark = sparkSessionComponent.getSparkSession();
        System.out.println(path);
        File csvFile = new File(path);
        return spark.read().csv(path);
    }
}
