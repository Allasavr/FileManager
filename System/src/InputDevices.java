import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.nio.file.*;

public class InputDevices {
    private static boolean isUSBAdded=false;

    public static void checkAndAddUSBDrive(){
        Path usbPath=findUsbPath();
        if(usbPath!=null) {
            try {
                String usbname = usbPath.getFileName().toString();
                if (!isUSBAdded) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(usbname);
                    SystemMethods.addFilesToNode(node, usbPath.toFile());
                    // Добавляем узел флешки в корневой узел дерева
                    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) FileManager.treeModel.getRoot();
                    rootNode.add(node);

                    // Обновляем модель дерева
                    FileManager.treeModel.reload();
                    System.out.println("Флешка '" + usbname + "' успешно добавлена.");

                    // Устанавливаем флаг
                    isUSBAdded = true;
                } else {
                    System.out.println("Флешка уже добавлена.");
                }
                } catch(Exception ex){
                    JOptionPane.showMessageDialog(null, "Ошибка при добавлении флешки: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
        } else {
            System.out.println("Флешка не найдена.");
        }
    }

    private static Path findUsbPath() {
        Path mediaPath=Paths.get("/media/alla");
        if (Files.exists(mediaPath) && Files.isDirectory(mediaPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(mediaPath)) {
                for (Path path : stream) {
                    if (Files.isDirectory(path)) {
                        System.out.println("Найдено съёмное устройство: " + path);
                        return path; // Возвращаем первый найденный путь
                    }
                }
            } catch (IOException ex) {
                System.err.println("Ошибка при поиске съёмных устройств: " + ex.getMessage());
            }
        }
        return null; // Если съёмные устройства не найдены
    }
}


