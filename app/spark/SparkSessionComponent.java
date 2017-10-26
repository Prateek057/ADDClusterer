package spark;

import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import javax.inject.Singleton;

@Singleton
public class SparkSessionComponent {

    private static SparkSessionComponent sparkSessionComponent;
    private final SparkSession sparkSession;
    private final SQLContext sqlContext;

    public SparkSessionComponent() {
        sparkSession = SparkSession.builder().appName("SparkPipelines").master("local[4]")
                .config("spark.ui.port", "9090")
                .getOrCreate();

        sqlContext = new SQLContext(sparkSession);
    }

    public static SparkSessionComponent getSparkSessionComponent(){
        if(null == sparkSessionComponent){
            sparkSessionComponent = new SparkSessionComponent();
        }
        return sparkSessionComponent;
    }

    public SparkSession getSparkSession() {
        return sparkSession;
    }

    public void stopSparkSession(){
        sparkSession.stop();
    }

    public SQLContext getSqlContext() {
        return sqlContext;
    }
}
