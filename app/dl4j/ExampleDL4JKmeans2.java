package dl4j;

import org.apache.spark.sql.Row;
import spark.dataloaders.CSVDataLoader;

import java.util.List;

public class ExampleDL4JKmeans2 {

    public static void clusterDocuments(){
        CSVDataLoader csvDataLoader = new CSVDataLoader();
        List<Row> dataset = csvDataLoader.loadData("myresources/datasets/tasksNoHeader.csv").collectAsList();

    }
}
