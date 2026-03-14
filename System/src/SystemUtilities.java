import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemUtilities {
    public static void runUtilities(String name){
        try{
            ProcessBuilder processBuilder = new ProcessBuilder(name.split(" "));
            processBuilder.redirectErrorStream(true);
            Process process= processBuilder.start();

            BufferedReader reader= new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            process.waitFor();
        }catch (IOException | InterruptedException e) {
            JOptionPane.showMessageDialog(null, "Ошибка при запуске утилиты: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
