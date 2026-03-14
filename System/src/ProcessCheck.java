import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ProcessCheck {
    private static final List<TrackedProcess> trackedProcesses = new ArrayList<>();

    public static void trackProcess(){
        ProcessHandle.allProcesses().filter(ProcessCheck::isLaunchByApp).forEach(process ->
        {
            String processName = process.info().command().orElse("Unknown");
            Instant startTime = process.info().startInstant().orElse(Instant.now());

            if(!isAlreadyTracked(processName)){
                trackedProcesses.add(new TrackedProcess(processName, startTime));
                System.out.println("Процесс добавлен: " + processName + " (Старт: " + startTime + ")");
            }
        });
    }

    private static boolean isAlreadyTracked(String processName) {
        return trackedProcesses.stream()
                .anyMatch(tracked -> tracked.getName().equals(processName));
    }

    private static boolean isLaunchByApp(ProcessHandle processHandle) {
        return processHandle.parent()
                .map(parent -> parent.pid() == ProcessHandle.current().pid())
                .orElse(false);

    }

    public static List<TrackedProcess> getTrackedProcesses() {
        return trackedProcesses;
    }
}
