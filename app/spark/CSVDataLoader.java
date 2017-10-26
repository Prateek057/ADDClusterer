package spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import java.io.File;

public class CSVDataLoader implements ISparkDataLoader {
    private @Inject SparkSessionComponent sparkSessionComponent;

    public CSVDataLoader(){
    }
    @Override
    public Dataset<Row> loadData(String path) {
        SparkSession spark = sparkSessionComponent.getSparkSession();
        File csvFile = new File(path);
        String absolutePath = csvFile.getAbsolutePath();
        return spark.read().csv(absolutePath);
    }


}
