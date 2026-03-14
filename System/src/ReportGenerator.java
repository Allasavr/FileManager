import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportGenerator {
    public static void reportGenerate(List<TrackedProcess> processes, String filename){
        String name=Main.DOCUMENTS_DIR + "/" + filename;
        try(BufferedWriter writer= new BufferedWriter(new FileWriter(name))){
            for(TrackedProcess process: processes){
                writer.write(process.toString());
                writer.newLine();
            }
            JOptionPane.showMessageDialog(null, "Отчёт успешно сохранён", "Успех", JOptionPane.PLAIN_MESSAGE);
            SystemMethods.refreshDirectoryNode(new File(Main.DOCUMENTS_DIR));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при сохранении отчёта", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
