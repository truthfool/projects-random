import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        ExecutorService executor = new ThreadPoolExecutor(

                4, 8, 30L, TimeUnit.SECONDS,

                new LinkedBlockingQueue<>(10));
    }
}