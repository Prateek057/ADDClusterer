package spark.pipelines;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.BisectingKMeans;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import spark.clustering.SparkClusterFactory;
import spark.dataloader.DataLoaderFactory;
import spark.dataloader.ISparkDataLoader;

import java.io.IOException;

import static services.PipelineService.saveClusterPipelineSettings;
import static spark.preprocessing.SparkCommonPreprocessor.commonPreprocess;
import static spark.utils.SparkStringColumnUtil.addIDColumn;

public class SparkPipelineFactory {


    private String pipelineName;
    private DataLoaderFactory dataLoaderFactory;
    private Tokenizer tokenizer;
    private StopWordsRemover stopWordsRemover;
    private Word2Vec word2Vec;
    private HashingTF hashingTF;
    private KMeans kMeans;
    private BisectingKMeans bisectingKMeans;
    private PipelineStage[] pipelineStages;
    private Pipeline pipeline;
    private PipelineModel pipelineModel;
    private NGram nGrams;
    private Dataset<Row> dataSet;


    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName.replaceAll("\"", "");
    }

    private void loadData(String type, String path){
        ISparkDataLoader dataLoader = dataLoaderFactory.getDataLoader(type);
        dataSet = dataLoader.loadData(path);
    }

    public SparkPipelineFactory(JsonNode settings){
        dataLoaderFactory = new DataLoaderFactory();

        initPipelineStages();
        saveClusterPipelineSettings(settings);
        setPipelineStages(settings);
        pipeline = new Pipeline().setStages(pipelineStages);
    }


    private void initPipelineStages(){
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

        hashingTF = new HashingTF()
                .setNumFeatures(1000)
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("features");

        nGrams = new NGram()
                .setN(2)
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("nGrams");

        kMeans = new KMeans()
                .setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);

        bisectingKMeans = new BisectingKMeans() .setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);

    }

    private void setPipelineStages(JsonNode settings) {
        switch (settings.get("algorithm").get("id").toString()){
            case "spark-kmeans":
                switch(settings.get("transformer").get("id").toString()){
                    case "spark-word2vec":
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                word2Vec,
                                kMeans
                        };
                        break;
                    case "hashing-tf":
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                hashingTF,
                                kMeans
                        };
                        break;
                    default:
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                hashingTF,
                                kMeans
                        };
                }
                break;
            case "spark-bi-kmeans":
                switch(settings.get("transformer").get("id").toString()){
                    case "spark-word2vec":
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                word2Vec,
                                bisectingKMeans
                        };
                        break;
                    case "hashing-tf":
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                hashingTF,
                                bisectingKMeans
                        };
                        break;
                    case "":
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                hashingTF,
                                bisectingKMeans
                        };
                        break;
                    default:
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                hashingTF,
                                bisectingKMeans
                        };
                }
                break;
            default:
                pipelineStages = new PipelineStage[]{
                        tokenizer,
                        stopWordsRemover,
                        word2Vec,
                        kMeans
                };
        }
        pipeline = new Pipeline().setStages(pipelineStages);
    }

    private void runCommonPreprocessor(){
        dataSet = commonPreprocess(dataSet, dataSet.columns());
        dataSet = addIDColumn(dataSet);
    }

    private void savePipelineModel(){
        String path = "myresources/models/"+pipelineName;
        System.out.print(path);
        try {
            pipelineModel.write().overwrite().save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveResults(Dataset<Row> results){
        results.write().format("json").mode("overwrite").save("myresources/results/" + pipelineName);
        results.write().mode(SaveMode.Overwrite).saveAsTable(pipelineName);
    }

    public Dataset<Row> trainPipeline(String pipelineName, String path, String type){
        setPipelineName(pipelineName);
        loadData(type, path);
        runCommonPreprocessor();
        pipelineModel = pipeline.fit(dataSet);
        savePipelineModel();
        Dataset<Row> results = pipelineModel.transform(dataSet);
        saveResults(results);
        return results;
    }


}
