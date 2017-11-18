package spark.dataloaders;

public class DataLoaderFactory {
    public ISparkDataLoader getDataLoader(String type){
        ISparkDataLoader dataLoader;
        switch (type){
            case "csv": dataLoader = new CSVDataLoader();
            break;
            default: dataLoader = new CSVDataLoader();
        }
        return dataLoader;
    }
}
