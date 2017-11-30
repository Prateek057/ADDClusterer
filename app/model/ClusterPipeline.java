package model;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.util.ArrayList;
import java.util.List;


@Entity("cluster-pipelines")
public class ClusterPipeline  extends PersistentEntity{



    @Id
    private ObjectId _id;

    private String href;

    @Indexed(name="pipeline_name", options = @IndexOptions(unique = true))
    private String name;

    private String library;
    private Algorithm algorithm;
    private ArrayList<String> preprocessors;
    private String transformer;
    private String dataset;
    private SCTypeEntity type;
    private List<String> miningAttributes;

    public ClusterPipeline(){

    }

    public ClusterPipeline(String href, String name, String library, Algorithm algorithm, String transformer, String dataset){
        this.name = name;
        this.href = href;
        this.library = library;
        this.algorithm = algorithm;
        this.transformer = transformer;
        this.dataset = dataset;
    }

    public ClusterPipeline(String href, String name, String library, Algorithm algorithm, String transformer, String dataset, SCTypeEntity type, List<String> miningAttributes){
        this.name = name;
        this.href = href;
        this.library = library;
        this.algorithm = algorithm;
        this.transformer = transformer;
        this.dataset = dataset;
        this.type = type;
        this.miningAttributes = miningAttributes;
    }

    public ClusterPipeline(String href, String name, String library, Algorithm algorithm, String transformer, String dataset, ArrayList<String> preprocessors){
        this.name = name;
        this.href = href;
        this.library = library;
        this.algorithm = algorithm;
        this.preprocessors = preprocessors;
        this.transformer = transformer;
        this.dataset = dataset;
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

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
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

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public SCTypeEntity getType() {
        return type;
    }

    public void setType(SCTypeEntity type) {
        this.type = type;
    }

    public List<String> getMiningAttributes() {
        return miningAttributes;
    }

    public void setMiningAttributes(List<String> miningAttributes) {
        this.miningAttributes = miningAttributes;
    }
}
