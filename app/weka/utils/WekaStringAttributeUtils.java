package weka.utils;

import weka.core.Attribute;
import weka.core.Instances;

import java.util.List;

public class WekaStringAttributeUtils {
    public static Instances concatStringTypeAttributes(Instances data){
        int numOfrecords = data.numInstances();
        int documentAttributeIndex = data.numAttributes();
        data.insertAttributeAt(new Attribute("document", (List<String>) null), data.numAttributes());
        for(int i=0;i<numOfrecords;i++){
            StringBuilder document = new StringBuilder();
            for(int j=0;j<documentAttributeIndex;j++)
                document.append(data.instance(i).stringValue(j));

            data.instance(i).setValue(documentAttributeIndex, document.toString());
        }

        return data;
    }
}
