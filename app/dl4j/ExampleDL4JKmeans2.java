package dl4j;

import org.apache.commons.io.FileUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import spark.dataloader.CSVDataLoader;

import java.util.List;

public class ExampleDL4JKmeans2 {

    public static void clusterDocuments(){
        CSVDataLoader csvDataLoader = new CSVDataLoader();
        List<Row> dataset = csvDataLoader.loadData("myresources/datasets/tasksNoHeader.csv").collectAsList();

    }
}
