package rm;

import com.rapidminer.*;
import com.rapidminer.Process;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.nio.ExcelExampleSource;
import com.rapidminer.operator.OperatorDescription;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CSVDataLoader {

    public static void rmCSVLoader(){
        try {
            RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);
            RapidMiner.init();
            Process process = new Process(new File("D:\\TUM\\Master Thesis\\RapidMiner\\NewProcess.rpm"));
            Operator op = process.getOperator("Read Excel");
            op.setParameter(ExcelExampleSource.PARAMETER_EXCEL_FILE, "D:\\TUM\\Master Thesis\\DataSets\\Task.xls");
            process.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
