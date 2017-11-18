package weka.dataLoaders;


public class DataLoaderFactory {
    public IWekaDataLoader getDataLoader(String type){
        IWekaDataLoader dataLoader;
        switch (type){
            case "csv": dataLoader = new CSVDataLoader();
            break;
            default: dataLoader = new CSVDataLoader();
        }
        return dataLoader;
    }
}
