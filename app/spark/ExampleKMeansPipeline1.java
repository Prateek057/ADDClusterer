package spark;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.sql.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;

/*
* This is an Example ML KMeans Pipeline with Preprocessors: Tokenizer, Remove Stop Words, Word2Vec.
* Input: CSV file with Summary & Description of Design Decisions
* Output: Cluster Labeled Dataset
*
*
* */
@Singleton
public class ExampleKMeansPipeline1 implements ISparkClusterPipeline{

    private @Inject SparkSessionComponent sparkSessionComponent;

    public Object[] trainPipeline() {

        System.out.println("\n...........................Example PipeLine 1: Tokenizer, Remove StopWords, Word2Vec, KMeans...........................");


        SparkSession spark = sparkSessionComponent.getSparkSession();
        System.out.print("\n");


        // Load and parse data
        String path = new File("D:\\TUM\\Master Thesis\\DataSets\\tasksNoHeader.csv").getAbsolutePath();
        Dataset<Row> inputData = spark.read().csv(path);

        //Display Loaded Dataset
        inputData.show();

        //Concat the String columns
        inputData = inputData.withColumn("document", functions.concat_ws(" ", inputData.col("_c0"), inputData.col("_c1")));

        //Split into training & testing datasets
        double[] weights = {0.5, 0.5};
        Dataset<Row>[] splitDataSet =  inputData.randomSplit(weights);
        Dataset<Row> trainingData = splitDataSet[0];
        Dataset<Row> testingData = splitDataSet[1];

        //Setup tokenizer
        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("document")
                .setOutputCol("words");

        //Setup Stop words removal
        StopWordsRemover stopWordsRemover = new StopWordsRemover()
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("filtered");

        //Setup Word2Vec
        Word2Vec word2Vec = new Word2Vec()
                .setInputCol(stopWordsRemover.getOutputCol())
                .setOutputCol("features")
                .setVectorSize(100)
                .setMinCount(0);


        KMeans kmeans = new KMeans()
                .setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);


        Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[] {tokenizer, stopWordsRemover, word2Vec, kmeans});


        // Fit the pipeline to training documents.
        PipelineModel model = pipeline.fit(trainingData);
        Dataset<Row> results = model.transform(inputData);

        System.out.println("\n...........................Results...........................");
        results.show();

        System.out.println("\n......Saving Results...........................");
        results.write().format("json").save("../DocClassification/myresources/results/example-pipeline-1");

        System.out.println("\n...........................Example PipeLine 1: The End...........................");

        return results.collectAsList().toArray();

    }
}
