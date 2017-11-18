package weka.dataLoaders;

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import java.io.File;
import java.io.IOException;

public class CSVDataLoader implements IWekaDataLoader {

    public Instances loadData(String path) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setNoHeaderRowPresent(true);
        loader.setSource(new File(path));
        return loader.getDataSet();
    }
}
