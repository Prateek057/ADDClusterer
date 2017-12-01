package spark.pipelines;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.clustering.BisectingKMeans;
import org.apache.spark.ml.clustering.KMeans;
import org.apache.spark.ml.feature.*;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import spark.dataloaders.DataLoaderFactory;
import spark.dataloaders.ISparkDataLoader;

import java.io.IOException;

import static java.lang.Integer.parseInt;
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

    private void loadData(String type, String path) {
        System.out.println(path);
        ISparkDataLoader dataLoader = dataLoaderFactory.getDataLoader(type);
        dataSet = dataLoader.loadData(path);
    }

    public SparkPipelineFactory(JsonNode settings) {
        dataLoaderFactory = new DataLoaderFactory();
        initPipelineStages();
        saveClusterPipelineSettings(settings);
        setPipelineStages(settings);
        pipeline = new Pipeline().setStages(pipelineStages);
    }

    private void initPipelineStages() {
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

        bisectingKMeans = new BisectingKMeans().setK(20)
                .setFeaturesCol("features")
                .setPredictionCol("cluster_label")
                .setMaxIter(20);

    }

    private void setPipelineStages(JsonNode settings) {
        JsonNode algorithm = settings.get("algorithm");
        System.out.println(algorithm.get("id").asText());
        switch (settings.get("algorithm").get("id").asText()) {
            case "spark-kmeans":
                System.out.println(".....Spark KMeans.......");
                ArrayNode kmeansOptions = (ArrayNode) algorithm.get("options");
                setKMeansOptions(kmeansOptions);
                System.out.println(settings.get("transformer").get("id").toString());
                switch (settings.get("transformer").get("id").asText()) {
                    case "spark-word2vec":
                        System.out.println(".....Spark Word2Vec.......");
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                word2Vec,
                                kMeans
                        };
                        break;
                    case "hashing-tf":
                    default:
                        System.out.println(".....Spark HashingTF.......");
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                hashingTF,
                                kMeans
                        };
                        break;
                }
                break;
            case "spark-bi-kmeans":
                System.out.println(".....Spark Bisecting KMeans.......");
                switch (settings.get("transformer").get("id").asText()) {
                    case "hashing-tf":
                        System.out.println(".....Spark HashingTF.......");
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                hashingTF,
                                bisectingKMeans
                        };
                        break;
                    case "spark-word2vec":
                    default:
                        System.out.println(".....Spark Word2Vec.......");
                        pipelineStages = new PipelineStage[]{
                                tokenizer,
                                stopWordsRemover,
                                word2Vec,
                                bisectingKMeans
                        };
                        break;
                }
                break;
            default:
                setKMeansOptions((ArrayNode) algorithm.get("options"));
                System.out.println(".....Spark Default: KMeans-Word2Vec.......");
                pipelineStages = new PipelineStage[]{
                        tokenizer,
                        stopWordsRemover,
                        word2Vec,
                        kMeans
                };
        }
        pipeline = new Pipeline().setStages(pipelineStages);
    }

    private void runCommonPreprocessor() {
        dataSet = commonPreprocess(dataSet, dataSet.columns());
        dataSet = addIDColumn(dataSet);
    }

    private void savePipelineModel() {
        String path = "myresources/models/" + pipelineName;
        System.out.print(path);
        try {
            pipelineModel.write().overwrite().save(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveResults(Dataset<Row> results) {
        results.write().format("json").mode("overwrite").save("myresources/results/" + pipelineName);
        //uncomment below line to save to Spark Warehouse
        //results.write().mode(SaveMode.Overwrite).saveAsTable(pipelineName);
    }

    public Dataset<Row> trainPipeline(String pipelineName, String path, String type) {
        setPipelineName(pipelineName);
        loadData(type, path);
        runCommonPreprocessor();
        pipelineModel = pipeline.fit(this.dataSet);
        savePipelineModel();
        Dataset<Row> results = pipelineModel.transform(this.dataSet);
        saveResults(results);
        return results;
    }

    private void setKMeansOptions(ArrayNode options) {
        for (JsonNode option : options) {
            System.out.println(option);
            String name = option.get("name").asText();
            String value = option.get("value").asText();
            System.out.println(name);
            System.out.println(value);
            if (name.equals("K-value"))
                kMeans.setK(option.get("value").asInt());
            if (name.equals("iterations"))
                kMeans.setMaxIter(parseInt(value));
        }
    }

}
