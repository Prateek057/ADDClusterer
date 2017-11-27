package weka.clusterers;

import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;

public class SimpleKMeansClusterer {
    private SimpleKMeans simpleKMeans;

    public void init(){
        simpleKMeans = new SimpleKMeans();
        DistanceFunction distanceFunction = new EuclideanDistance();
        try {
            simpleKMeans.setDistanceFunction(distanceFunction);
            simpleKMeans.setNumClusters(20);
            simpleKMeans.setMaxIterations(10);
            simpleKMeans.setSeed(1);
            simpleKMeans.setDisplayStdDevs(true);
            simpleKMeans.setPreserveInstancesOrder(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[] getClusterAssignments(SimpleKMeans kmeans) throws Exception {
        return kmeans.getAssignments();
    }

    public AbstractClusterer get() {
        init();
        return simpleKMeans;
    }

}
