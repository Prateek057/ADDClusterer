package weka.Clusterers;

import weka.clusterers.SimpleKMeans;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;

public class SimpleKMeansClusterer {
    private SimpleKMeans simpleKMeans;
    private DistanceFunction distanceFunction;

    public void init(){
        simpleKMeans = new SimpleKMeans();
        distanceFunction = new EuclideanDistance();
        //simpleKMeans.setDistanceFunction(distanceFunction);

    }

}
