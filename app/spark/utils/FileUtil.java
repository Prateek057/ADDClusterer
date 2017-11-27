package spark.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
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

    public static String constructCSVLineRecord(List<String> columnValues, String separator){
        StringBuilder sb = new StringBuilder();
        for(String column: columnValues){
            sb.append(column);
            if(columnValues.indexOf(column) == columnValues.size()-1){
                sb.append("\n");
            }
            else{
                sb.append(separator);
            }
        }
        return sb.toString();
    }

    public static String constructCSVRecords(List<String> rowValues){
        StringBuilder sb = new StringBuilder();
        for(String row: rowValues){
            sb.append(row);
        }
        return sb.toString();
    }

    public static File saveDataAsCSV(String path, StringBuilder content) throws FileNotFoundException {
        File file = new File(path);
        PrintWriter pw = new PrintWriter(file);
        pw.write(content.toString());
        pw.close();
        return file;
    }
}
