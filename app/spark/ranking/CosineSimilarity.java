package spark.ranking;

import org.apache.spark.ml.linalg.Vector;

public class CosineSimilarity implements IRankingStrategy{

    public Double getSimilarity(Vector v1, Vector v2){
        if(v1.size() == v2.size()){
            return dot(v1, v2) / (magnitude(v1) * magnitude(v2));
        }
        return 0.0;
    }

    private double dot(Vector v1, Vector v2) {
        if (v1.size() != v2.size())
            throw new IllegalArgumentException("dimensions disagree");
        double sum = 0.0;
        for (int i = 0; i < v1.size(); i++)
            sum = sum + (v1.apply(i) * v2.apply(i));
        return sum;
    }

    // return the Euclidean norm of this Vector
    private double magnitude(Vector v1) {
        return Math.sqrt(dot(v1, v1));
    }
}
