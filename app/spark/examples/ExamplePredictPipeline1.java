package spark.examples;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import spark.SparkSessionComponent;

import java.io.File;

public class ExamplePredictPipeline1 {

    private static SparkSessionComponent sparkSessionComponent;
    //private SparkSessionComponent sparkSessionComponent;
    //TODO: Load CSV Data as Testing Dataset
    //TODO: Load given Pipeline Model
    //TODO: Transform Dataset using the model
    //TODO: Return Results
    public static Dataset<Row> predictLables(String modelName){
        System.out.println("\n...........................Example PipeLine 1: Tokenizer, Remove StopWords, Word2Vec, KMeans...........................");

        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();

        SparkSession spark = sparkSessionComponent.getSparkSession();
        System.out.print("\n");


        // Load and parse data
        String path = new File("../DocClassification/myresources/datasets/tasksNoHeader.csv").getAbsolutePath();
        Dataset<Row> inputData = spark.read().csv(path);

        //Display Loaded Dataset
        inputData.show();

        //Concat the String columns
        inputData = inputData.withColumn("document", functions.concat_ws(" ", inputData.col("_c0"), inputData.col("_c1")));

        //Split into training & testing datasets
        double[] weights = {0.7, 0.3};
        Dataset<Row>[] splitDataSet = inputData.randomSplit(weights);
        Dataset<Row> testingData = splitDataSet[0];

        PipelineModel savedModel = PipelineModel.load("myresources/models/" + modelName);
        return savedModel.transform(testingData);


    }


}
