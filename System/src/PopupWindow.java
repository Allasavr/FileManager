import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class PopupWindow {

    public static JFrame popupFrame;
    public static JTextArea textArea;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public PopupWindow() {
        // Создаем окно
        popupFrame = new JFrame("Счетчик дескрипторов");
        popupFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        popupFrame.setSize(400, 200);

        // Создаем текстовую область
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        // Добавляем текстовую область в JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        popupFrame.add(scrollPane, BorderLayout.CENTER); // Размещаем текстовую область в центре

        // Создаем кнопку
        JButton logButton = new JButton("Логировать отчёт");
        logButton.setFont(new Font("Arial", Font.BOLD, 14));
        logButton.addActionListener(e -> {
            saveContentToFile();
        });

        // Добавляем кнопку в нижнюю часть окна
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Центрируем кнопку
        buttonPanel.add(logButton);
        popupFrame.add(buttonPanel, BorderLayout.SOUTH); // Размещаем кнопку внизу

        // Делаем окно видимым
        popupFrame.setVisible(true);

        // Запускаем поток для получения сообщений из очереди
        startMessageReceiverThread();
    }

    private void saveContentToFile() {
        // Получаем содержимое текстовой области
        String content = textArea.getText();
        if (content == null || content.isEmpty()) {
            JOptionPane.showMessageDialog(popupFrame, "Текстовая область пуста. Нечего сохранять.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Определяем путь к папке "Документы"
        File documentsDir = new File(Main.DOCUMENTS_DIR);

        // Проверяем, существует ли папка "Документы"
        if (!documentsDir.exists() || !documentsDir.isDirectory()) {
            JOptionPane.showMessageDialog(popupFrame, "Папка 'Документы' не найдена.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Запрашиваем имя файла у пользователя
        String fileName = JOptionPane.showInputDialog(popupFrame, "Введите имя файла:", "Сохранение файла", JOptionPane.PLAIN_MESSAGE);
        if (fileName == null || fileName.isEmpty()) {
            JOptionPane.showMessageDialog(popupFrame, "Имя файла не может быть пустым.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Формируем полный путь к файлу
        File file = new File(documentsDir, fileName + ".txt");

        // Записываем содержимое в файл
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            JOptionPane.showMessageDialog(popupFrame, "Файл успешно сохранен: " + file.getAbsolutePath(), "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(popupFrame, "Не удалось сохранить файл: " + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startMessageReceiverThread() {
        new Thread(() -> {
            while (running.get()) {
                try {
                    // Получаем сообщение из очереди
                    String receivedMessage = MessageQueue.receive();

                    if (receivedMessage != null && !receivedMessage.isEmpty()) {
                        // Обновляем текстовое поле в Event Dispatch Thread
                        SwingUtilities.invokeLater(() -> {
                            textArea.append(receivedMessage + "\n");
                            textArea.setCaretPosition(textArea.getDocument().getLength()); // Авто-скроллинг
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}