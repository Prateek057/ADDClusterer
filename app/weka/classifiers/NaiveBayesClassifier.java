package weka.classifiers;

import weka.classifiers.bayes.NaiveBayes;

/**
 * Created by Manoj on 7/12/2017.
 */
public class NaiveBayesClassifier {
    private NaiveBayes nb;

    void init() {
        nb = new NaiveBayes();
        nb.setNumDecimalPlaces(2);
    }

    public AbstractClassifier get() {
        init();
        return nb;
    }
}
