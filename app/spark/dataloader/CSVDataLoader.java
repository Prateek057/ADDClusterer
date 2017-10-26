package spark.dataloader;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import spark.SparkSessionComponent;

import java.io.File;

public class CSVDataLoader implements ISparkDataLoader {
    private SparkSessionComponent sparkSessionComponent;
    public CSVDataLoader(){
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
    }

    public Dataset<Row> loadData(String path) {
        SparkSession spark = sparkSessionComponent.getSparkSession();
        File csvFile = new File(path);
        String absolutePath = csvFile.getAbsolutePath();
        return spark.read().csv(absolutePath);
    }
}
