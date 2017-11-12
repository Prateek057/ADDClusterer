package weka.Clusterers;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.*;

public class ClusterFactory {

    public AbstractClusterer get(String classifierName) {
        AbstractClusterer clusterer;
        switch (classifierName) {
            case "KMeans":
                clusterer = new SimpleKMeans();
                break;
            default:
                clusterer = getDefaultClusterer();
        }
        return clusterer;
    }

    public AbstractClusterer getDefaultClusterer(){
        return new SimpleKMeans();
    }
}
