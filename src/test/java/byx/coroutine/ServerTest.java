package byx.coroutine;

import byx.coroutine.core.Coroutine;
import byx.coroutine.dispatcher.Dispatcher;
import byx.coroutine.dispatcher.NettyServer;

import java.text.SimpleDateFormat;
import java.util.Date;

import static byx.coroutine.core.Thunks.exec;
import static byx.coroutine.dispatcher.SystemCall.createTask;

public class ServerTest {
    public static void main(String[] args) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.addTask(serverCo());
        dispatcher.run();
    }

    private static Coroutine serverCo() {
        NettyServer server = new NettyServer(8888);
        return exec(server::accept)
            .then(client -> System.out.printf("[%s] [%s] accept\n", currentTime(), client.getChannel().remoteAddress()))
            .flatMap(client -> createTask(handleClient(client)))
            .loopForever()
            .toCoroutine();
    }

    private static Coroutine handleClient(NettyServer.Client client) {
        return exec(client::read)
            .then(msg -> System.out.printf("[%s] [%s] read data: %s\n", currentTime(), client.getChannel().remoteAddress(), msg))
            .flatMap(msg -> client.write(msg + " reply"))
            .then(() -> System.out.printf("[%s] [%s] write data\n", currentTime(), client.getChannel().remoteAddress()))
            .toCoroutine();
    }

    private static String currentTime() {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
