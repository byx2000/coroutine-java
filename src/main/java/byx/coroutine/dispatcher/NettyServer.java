package byx.coroutine.dispatcher;

import byx.coroutine.core.Thunk;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Map;
import java.util.concurrent.*;

import static byx.coroutine.core.Thunks.exec;
import static byx.coroutine.dispatcher.SystemCall.withContinuation;

public class NettyServer {
    private final int port;
    private final ExecutorService acceptExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService readExecutor = Executors.newSingleThreadExecutor();
    private final BlockingQueue<Client> acceptClients = new LinkedBlockingQueue<>();
    private final Map<String, BlockingQueue<String>> readEventMap = new ConcurrentHashMap<>();

    public NettyServer(int port) {
        this.port = port;
        startServer();
    }

    private void startServer() {
        new Thread(() -> {
            new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel sc) {
                        sc.pipeline().addLast(new StringDecoder(), new StringEncoder());
                        sc.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                acceptClients.add(buildClient(ctx));
                                readEventMap.put(ctx.channel().id().asLongText(), new LinkedBlockingQueue<>());
                                super.channelActive(ctx);
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                readEventMap.remove(ctx.channel().id().asLongText());
                                super.channelInactive(ctx);
                            }

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                readEventMap.get(ctx.channel().id().asLongText()).add(msg);
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                cause.printStackTrace();
                                ctx.close();
                            }
                        });
                    }
                }).bind(port);
        }).start();
    }

    private Client buildClient(ChannelHandlerContext ctx) {
        return new Client(ctx.channel());
    }

    public Thunk<Client> accept() {
        return withContinuation(c -> {
            acceptExecutor.submit(() -> {
                try {
                    c.resume(acceptClients.take());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private Thunk<String> read(Client client) {
        return withContinuation(c -> {
            readExecutor.submit(() -> {
                try {
                    c.resume(readEventMap.get(client.getId()).take());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    private Thunk<Void> write(Client client, String msg) {
        return exec(() -> client.getChannel().writeAndFlush(msg));
    }

    public class Client {
        private final Channel channel;

        public Client(Channel channel) {
            this.channel = channel;
        }

        public Channel getChannel() {
            return channel;
        }

        public String getId() {
            return channel.id().asLongText();
        }

        public Thunk<String> read() {
            return NettyServer.this.read(this);
        }

        public Thunk<Void> write(String msg) {
            return NettyServer.this.write(this, msg);
        }
    }
}
