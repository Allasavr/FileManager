import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MessageQueue {
    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    /**
     * Отправляет сообщение в очередь.
     *
     * @param message Сообщение для отправки.
     */
    public static void send(String message) {
        try {
            queue.put(message); // Добавляем сообщение в очередь
            System.out.println("Сообщение отправлено: " + message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    /**
     * Получает сообщение из очереди.
     *
     * @return Полученное сообщение.
     */
    public static String receive() {
        try {
            String message = queue.take(); // Извлекаем сообщение из очереди
            System.out.println("Сообщение получено: " + message);
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Ошибка при получении сообщения: " + e.getMessage());
            return null;
        }
    }

    /**
     * Очищает очередь сообщений.
     */
    public static void clear() {
        queue.clear();
        System.out.println("Очередь очищена.");
    }
}
