import javax.swing.tree.TreePath;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileManager {
    public static DefaultTreeModel treeModel; // Модель дерева файлов
    public static JTree fileTree; // Дерево файлов
    public static JFrame frame;
    private static SystemMethods systemMethods;
    public static JScrollPane scrollPane;
    public static JPanel panel;
    public static JTextArea terminalOutput;
    public static JTextField commandInput;

    public FileManager() {
        systemMethods = new SystemMethods();
        // Создаем экземпляр главного окна
        frame = new JFrame("Суперапп");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Создаем панель для контента
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        frame.add(panel);

        fileTree = systemMethods.createFileTree();


        // Добавляем дерево в скроллпанель
        scrollPane = new JScrollPane(fileTree);
        panel.add(scrollPane, BorderLayout.CENTER);


        // Создаем главное меню
        JMenuBar menubar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem delete = new JMenuItem("Удалить");
        delete.setAccelerator(KeyStroke.getKeyStroke("DELETE"));
        delete.addActionListener(e -> trashMover());
        fileMenu.add(delete);

        JMenu createItemM = new JMenu("Создать");
        fileMenu.add(createItemM);
        JMenuItem fileItemM = new JMenuItem("Файл");
        fileItemM.addActionListener(e -> createFile());
        createItemM.add(fileItemM);
        JMenuItem folderItemM = new JMenuItem("Папкy");
        folderItemM.addActionListener(e -> createFolder());
        createItemM.add(folderItemM);

        JMenuItem reportItem = new JMenuItem("Сохранить отчёт");
        reportItem.addActionListener(e -> {
            String name=JOptionPane.showInputDialog("Введите имя отчёта: ");
            if (!name.isEmpty()){
                ReportGenerator.reportGenerate(ProcessCheck.getTrackedProcesses(), name);
            }
        });
        fileMenu.add(reportItem);

        fileMenu.addSeparator();
        JMenuItem exitProgram = new JMenuItem("Выход");
        exitProgram.setAccelerator(KeyStroke.getKeyStroke("shift E"));
        exitProgram.addActionListener(e -> exitWindow());
        fileMenu.add(exitProgram);

        JMenu editMenu = new JMenu("  Правка  ");

        JMenuItem copyFile = new JMenuItem("Копировать");
        copyFile.setAccelerator(KeyStroke.getKeyStroke("shift C"));
        copyFile.addActionListener(e -> copyFiles());
        editMenu.add(copyFile);
        JMenuItem pasteFile = new JMenuItem("Вставить");
        pasteFile.setAccelerator(KeyStroke.getKeyStroke("shift V"));
        pasteFile.addActionListener(e -> pasteFiles());
        editMenu.add(pasteFile);
        JMenuItem copypasteFile = new JMenuItem("Копировать в...");
        copypasteFile.setAccelerator(KeyStroke.getKeyStroke("shift A"));
        copypasteFile.addActionListener(e -> copypasteFiles());
        editMenu.add(copypasteFile);

        // Меню "Справка"
        JMenu helpMenu = new JMenu("Справка");
        JMenuItem aboutItem = new JMenuItem("О программе");
        aboutItem.addActionListener(e -> helpAction());
        helpMenu.add(aboutItem);
        JMenuItem keyItem = new JMenuItem("Горячие клавиши");
        keyItem.addActionListener(e -> helpHotKeyAction());
        helpMenu.add(keyItem);


        JMenu fileSearch = new JMenu(" Поиск ");
        JMenuItem searchItem = new JMenuItem("Поиск файлов");
        searchItem.setAccelerator(KeyStroke.getKeyStroke("shift F"));
        searchItem.addActionListener(e -> SearchFiles.searchForFiles());
        fileSearch.add(searchItem);


        JMenu utilits = new JMenu("Cистемные утилиты");
        JMenuItem terminalItem = new JMenuItem("Терминал");
        terminalItem.setAccelerator(KeyStroke.getKeyStroke("shift T"));
        terminalItem.addActionListener(e -> new TerminalLinux());
        utilits.add(terminalItem);
        JMenuItem recurcelItem = new JMenuItem("Монитор ресурсов");
        recurcelItem.addActionListener(e -> SystemUtilities.runUtilities("gnome-system-monitor"));
        utilits.add(recurcelItem);

        JMenuItem diskManagerItem = new JMenuItem("Управление дисками");
        diskManagerItem.addActionListener(e -> SystemUtilities.runUtilities("gnome-disks"));
        utilits.add(diskManagerItem);

        JMenuItem networkSettingsItem = new JMenuItem("Настройки сети");
        networkSettingsItem.addActionListener(e -> SystemUtilities.runUtilities("nm-connection-editor"));
        utilits.add(networkSettingsItem);

        JMenu popWindow = new JMenu("Доп.Окно");
        JMenuItem popWindowItem = new JMenuItem("Запуск");
        popWindowItem.setAccelerator(KeyStroke.getKeyStroke("shift D"));
        popWindowItem.addActionListener(e -> launchPopupWindow()); // Вызываем метод
        popWindow.add(popWindowItem);

        fileTree.addTreeSelectionListener(e -> SystemMethods.updateMainMenuState(copyFile, pasteFile, delete, copypasteFile, createItemM));

        menubar.add(fileMenu);
        menubar.add(editMenu);
        menubar.add(helpMenu);
        menubar.add(fileSearch);
        menubar.add(utilits);
        menubar.add(popWindow);

        // Создаем контекстное меню
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Копировать");
        copyItem.addActionListener(e -> copyFiles());
        contextMenu.add(copyItem);
        JMenuItem pasteItem = new JMenuItem("Вставить");
        pasteItem.addActionListener(e -> pasteFiles());
        contextMenu.add(pasteItem);

        contextMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.addActionListener(e -> trashMover());
        contextMenu.add(deleteItem);
        JMenu createItem = new JMenu("Создать");
        contextMenu.add(createItem);
        JMenuItem fileItem = new JMenuItem("Файл");
        fileItem.addActionListener(e -> createFile());
        createItem.add(fileItem);
        JMenuItem folderItem = new JMenuItem("Папкy");
        folderItem.addActionListener(e -> createFolder());
        createItem.add(folderItem);

        contextMenu.addSeparator();
        JMenuItem moveItem = new JMenuItem("Переместить в...");
        moveItem.addActionListener(e -> moveFiles());
        contextMenu.add(moveItem);
        JMenuItem movecutItem = new JMenuItem("Копировать в...");
        movecutItem.addActionListener(e -> copypasteFiles());
        contextMenu.add(movecutItem);
        JMenuItem renameItem = new JMenuItem("Переименовать");
        renameItem.addActionListener(e -> renameFiles());
        contextMenu.add(renameItem);
        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TreePath path = fileTree.getPathForLocation(e.getX(), e.getY());
                if (path == null) {
                    return;
                }

                DefaultMutableTreeNode nodeS = (DefaultMutableTreeNode) path.getLastPathComponent();
                String nodeNameS = nodeS.getUserObject().toString();

                if (nodeNameS.equals("System")) {
                    JOptionPane.showMessageDialog(null, "Папка 'System' защищена и не может быть изменена",
                            "Предупреждение", JOptionPane.WARNING_MESSAGE);
                    e.consume(); // Игнорируем событие
                    return;
                }

                if (e.isPopupTrigger()) {
                    int row = fileTree.getRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        fileTree.setSelectionRow(row); // Выбираем узел под курсором
                    }
                    TreePath selectedPath = fileTree.getSelectionPath();
                    if (selectedPath == null) {
                        return;
                    }

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                    String nodeName = node.getUserObject().toString();
                    File selectedFile = new File(SystemMethods.getPathFromNode(node));

                    for (Component component : contextMenu.getComponents()) {
                        if (component instanceof JMenuItem && "Очистить Корзину".equals(((JMenuItem) component).getText())) {
                            contextMenu.remove(component);
                            break;
                        }
                    }
                    if (selectedFile.isDirectory() && nodeName.equals("Корзина")) {
                        JMenuItem clearItem = new JMenuItem("Очистить Корзину");
                        clearItem.addActionListener(e1 -> clearTrashFolder());
                        contextMenu.add(clearItem);
                    }

                    if(selectedFile.isDirectory())  {
                        if(nodeName.equals("Корзина") || nodeName.equals("Изображения") || nodeName.equals("Документы")) {
                            deleteItem.setEnabled(false);
                            movecutItem.setEnabled(false);
                            copyItem.setEnabled(false);
                            renameItem.setEnabled(false);
                        }
                        else{
                            deleteItem.setEnabled(true);
                            movecutItem.setEnabled(true);
                            copyItem.setEnabled(false);
                            renameItem.setEnabled(true);
                        }
                    }
                    else{
                        movecutItem.setEnabled(false);
                        copyItem.setEnabled(true);
                        renameItem.setEnabled(true);
                        deleteItem.setEnabled(true);
                    }
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = fileTree.getRowForLocation(e.getX(), e.getY());
                    if (row != -1) {
                        fileTree.setSelectionRow(row); // Выбираем узел под курсором
                    }

                    // Отображаем контекстное меню
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

        });

        startUSBMonitoring();
        startMonitoring();

        DragDrop.fromManagerToSystem();
        DragDrop.fromSystemToManager();

        frame.setFocusable(true);
        frame.requestFocus();
        frame.setJMenuBar(menubar);
        frame.setVisible(true);

    }

    public void launchPopupWindow() {
        try {
            // Шаг 1: Получение PID процесса
            String pidInput = JOptionPane.showInputDialog("Введите PID процесса:");
            if (pidInput == null || pidInput.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PID не введен.");
                return;
            }

            int pid = Integer.parseInt(pidInput);

            // Шаг 2: Создаем фоновый поток для выполнения длительных операций
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    // Шаг 3: Получение времени работы процесса
                    String uptime = ProcessInfo.getProcessUptime(pid);

                    int descriptorCount = ProcessInfo.getDescriptorCount(pid);
                    MessageQueue.send("Количество дескрипторов: " + descriptorCount);

                    // Отправляем время работы процесса через очередь сообщений
                    MessageQueue.send("Время работы процесса: " + uptime);

                    // Шаг 4: Отправляем информацию о потоках
                    sendThreadInfo(pid);

                    // Шаг 5: Запуск всплывающего окна напрямую
                    SwingUtilities.invokeLater(() -> new PopupWindow());
                    return null;
                }
            }.execute();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Неверный формат PID.");
        }
    }

    public void sendThreadInfo(int pid) {
        String threadInfo = ProcessInfo.getThreadInfo(pid);

        // Разделяем данные на строки
        String[] lines = threadInfo.split("\n");
        int chunkSize = 10; // Количество потоков в одной части
        int totalChunks = (int) Math.ceil((double) lines.length / chunkSize);

        for (int i = 0; i < totalChunks; i++) {
            StringBuilder chunk = new StringBuilder();

            // Добавляем метку начала/конца
            if (i == 0) {
                chunk.append("Потоки:\n");
            }

            // Добавляем строки текущей части
            for (int j = i * chunkSize; j < Math.min((i + 1) * chunkSize, lines.length); j++) {
                chunk.append(lines[j]).append("\n");
            }

            // Отправляем часть через очередь сообщений
            MessageQueue.send(chunk.toString());
        }
    }

    public static void startMonitoring() {
        Thread monitorThread = new Thread(() -> {
            while (true) {
                try {
                    ProcessCheck.trackProcess();
                    Thread.sleep(5000); // Проверка каждые 5 секунд
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    private void startUSBMonitoring() {
        Thread usbMonitorThread = new Thread(() -> {
            while (true) {
                try {
                    InputDevices.checkAndAddUSBDrive();
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        usbMonitorThread.setDaemon(true);
        usbMonitorThread.start();
    }
    private void renameFiles() {
        // Получаем выбранный узел в дереве файлов
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(null, "Выберите файл или папку для переименования.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Получаем файл/папку, связанную с выбранным узлом
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        String fileName = node.getUserObject().toString();
        // Получаем полный путь к выбранному файлу/папке
        File sourceUnit = new File(SystemMethods.getPathFromNode(node));

        if (!sourceUnit.exists()) {
            JOptionPane.showMessageDialog(null, "Файл/папка не найдена: " + fileName, "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Запрашиваем у пользователя новое имя
        String renameName = JOptionPane.showInputDialog(frame, "Задайте новое имя файлу/папке:", "Переименование файла/папки", JOptionPane.PLAIN_MESSAGE);
        if (renameName == null || renameName.isEmpty()) return;
        if (renameName.contains("\\") || renameName.contains("/") || renameName.contains("*") ||  renameName.contains("System")){
            JOptionPane.showMessageDialog(null, "Недопустимое имя файла/папки.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Формируем новый путь
            Path sourcePath = sourceUnit.toPath();
            Path targetPath = sourceUnit.getParentFile().toPath().resolve(renameName);

            // Переименовываем файл/папку
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Обновляем дерево файлов
            systemMethods.refreshDirectoryNode(targetPath.getParent().toFile());

            // Уведомляем пользователя об успехе
            JOptionPane.showMessageDialog(null, "Файл/папка успешно переименован(а): " + targetPath, "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при переименовании: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copypasteFiles() {
        // Получаем выбранный узел в дереве файлов
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(null, "Выберите файл для копирования.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Получаем файл, связанный с выбранным узлом
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        String fileName = node.getUserObject().toString();
        File sourceFolder = systemMethods.findFileInAllDirectories(fileName);

        if (sourceFolder == null || !sourceFolder.exists()) {
            JOptionPane.showMessageDialog(null, "Папка не найдена: " + fileName, "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String targetfolderName = JOptionPane.showInputDialog(frame, "Задайте имя папке:", "Копирование папки", JOptionPane.PLAIN_MESSAGE);
        if (targetfolderName == null || targetfolderName.isEmpty()) {
            return;
        }

        File root_dir=new File(Main.systemPath);
        File targetFolder = SystemMethods.findDirectoryByName(root_dir, targetfolderName);

        if (targetFolder == null || !targetFolder.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Папка с именем \"" + targetfolderName + "\" не найдена.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (targetFolder.getAbsolutePath().contains("System")) {
            JOptionPane.showMessageDialog(null, "Папка 'System' защищена и не может быть изменена.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File targetFile = new File(targetFolder, sourceFolder.getName());
        SystemMethods.copyDirectory(sourceFolder, targetFile);
        SystemMethods.refreshDirectoryNode(targetFolder);
        JOptionPane.showMessageDialog(null, "Копирование выполнено успешно!", "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    private void moveFiles() {
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(null, "Выберите файл для перемещения", "Ошибка", JOptionPane.WARNING_MESSAGE);
        } else {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            File selectedFile = new File(systemMethods.getPathFromNode(node));

            if (!selectedFile.exists()) {
                JOptionPane.showMessageDialog(null, "Файл не найден: " + selectedFile.getAbsolutePath(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String folderName = JOptionPane.showInputDialog(frame, "Задайте имя папке:", "Перемещение файла/папки", JOptionPane.PLAIN_MESSAGE);
            if (folderName == null || folderName.isEmpty()) {
                return;
            }

            // Формируем объект File для целевой папки
            File targetFolder = new File(folderName);

            // Проверяем, существует ли целевая папка
            if (!targetFolder.exists() || !targetFolder.isDirectory()) {
                JOptionPane.showMessageDialog(null, "Целевая папка не существует: " + folderName, "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (targetFolder.getAbsolutePath().contains("System")) {
                JOptionPane.showMessageDialog(null, "Папка 'System' защищена и не может быть изменена.", "Предупреждение", JOptionPane.WARNING_MESSAGE);
                return;
            }

            File newfile = new File(folderName, selectedFile.getName());
            if (newfile.exists()) {
                JOptionPane.showMessageDialog(null, "Папка/файл с именем \"" + folderName + "\" уже существует.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Files.move(selectedFile.toPath(), newfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Ошибка при перемещении", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            SystemMethods.refreshDirectoryNode(new File(String.valueOf(newfile.getParentFile())));
            // Обновляем дерево файлов
            SystemMethods.refreshDirectoryNode(selectedFile.getParentFile());
        }
    }

    private void clearTrashFolder() {
        File trashDir=new File(Main.TRASH_DIR);
        File[] files=trashDir.listFiles();
        if(files==null){
            JOptionPane.showMessageDialog(null, "Корзина пуста", "Корзина", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        int result=JOptionPane.showConfirmDialog(frame, "Хотите очистить папку `Корзина`?", "Очистка", JOptionPane.YES_NO_OPTION);
        if (result==JOptionPane.YES_OPTION) {
            for(File file:files){
                if(file.isDirectory()) {
                    freshDirectory(file);
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Ошибка при удалении папки: " + file.getName(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "Ошибка при удалении файла: " + file.getName(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            SystemMethods.refreshDirectoryNode(trashDir);
            JOptionPane.showMessageDialog(null, "Корзина очищена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    private void createFile() {
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(null, "Выберите директорию для создания файла.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Получаем узел, связанный с выбранным элементом
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

        // Получаем полный путь к выбранному файлу
        String filePath = SystemMethods.getPathFromNode(node);
        File directory = new File(filePath);

        if(directory == null || !directory.isDirectory()){
            JOptionPane.showMessageDialog(null, "Файл не является папкой", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fileName =JOptionPane.showInputDialog(frame,"Задайте имя файлу:", "Создание файла", JOptionPane.PLAIN_MESSAGE);
        if (fileName ==null || fileName.isEmpty()){
            return;
        }

        File newfile=new File(directory, fileName);
        if(newfile.exists()) {
            JOptionPane.showMessageDialog(null, "Файл с именем \"" + fileName + "\" уже существует.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            newfile.createNewFile();
            JOptionPane.showMessageDialog(null, "Файл успешно создан: " + newfile.getAbsolutePath(), "Успех", JOptionPane.INFORMATION_MESSAGE);
            SystemMethods.refreshDirectoryNode(directory);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Не удалось создать файл: " + newfile.getAbsolutePath(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createFolder() {
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(null, "Выберите директорию для создания папки.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Получаем узел, связанный с выбранным элементом
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();

        // Получаем полный путь к выбранному файлу
        String directoryPath = SystemMethods.getPathFromNode(node);
        File directory = new File(directoryPath);

        if(directory ==null || !directory.isDirectory()){
            JOptionPane.showMessageDialog(null, "Файл не является папкой", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String folderName=JOptionPane.showInputDialog(frame,"Задайте имя папке:", "Создание папки", JOptionPane.PLAIN_MESSAGE);
        if (folderName==null || folderName.isEmpty()){
            return;
        }

        File newfolder=new File(directory, folderName);
        if(newfolder.exists()) {
            JOptionPane.showMessageDialog(null, "Папка с именем \"" + folderName + "\" уже существует.", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            newfolder.mkdir();
            JOptionPane.showMessageDialog(null, "Папка успешно создана: " + newfolder.getAbsolutePath(), "Успех", JOptionPane.INFORMATION_MESSAGE);
            SystemMethods.refreshDirectoryNode(directory);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Не удалось создать папку: " + newfolder.getAbsolutePath(), "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void exitWindow() {
        int result=JOptionPane.showConfirmDialog(frame, "Хотите закыть приложение?", "SuperApp", JOptionPane.YES_NO_OPTION);
        if (result==JOptionPane.YES_OPTION) {
            System.exit(0);

        }
    }

    private void freshDirectory(File filefordelete) {
        if (filefordelete == null || !filefordelete.exists()) {
            return;
        }
        File[] files = filefordelete.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при удалении файла: " + file.getName() + "\n" + ex.getMessage(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                } else if (file.isDirectory()) {
                    freshDirectory(file);
                    try {
                        Files.delete(file.toPath());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Ошибка при удалении папки: " + file.getName() + "\n" + ex.getMessage(),
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    private void copyFiles() {
        // Получаем выбранный узел в дереве файлов
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath == null) {
            JOptionPane.showMessageDialog(null, "Выберите файл для копирования.", "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Получаем файл, связанный с выбранным узлом
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
        String fileName = node.getUserObject().toString();
        File file = systemMethods.findFileInAllDirectories(fileName);

        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(null, "Файл не найден: " + fileName, "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            StringSelection stringSelection=new StringSelection(file.getAbsolutePath());
            Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
            // Устанавливаем данные в буфер обмена
            clipboard.setContents(stringSelection, null);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Ошибка при копировании: " + file.getAbsolutePath(), "Ошибка", JOptionPane.INFORMATION_MESSAGE);

        }

    }

    private void trashMover() {
        TreePath selectedPath = fileTree.getSelectionPath();
        if (selectedPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            File selectedFile = new File(systemMethods.getPathFromNode(node));

            try {
                if (!selectedFile.exists()) {
                    JOptionPane.showMessageDialog(null, "Файл не найден: " + selectedFile.getAbsolutePath(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Создаем папку "Корзина", если она не существует
                File trashDir = new File(Main.TRASH_DIR);
                if (!trashDir.exists()) {
                    trashDir.mkdirs();
                }
                // Перемещаем файл в корзину
                File trashFile = new File(trashDir, selectedFile.getName());
                Files.move(selectedFile.toPath(), trashFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Ошибка при удалении: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
            SystemMethods.refreshDirectoryNode(new File(Main.TRASH_DIR));
            // Обновляем дерево файлов
            SystemMethods.refreshDirectoryNode(selectedFile.getParentFile());
        } else {
            JOptionPane.showMessageDialog(null, "Выберите файл для удаления.", "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void pasteFiles() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String filepath = null;
            filepath = (String) clipboard.getData(DataFlavor.stringFlavor);
            File sourceFile = new File(filepath);


            TreePath selectedPath = fileTree.getSelectionPath();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            String targetName = node.getUserObject().toString();
            File targetDir = systemMethods.findFileInAllDirectories(targetName);

            if (targetDir == null || !targetDir.isDirectory()) {
                JOptionPane.showMessageDialog(null, "Файл не является папкой", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File file = new File(targetDir, sourceFile.getName());
            Files.copy(sourceFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            SystemMethods.refreshDirectoryNode(targetDir);
        } catch (IOException | UnsupportedFlavorException ex) {
            JOptionPane.showMessageDialog(null, "Буфер обмена не содержит пути к файлу.", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void helpAction() {
        JOptionPane.showMessageDialog(frame,
                "Суперапп\n" +
                        "Операционные системы и оболочки\n" +
                        "Язык программирования: Java\n" +
                        "Разработчик: Пекарева А.А., группа ПрИ-32",
                "О программе", JOptionPane.INFORMATION_MESSAGE);
    }

    private void helpHotKeyAction() {
        JOptionPane.showMessageDialog(frame,
                "DELETE - Переместить файл в папку `Корзина`\n" +
                        "Shift C - Копировать файл\n" +
                        "Shift V - Вставить файл\n" +
                        "Shift E - Завершить работу приложения\n" +
                        "Shift A - Копировать папку\n" +
                        "Shift F - Поик файлов в системе приложения\n" +
                        "Shift T - Открыть терминал\n" +
                        "Shift D - Запуск всплывающего окна\n",
                "Горячие клавиши", JOptionPane.INFORMATION_MESSAGE);
    }

}