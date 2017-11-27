package weka.clusterers;

public class ClusterFactory {

    public AbstractClusterer get(String classifierName) {
        AbstractClusterer clusterer;
        switch (classifierName) {
            case "KMeans":
                clusterer = new SimpleKMeansClusterer().get();
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
