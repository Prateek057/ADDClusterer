package spark.clustering;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import spark.dataloader.CSVDataLoader;
import spark.dataloader.ISparkDataLoader;
import spark.preprocessing.SparkCommonPreprocessor;

import java.io.IOException;

import static spark.utils.SparkStringColumnUtil.addIDColumn;

public class BaseClusterPipeline implements ISparkClusterPipeline {

    private ISparkDataLoader dataLoader;
    private PipelineStage[] basePipelineStages;
    private Pipeline basePipeline;
    private Tokenizer tokenizer;
    private StopWordsRemover stopWordsRemover;
    private Word2Vec word2Vec;
    private KMeans kMeans;


    public BaseClusterPipeline(ISparkDataLoader Dl) {
        if (Dl.getClass() == CSVDataLoader.class) {
            dataLoader = new CSVDataLoader();
        }
        createPipelineStages();
    }

    private void createPipelineStages() {
        tokenizer = new Tokenizer()
                .setInputCol("document")
                .setOutputCol("words");

        stopWordsRemover = new StopWordsRemover()
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("filtered");

        word2Vec = new Word2Vec()
                .setInputCol(stopWordsRemover.getOutputCol())
                .setOutputCol("features")
                .setVectorSize(100)
                .setMinCount(0);


        kMeans = new KMeans()
                .setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);

        basePipelineStages = new PipelineStage[]{tokenizer, stopWordsRemover, word2Vec, kMeans};
        basePipeline = new Pipeline().setStages(basePipelineStages);
    }


    public Dataset<Row> trainPipeline(String path, String pipelineName) {

        Dataset<Row> dataSet = dataLoader.loadData(path);
        dataSet = addIDColumn(dataSet);
        String[] columns = dataSet.columns();
        dataSet = SparkCommonPreprocessor.commonPreprocess(dataSet, columns);

        PipelineModel pipelineModel = basePipeline.fit(dataSet);
        Dataset<Row> results = pipelineModel.transform(dataSet);

        try {
            pipelineModel.write().overwrite().save("myresources/models/" + pipelineName);
            results.write().format("json").mode("overwrite").save("myresources/results/" + pipelineName);
            results.write().mode(SaveMode.Overwrite).saveAsTable(pipelineName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

}
