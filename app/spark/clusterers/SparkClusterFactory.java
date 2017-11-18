package spark.clusterers;

import org.apache.spark.ml.clustering.BisectingKMeans;
import org.apache.spark.ml.clustering.KMeans;

public class SparkClusterFactory {



    public Object getClusterer(String clusterName){
        Object clusterer;
        switch(clusterName){
            case "spark-kmeans": clusterer = new KMeans()
                    .setK(20)
                    .setFeaturesCol("features")
                    .setPredictionCol("cluster_label")
                    .setMaxIter(20);
            break;
            case "spark-bi-kmeans": clusterer  = new BisectingKMeans()
                    .setK(20)
                    .setFeaturesCol("features")
                    .setPredictionCol("cluster_label")
                    .setMaxIter(20);
            break;
            default: clusterer = new KMeans()
                    .setK(20)
                    .setFeaturesCol("features")
                    .setPredictionCol("cluster_label")
                    .setMaxIter(20);
        }
        return clusterer;
    }

}
