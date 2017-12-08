package spark.pipelines;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import interfaces.IPredictPipeline;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.feature.StopWordsRemover;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.ml.feature.Word2Vec;
import org.apache.spark.ml.feature.Word2VecModel;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import play.libs.Json;
import services.PipelineService;
import spark.SparkSessionComponent;
import spark.ranking.IRankingStrategy;
import spark.ranking.RankingStrategy;
import util.StaticFunctions;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.lang.Integer.parseInt;

public class SparkPredictPipeline implements IPredictPipeline {

    private final Tokenizer tokenizer;
    private final StopWordsRemover stopWordsRemover;
    private PipelineModel predictModel;
    private String pipelineName;
    private String textToClassify;
    private Integer predictedLabel;
    private StructType schema;
    private SparkSessionComponent sparkSessionComponent;
    private SparkSession spark;
    private String modelPath;
    private Dataset<Row> results;
    private Dataset<Row> clusters;
    private IRankingStrategy rankingStrategy;


    public SparkPredictPipeline(String pipelineName) {
        rankingStrategy = new RankingStrategy().getRankingStrategy("cosineSimilarity");
        this.pipelineName = pipelineName;
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
        spark = sparkSessionComponent.getSparkSession();
        modelPath = "myresources/models/" + pipelineName;

        tokenizer = new Tokenizer()
                .setInputCol("document")
                .setOutputCol("words");

        stopWordsRemover = new StopWordsRemover()
                .setInputCol(tokenizer.getOutputCol())
                .setOutputCol("filtered");
    }

    public ArrayNode predict(String textToCluster) {
        createSchema();
        readModel();
        transfromText(textToCluster);
        getClusterByLabel();
        ArrayNode mapResults = null;
        try {
            mapResults = applyRanking();
        } catch (FileAlreadyExistsException f) {
            File file = new File("myresources/models/word2vec");
            try {
                FileUtils.deleteDirectory(file);
                mapResults = applyRanking();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapResults;
    }

    private void createSchema() {
        this.schema = new StructType(new StructField[]{
                new StructField("document", DataTypes.StringType, false, Metadata.empty())
        });
    }

    private void readModel() {
        this.predictModel = PipelineModel.load(modelPath);
    }

    private void transfromText(String textToCluster) {
        List<Row> textInput = Arrays.asList(
                RowFactory.create(textToCluster)
        );
        Dataset<Row> inputDocuments = spark.createDataFrame(textInput, this.schema);
        results = predictModel.transform(inputDocuments);
    }

    private void getClusterByLabel() {
        predictedLabel = results.select("cluster_label").first().getInt(0);
        Dataset<Row> cluster_table = PipelineService.getPipelineClusters(pipelineName);
        Dataset<Row> results1 = cluster_table.filter("cluster_label=" + predictedLabel);
        clusters = results1;
    }

    private ArrayNode applyRanking() throws FileAlreadyExistsException, IOException {
        Word2VecModel model = new Word2Vec().setInputCol("filtered")
                .setOutputCol("vectors")
                .setVectorSize(100)
                .setMinCount(0).fit(clusters);
        Dataset<Row> clusterDocuments = model.transform(clusters);
        Dataset<Row> queryDocuments = model.transform(results);
        Map<Long, Double> similarityMap = getSimilarityMap(clusterDocuments, queryDocuments);
        TreeMap<Long, Double> sortedMap = StaticFunctions.sortByValues(similarityMap);
        return getJsonFromSimilarityMap(sortedMap);
    }

    private ArrayNode getJsonFromSimilarityMap(TreeMap<Long, Double> sortedMap) {
        ArrayNode array = new ArrayNode(new JsonNodeFactory(true));
        sortedMap.forEach((doc_id,doc_similarity)->{
            ObjectNode topDoc = (ObjectNode) Json.parse(clusters.filter("DOC_ID=" + doc_id).limit(1).toJSON().collectAsList().get(0));
            if(doc_similarity != null){
                Float similarity = (doc_similarity.floatValue() * 100);
                if(similarity > 0.0){
                    topDoc.set("similarity", Json.toJson(String.format("%.2f", similarity)));
                    array.add(topDoc);
                }
            }
        });
        return array;
    }

    private Map<Long, Double> getSimilarityMap(Dataset<Row> clusterDocuments, Dataset<Row> queryDocuments) {
        Map<Long, Double> similarityMap = new HashMap<>();
        for (Row row : queryDocuments.collectAsList()) {
            Vector queryVector = (Vector) row.get(row.fieldIndex("vectors"));
            for (Row document : clusterDocuments.collectAsList()) {
                Long documentId = document.getLong(document.fieldIndex("DOC_ID"));
                Vector docVector = (Vector) document.get(document.fieldIndex("vectors"));
                Double similarity = rankingStrategy.getSimilarity(queryVector, docVector);
                similarityMap.put(documentId, similarity);
            }
        }
        return similarityMap;
    }

}
