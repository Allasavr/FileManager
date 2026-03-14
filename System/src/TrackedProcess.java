import java.time.Instant;

public class TrackedProcess {
    private String name;
    private Instant start;

    public TrackedProcess(String name, Instant startTime){
        this.name=name;
        this.start=startTime;
    }

    public String getName() {
        return name;
    }

    public Instant getStartTime() {
        return start;
    }

    @Override
    public String toString() {
        return "Имя процесса: " + name + ", Время старта: " + start;
    }
}
