package spark.pipelines;

import interfaces.IPredictPipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.Transformer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.ArrayList;

public class SparkPredictPipeline implements IPredictPipeline{

    private PipelineModel predictModel;
    private Integer predictedLabel;

    SparkPredictPipeline(){

    }

    SparkPredictPipeline(String modelPath){
        this.readModel(modelPath);
    }

    public void readModel(String modelPath) {
        this.predictModel = PipelineModel.load(modelPath);
    }

    public Dataset<Row> tranfromText(Dataset<Row> input) {
        return predictModel.transform(input);
    }

    public void getClusterByLabel() {

    }

    public void applyRanking() {

    }
}
