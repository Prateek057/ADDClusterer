package spark;

import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import play.inject.ApplicationLifecycle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Singleton
public class SparkSessionComponent {
    private final SparkSession sparkSession;
    private final SQLContext sqlContext;

    @Inject
    public SparkSessionComponent(ApplicationLifecycle lifecycle) {
        sparkSession = SparkSession.builder().appName("SparkPipelines").master("local[4]")
                .config("spark.ui.port", "9090")
                .getOrCreate();

        sqlContext = new SQLContext(sparkSession);

        lifecycle.addStopHook(() -> {
            sparkSession.stop();
            return CompletableFuture.completedFuture(null);
        });
    }

    public SparkSession getSparkSession() {
        return sparkSession;
    }

    public SQLContext getSqlContext() {
        return sqlContext;
    }
}
