package byx.coroutine.dispatcher;

import byx.coroutine.core.Thunk;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static byx.coroutine.dispatcher.SystemCall.withContinuation;

public class Reactor {
    private static final Selector selector;
    private static final Map<SelectionKey, Continuation> continuationMap = new ConcurrentHashMap<>();

    static {
        try {
            selector = Selector.open();
            new Thread(() -> {
                try {
                    while (true) {
                        if (selector.select(100) == 0) {
                            continue;
                        }
                        Set<SelectionKey> keys = selector.selectedKeys();
                        Iterator<SelectionKey> iterator = keys.iterator();
                        while (iterator.hasNext()) {
                            SelectionKey key = iterator.next();
                            iterator.remove();
                            if (continuationMap.containsKey(key)) {
                                Continuation continuation = continuationMap.get(key);
                                continuationMap.remove(key);
                                if (key.isAcceptable()) {
                                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                                    continuation.resume(ssc.accept());
                                } else if (key.isReadable() || key.isWritable()) {
                                    SocketChannel sc = (SocketChannel) key.channel();
                                    continuation.resume(sc);
                                }
                            } else {
                                key.cancel();
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }, "thread-selector").start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Thunk<SocketChannel> waitAccept(SelectableChannel channel) {
        try {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(selector, SelectionKey.OP_ACCEPT);
            return withContinuation(c -> continuationMap.put(key, c));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Thunk<SocketChannel> waitRead(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
            return withContinuation(c -> continuationMap.put(key, c));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Thunk<SocketChannel> waitWrite(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(selector, SelectionKey.OP_WRITE);
            return withContinuation(c -> continuationMap.put(key, c));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
