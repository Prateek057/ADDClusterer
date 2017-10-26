package spark;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.*;

import javax.inject.Inject;
import java.io.File;

public class ExampleKMeansPipeline2 implements  ISparkClusterPipeline{
    private @Inject SparkSessionComponent sparkSessionComponent;

    public void trainPipeline() {

        System.out.println("\n...........................Example PipeLine 2: Tokenizer, Remove StopWords, Hashing TF, KMeans...........................");

        SparkSession spark = sparkSessionComponent.getSparkSession();
        System.out.print("\n");

        // Load and parse data
        String path = new File("../DocClassification/myresources/datasets/tasksNoHeader.csv").getAbsolutePath();
        Dataset<Row> listData = spark.read().csv(path);
        listData = listData.withColumn("document", functions.concat_ws(" ", listData.col("_c0"), listData.col("_c1")));
        listData.show();

        double[] weights = {0.5, 0.5};
        Dataset<Row>[] splitDataSet =  listData.randomSplit(weights);
        Dataset<Row> trainingData = splitDataSet[0];
        Dataset<Row> testingData = splitDataSet[1];


        Tokenizer tokenizer = new Tokenizer()
                .setInputCol("document")
                .setOutputCol("words");

        StopWordsRemover stopWordsRemover = new StopWordsRemover()
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("filtered");


        HashingTF hashingTF = new HashingTF()
                .setNumFeatures(1000)
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("features");

        KMeans kmeans = new KMeans()
                .setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);



        Pipeline pipeline = new Pipeline()
                .setStages(new PipelineStage[] {tokenizer, stopWordsRemover, hashingTF, kmeans});


        // Fit the pipeline to training documents.
        PipelineModel model = pipeline.fit(trainingData);
        Dataset<Row> results = model.transform(testingData);

        System.out.println("\n.....................Results...........................");
        results.show();

        System.out.println("\n......Saving Results...........................");
        results.write().format("json").save("../DocClassification/myresources/results/example-pipeline-2");

        System.out.println("\n...........................Example Pipeline 2: The End...........................");

    }
}
