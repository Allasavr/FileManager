import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ProcessInfo {

    public static String getThreadInfo(int pid) {
        StringBuilder threadInfo = new StringBuilder();
        File taskDir = new File("/proc/" + pid + "/task");

        if (!taskDir.exists() || !taskDir.isDirectory()) {
            return "Не удалось получить информацию о потоках.";
        }

        File[] threads = taskDir.listFiles();
        if (threads == null || threads.length == 0) {
            return "Потоки не найдены.";
        }

        for (File thread : threads) {
            try {
                int tid = Integer.parseInt(thread.getName());
                String state = readThreadState(pid, tid);
                int priority = readThreadPriority(pid, tid);

                threadInfo.append("TID: ").append(tid)
                        .append(", Состояние: ").append(state)
                        .append(", Приоритет: ").append(priority)
                        .append("\n");
            } catch (NumberFormatException e) {
                // Пропускаем невалидные записи
            }
        }

        return threadInfo.toString();
    }

    private static String readThreadState(int pid, int tid) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/" + pid + "/task/" + tid + "/status"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("State:")) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Неизвестно";
    }

    private static int readThreadPriority(int pid, int tid) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/" + pid + "/task/" + tid + "/stat"));
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(" ");
                if (parts.length > 17) {
                    return Integer.parseInt(parts[17]); // Поле приоритета в /proc/[pid]/task/[tid]/stat
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getProcessUptime(int pid) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/uptime"));
            String uptimeLine = reader.readLine();
            if (uptimeLine != null) {
                String[] parts = uptimeLine.split("\\s+");
                double systemUptime = Double.parseDouble(parts[0]);
                double processStartTime = getProcessStartTime(pid);
                return formatUptime(systemUptime - processStartTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Неизвестно";
    }

    private static double getProcessStartTime(int pid) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/" + pid + "/stat"));
            String statLine = reader.readLine();
            if (statLine != null) {
                String[] parts = statLine.split("\\s+");
                long startTimeTicks = Long.parseLong(parts[21]);
                long ticksPerSecond = 100; // Количество тиков в секунду
                return startTimeTicks / (double) ticksPerSecond;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String formatUptime(double uptimeSeconds) {
        int hours = (int) (uptimeSeconds / 3600);
        int minutes = (int) ((uptimeSeconds % 3600) / 60);
        int seconds = (int) (uptimeSeconds % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static int getDescriptorCount(int pid) {
        File descriptorDir = new File("/proc/" + pid + "/fd");
        if (descriptorDir.exists() && descriptorDir.isDirectory()) {
            return descriptorDir.list().length; // Подсчитываем количество файлов
        }
        return 0; // Если директория не существует, возвращаем 0
    }
}