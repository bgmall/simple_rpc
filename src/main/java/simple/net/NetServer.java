package simple.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.rpc.RpcServerOptions;
import simple.util.NettyUtil;

import java.net.InetSocketAddress;

public class NetServer extends ServerBootstrap {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetServer.class);

    private RpcServerOptions serverOptions;
    /**
     * The boss group.
     */
    private EventLoopGroup bossGroup;
    /**
     * The worker group.
     */
    private EventLoopGroup workerGroup;
    /**
     * The channel.
     */
    private Channel channel;

    public NetServer(RpcServerOptions serverOptions) {
        this.serverOptions = serverOptions;

        Class<? extends ServerChannel> serverChannel;
        if (NettyUtil.isLinuxPlatform()) {
            this.bossGroup = new EpollEventLoopGroup(serverOptions.getAcceptorThreads());
            this.workerGroup = new EpollEventLoopGroup(serverOptions.getWorkThreads());
            serverChannel = EpollServerSocketChannel.class;
        } else {
            this.bossGroup = new NioEventLoopGroup(serverOptions.getAcceptorThreads());
            this.workerGroup = new NioEventLoopGroup(serverOptions.getWorkThreads());
            serverChannel = NioServerSocketChannel.class;
        }

        this.group(this.bossGroup, this.workerGroup);
        this.channel(serverChannel);
        this.option(ChannelOption.SO_BACKLOG, serverOptions.getBacklog());

        this.childOption(ChannelOption.SO_KEEPALIVE, serverOptions.isKeepAlive());
        this.childOption(ChannelOption.SO_REUSEADDR, true);
        this.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        this.childOption(ChannelOption.TCP_NODELAY, serverOptions.isTcpNoDelay());
        this.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, serverOptions.getConnectTimeout());
        this.childOption(ChannelOption.SO_RCVBUF, serverOptions.getReceiveBufferSize());
        this.childOption(ChannelOption.SO_SNDBUF, serverOptions.getSendBufferSize());
        this.childHandler(new NetServerChannelInitializer(this));
    }

    public void start(int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        try {
            ChannelFuture channelFuture = this.bind(inetSocketAddress).sync();
            if (channelFuture.isSuccess()) {
                this.channel = channelFuture.channel();
                logger.info("server address[{}] started", channel);
            }
        } catch (Throwable e) {
            shutdown();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void startAsync(int port) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);
        this.bind(inetSocketAddress).addListener(new ChannelFutureListener() {

            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    channel = future.channel();
                    logger.info("server address[{}] started", channel);
                } else {
                    shutdown();
                    throw new Exception("bind port failed:" + inetSocketAddress.toString() + " message:" + future.toString());
                }
            }
        });
    }

    public void shutdown() {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }

        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public RpcServerOptions getServerOptions() {
        return serverOptions;
    }
}
