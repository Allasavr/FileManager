import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchFiles {

    public static void searchForFiles() {
        String query = JOptionPane.showInputDialog("Введите название файла или папки:");
        if (query != null && !query.isEmpty()) {
            List<File> results = searchFiles(new File(Main.systemPath), query);
            if(!results.isEmpty()) {
                StringBuilder message = new StringBuilder("Результаты поиска:\n");
                for (File file : results) {
                    message.append(file.getAbsolutePath()).append("\n");
                }
                JOptionPane.showMessageDialog(null, message.toString(), "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);
            } else
            {JOptionPane.showMessageDialog(null, "По вашему запросу файлов в системе не обнаружено",
                    "Результаты поиска", JOptionPane.INFORMATION_MESSAGE);}
        }
    }

    private static List<File> searchFiles(File directory, String query) {
        List<File> results = new ArrayList<>();
        File[] files = directory.listFiles();
        if (directory.isDirectory()) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().contains(query)) {
                        results.add(file); // Добавляем папку в результаты
                    }
                    results.addAll(searchFiles(file, query)); // Рекурсия
                } else if (file.getName().contains(query)) {
                    results.add(file);
                }
            }
        }
        return results;
    }
}
