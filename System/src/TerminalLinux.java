import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;

public class TerminalLinux {
    private JFrame terminalFrame;
    private JTextArea terminalOutput;
    private JTextField commandInput;

    public TerminalLinux() {
        // Создаем окно терминала
        terminalFrame = new JFrame("Терминал Linux");
        terminalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        terminalFrame.setSize(600, 500);

        // Создаем текстовую область для вывода команд
        terminalOutput = new JTextArea();
        terminalOutput.setEditable(false);
        terminalOutput.setBackground(Color.BLACK);
        terminalOutput.setForeground(Color.GREEN);
        terminalOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane outputScrollPane = new JScrollPane(terminalOutput);

        commandInput = new JTextField();
        commandInput.setBackground(Color.BLACK);
        commandInput.setForeground(Color.YELLOW);
        commandInput.addActionListener(e -> executeCommand(commandInput.getText()));

        // Создаем панель для размещения компонентов
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(outputScrollPane, BorderLayout.CENTER);
        mainPanel.add(commandInput, BorderLayout.SOUTH);

        // Добавляем панель в окно
        terminalFrame.add(mainPanel);
        // Отображаем окно
        terminalFrame.setVisible(true);

        terminalOutput.append("Добро пожаловать в терминал Linux!\n");
        terminalOutput.append("Введите 'help' для просмотра доступных команд.\n");
    }

    public void executeCommand(String command) {
        if (command==null || command.trim().isEmpty()) return;

        commandInput.setText("");

        terminalOutput.append("> " + command + "\n");

        String[] parts=command.trim().split(" ");
        String cmd=parts[0];
        try {
            switch (cmd) {
                case "help":
                    showHelp();
                    break;
                case "ps":
                    listProcesses();
                    break;
                case "lsusb":
                    listUSBDevices();
                    break;
                case "lspci":
                    listPCIDevices();
                    break;
                case "pidof":
                    if (parts.length > 1 && parts.length<3) findPDIname(parts[1]);
                    else terminalOutput.append("Использование: pidof <имя>\n");
                    break;
                case "kill":
                    if (parts.length > 1 && parts.length<3) killProcess(parts[1]);
                    else terminalOutput.append("Использование: kill <PID>\n");
                    break;
                case "killall":
                    if (parts.length > 1 && parts.length<3) killallProcesses(parts[1]);
                    else terminalOutput.append("Использование: killall <имя>\n");
                    break;
                case "uptime":
                    showUptime();
                    break;
                case "nice":
                    if (parts.length > 2 && parts.length<4) runWithPriority(parts);
                    else terminalOutput.append("Использование: nice <приоритет> <команда>\n");
                    break;
                case "renice":
                    if (parts.length > 2 && parts.length<4) changePriority(parts[1], parts[2]);
                    else terminalOutput.append("Использование: renice <приоритет> <PID>\n");
                    break;
                case "sth":
                    if (parts.length > 1 && parts.length<3) saveTerminalHistory(parts[1]);
                    else terminalOutput.append("Использование: sth <имя>\n");
                    break;
                default:
                    terminalOutput.append("Команды " + command + " нет в системе" + "\n");
                    break;
            }
        }  catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void saveTerminalHistory(String filename) {
        String name=Main.DOCUMENTS_DIR + "/" + filename;
        String text=terminalOutput.getText();
        try {
            File file = new File(name);
            Files.write(file.toPath(), text.getBytes());
            SystemMethods.refreshDirectoryNode(new File(Main.DOCUMENTS_DIR));
            terminalOutput.append("Текст успешно сохранен в файл: " + file.getAbsolutePath() + "\n");
        } catch (Exception e) {
            terminalOutput.append("Ошибка при сохранении файла: " + e.getMessage() + "\n");
        }
    }

    private void runWithPriority(String[] args) {
        try {
            String priority = args[1];
            String command = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            Runtime.getRuntime().exec("nice -n " + priority + " " + command);
            terminalOutput.append("Процесс запущен с приоритетом " + priority + ".\n");
        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void showUptime() {
        try {
            Process process = Runtime.getRuntime().exec("uptime");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                terminalOutput.append(line + "\n");
            }
        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void killallProcesses(String name) {
        try {
            Runtime.getRuntime().exec("killall " + name);
            terminalOutput.append("Все процессы с именем " + name + " завершены.\n");
        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void listUSBDevices() {
        try {
            Process process = Runtime.getRuntime().exec("lsusb");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                terminalOutput.append(line + "\n");
            }
        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void listPCIDevices() {

        try {
            Process process = Runtime.getRuntime().exec("lspci");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                terminalOutput.append(line + "\n");
            }

        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void listProcesses() {
        try {
            Process process = Runtime.getRuntime().exec("ps -e");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                terminalOutput.append(line + "\n");
            }

        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void findPDIname(String name){
        try {
            Process process = Runtime.getRuntime().exec("pidof " + name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] pids = line.split("\\s+");
                for (String pid : pids) {
                    terminalOutput.append(name + " PID: " + pid + "\n");
                }
            }
        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void killProcess(String pid) {
        try {
            Runtime.getRuntime().exec("kill " + pid);
            terminalOutput.append("Процесс с PID " + pid + " завершен.\n");
        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void changePriority(String priority, String pid) {
        try {
            Runtime.getRuntime().exec("renice " + priority + " -p " + pid);
            terminalOutput.append("Приоритет процесса с PID " + pid + " изменен на " + priority + ".\n");
        } catch (Exception e) {
            terminalOutput.append("Ошибка: " + e.getMessage() + "\n");
        }
    }

    private void showHelp(){
        terminalOutput.append("""
            Доступные команды:
            ps - Вывести список процессов
            kill <PID> - Остановить процесс по PID
            nice <приоритет> <команда> - Запустить процесс с указанным приоритетом
            renice <приоритет> <PID> - Изменить приоритет процесса
            uptime - Показать время работы системы
            pidof <имя> - Найти PID процесса по имени
            killall <имя> - Остановить все процессы с указанным именем
            lsusb - Вывести список USB-устройств
            lspci - Вывести список PCI-устройств
            help - Показать эту справку
            sth <имя> - Сохранить в папку 'Документы' историю запросов терминала
            """);
    }
}
