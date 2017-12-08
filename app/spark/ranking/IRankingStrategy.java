package spark.ranking;
import org.apache.spark.ml.linalg.Vector;


public interface IRankingStrategy {
    //applyRanking()
    Double getSimilarity(Vector v1, Vector V2);
}
