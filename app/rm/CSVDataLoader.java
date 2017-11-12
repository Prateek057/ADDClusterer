package rm;

import com.rapidminer.*;
import com.rapidminer.Process;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.ToolbarGUIStartupListener;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.nio.ExcelExampleSource;
import com.rapidminer.operator.OperatorDescription;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CSVDataLoader {

    public static void rmCSVLoader(){
        try {
            //RapidMinerGUI.registerStartupListener(new ToolbarGUIStartupListener());
            //RapidMinerGUI.main(new String[] {});
            //RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);
            //RapidMiner.init();
            //Process process = new Process(new File("D:\\TUM\\Master Thesis\\RapidMiner\\processDocuments.rpm"));
            //Operator op = process.getOperator("Read Excel");
            //op.setParameter(ExcelExampleSource.PARAMETER_EXCEL_FILE, "D:\\TUM\\Master Thesis\\DataSets\\Task.xls");
            //process.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}
