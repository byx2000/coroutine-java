package byx.coroutine;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientTest {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        int cnt = 100;
        CountDownLatch latch = new CountDownLatch(cnt);

        for (int i = 0; i < cnt; i++) {
            executor.submit(() -> {
                long begin = System.currentTimeMillis();
                SocketChannel sc = SocketChannel.open();

                sc.connect(new InetSocketAddress("localhost", 8888));
                SocketAddress addr = sc.getLocalAddress();
                System.out.printf("[%s] [%s] connected to server\n", currentTime(), addr);

                sc.write(StandardCharsets.UTF_8.encode("hello"));
                System.out.printf("[%s] [%s] write to server: hello\n", currentTime(), addr);

                ByteBuffer buf = ByteBuffer.allocate(1024);
                sc.read(buf);
                buf.flip();
                System.out.printf("[%s] [%s] receive from server: %s\n", currentTime(), addr, StandardCharsets.UTF_8.decode(buf));

                sc.close();
                System.out.printf("[%s] [%s] connection closed, cost %ss\n", currentTime(), addr, (System.currentTimeMillis() - begin) / 1000.0);

                latch.countDown();
                return null;
            });
        }

        latch.await();
        executor.shutdown();
    }

    private static String currentTime() {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
