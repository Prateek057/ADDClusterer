package dl4j;

import org.deeplearning4j.clustering.cluster.Cluster;
import org.deeplearning4j.clustering.cluster.ClusterSet;
import org.deeplearning4j.clustering.cluster.Point;
import org.deeplearning4j.clustering.kmeans.KMeansClustering;
import org.deeplearning4j.eval.curves.PrecisionRecallCurve;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.documentiterator.LabelsSource;
import org.deeplearning4j.text.sentenceiterator.LineSentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExampleDL4JKmeans {

    public static void clusterDocuments(){
        File inputFile = new File("myresources/datasets/tasksNoHeader.csv");
        SentenceIterator sentenceIterator = new LineSentenceIterator(inputFile);

        LabelsSource source = new LabelsSource("DOC_");

        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());


        System.out.println("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(0)
                .iterations(5)
                .layerSize(100)
                .learningRate(0.025)
                .seed(42)
                .windowSize(5)
                .iterate(sentenceIterator)
                .tokenizerFactory(t)
                .build();

        System.out.println("Fitting Word2Vec model....");
        vec.fit();

        List<INDArray> vectors = new ArrayList<INDArray>();

        for(String word: vec.vocab().words()){
            vectors.add(vec.getWordVectorMatrix(word));
        }

        List<Point> pointList = Point.toPoints(vectors);
        System.out.println(pointList.size());

        KMeansClustering kMeansClustering = KMeansClustering.setup(20, 10, "cosinesimilarity", false );
        ClusterSet cs = kMeansClustering.applyTo(pointList);
        List<Cluster> clsterLst = cs.getClusters();

        //Map<String, String> pointDistribution = cs.getPointDistribution();
        //pointDistribution.forEach((k,v) -> System.out.println(k + "," + v ));

        for(Cluster c: clsterLst) {
            Point center = c.getCenter();
            System.out.println(center.getId());
            int clusterPoints = c.getPoints().size();
            System.out.println(clusterPoints);
        }

    }


}
