package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import java.util.ArrayList;

@Entity("cluster-pipelines")
public class ClusterPipeline  extends PersistentEntity{

    @Id
    private ObjectId _id;

    private String href;
    private String name;
    private String library;
    private String algorithm;
    private ArrayList<String> preprocessors;
    private String transformer;

    public ClusterPipeline(String href, String name, String library, String algorithm, String transformer, ArrayList<String> preprocessors){
        this.name = name;
        this.href = href;
        this.library = library;
        this.algorithm = algorithm;
        this.preprocessors = preprocessors;
        this.transformer = transformer;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public ArrayList<String> getPreprocessors() {
        return preprocessors;
    }

    public void setPreprocessors(ArrayList<String> preprocessors) {
        this.preprocessors = preprocessors;
    }

    public String getTransformer() {
        return transformer;
    }

    public void setTransformer(String transformer) {
        this.transformer = transformer;
    }
}
