package byx.coroutine;

import byx.coroutine.core.Coroutine;
import byx.coroutine.dispatcher.Dispatcher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static byx.coroutine.core.Thunks.exec;
import static byx.coroutine.core.Thunks.loop;
import static byx.coroutine.dispatcher.Reactor.*;
import static byx.coroutine.dispatcher.SystemCall.createTask;

@Disabled
public class SelectorTest {
    @Test
    public void test() throws Exception {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(server());
        dispatcher.run();
    }

    private Coroutine server() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("localhost", 8888));
        return loop(() -> true, exec(() -> waitAccept(ssc))
            .then(sc -> System.out.printf("[client %s] accepted\n", getRemotePort(sc)))
            .flatMap(sc -> createTask(handleClient(sc))))
            .toCoroutine();
    }

    private Coroutine handleClient(SocketChannel sc) {
        int port = getRemotePort(sc);
        return exec(() -> waitRead(sc))
            .then(() -> {
                try {
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    sc.read(buf);
                    buf.flip();
                    System.out.printf("[client %s] read from client: %s\n", port, StandardCharsets.UTF_8.decode(buf));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .then(() -> waitWrite(sc))
            .then(() -> {
                try {
                    System.out.printf("[client %s] write to client: hi\n", port);
                    sc.write(StandardCharsets.UTF_8.encode("hi"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .toCoroutine();
    }

    @Test
    public void client() throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        sc.write(StandardCharsets.UTF_8.encode("hello"));
        ByteBuffer buf = ByteBuffer.allocate(1024);
        System.out.println("waiting read from server...");
        sc.read(buf);
        buf.flip();
        System.out.println("read from server: " + StandardCharsets.UTF_8.decode(buf));
        sc.close();
    }

    @Test
    public void manyClients() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Thread t = new Thread(() -> {
                try {
                    long begin = System.currentTimeMillis();
                    SocketChannel sc = SocketChannel.open();
                    sc.connect(new InetSocketAddress("localhost", 8888));
                    int port = getLocalPort(sc);
                    System.out.printf("[client %s] connect to server\n", port);
                    sc.write(StandardCharsets.UTF_8.encode("hello"));
                    System.out.printf("[client %s] write data to server: hello\n", port);
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    sc.read(buf);
                    buf.flip();
                    System.out.printf("[client %s] read data from server: %s\n", port, StandardCharsets.UTF_8.decode(buf));
                    sc.close();
                    System.out.printf("[client %s] connect closed\n", port);
                    System.out.printf("[client %s] duration: %ss\n", port, (System.currentTimeMillis() - begin) / 1000.0);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
            threads.add(t);
        }

        for (Thread t : threads) {
            t.join();
        }
    }

    private int getLocalPort(SocketChannel sc) {
        try {
            InetSocketAddress address = (InetSocketAddress) sc.getLocalAddress();
            return address.getPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getRemotePort(SocketChannel sc) {
        try {
            InetSocketAddress address = (InetSocketAddress) sc.getRemoteAddress();
            return address.getPort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
