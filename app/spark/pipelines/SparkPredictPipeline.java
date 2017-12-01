package spark.pipelines;

import interfaces.IPredictPipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.Transformer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import spark.SparkSessionComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SparkPredictPipeline implements IPredictPipeline {

    private PipelineModel predictModel;
    private Integer predictedLabel;
    private StructType schema;
    private SparkSessionComponent sparkSessionComponent;
    private SparkSession spark;
    private String modelPath;

    public SparkPredictPipeline(String pipelineName) {
        sparkSessionComponent = SparkSessionComponent.getSparkSessionComponent();
        spark = sparkSessionComponent.getSparkSession();
        modelPath = "myresources/models/"+pipelineName;

    }

    public Dataset<Row> predict(String textToCluster){
        createSchema();
        readModel();
        return tranfromText(textToCluster);
    }

    private void createSchema() {
        this.schema = new StructType(new StructField[]{
                new StructField("document", DataTypes.StringType, false, Metadata.empty())
        });
    }

    private void readModel() {
        this.predictModel = PipelineModel.load(modelPath);
    }

    private Dataset<Row> tranfromText(String textToCluster) {
        List<Row> textInput = Arrays.asList(
                RowFactory.create(textToCluster)
        );
        Dataset<Row> dataset = spark.createDataFrame(textInput, this.schema);
        return predictModel.transform(dataset);
    }

    private void getClusterByLabel() {

    }

    private void applyRanking() {

    }
}
