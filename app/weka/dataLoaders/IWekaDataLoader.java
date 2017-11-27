package weka.dataLoaders;

import interfaces.IDataLoader;
import weka.core.Instances;

import java.io.IOException;

public interface IWekaDataLoader extends IDataLoader<Instances>{
    @Override
    Instances loadData(String path) throws IOException;
}
