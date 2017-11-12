package spark.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
    public static List<String> getFilesList(String path) {
        File folder = new File(path);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        };
        List<String> filenames = new ArrayList<>();
        File[] files = folder.listFiles(filter);

        if (null != files) {
            for (File file : files) {
                filenames.add(file.getName());
            }
        }
        return filenames;
    }
}
