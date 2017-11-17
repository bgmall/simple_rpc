package simple.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import simple.util.NettyUtil;

public class NetClient extends Bootstrap {

    private static final int CLIENT_EVENTLOOP_THREADS =
            Runtime.getRuntime().availableProcessors();

    private static EventLoopGroup workerGroup = createWorkerGroup(CLIENT_EVENTLOOP_THREADS);

    private NetClientOptions clientOptions;

    private String address;

    private static EventLoopGroup createWorkerGroup(int eventLoopThreads) {
        if (NettyUtil.isLinuxPlatform()) {
            return new EpollEventLoopGroup(eventLoopThreads);
        } else {
            return new NioEventLoopGroup(eventLoopThreads);
        }
    }

    public NetClient(NetClientOptions clientOptions, String host, int port) {
        this.address = host + ":" + port;
        this.remoteAddress(host, port);
        this.clientOptions = clientOptions;

        Class<? extends Channel> channelClass;
        if (workerGroup instanceof EpollEventLoopGroup) {
            channelClass = EpollSocketChannel.class;
        } else {
            channelClass = NioSocketChannel.class;
        }

        this.group(workerGroup);
        this.channel(channelClass);
        this.handler(new NetClientChannelInitializer(this));
        this.option(ChannelOption.SO_REUSEADDR, clientOptions.isReuseAddress());
        this.option(ChannelOption.SO_SNDBUF, clientOptions.getSendBufferSize());
        this.option(ChannelOption.SO_RCVBUF, clientOptions.getReceiveBufferSize());
        this.option(ChannelOption.SO_KEEPALIVE, clientOptions.isKeepAlive());
        this.option(ChannelOption.TCP_NODELAY, clientOptions.isTcpNoDelay());
        this.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOptions.getConnectTimeout());
    }

    public NetClientOptions getClientOptions() {
        return clientOptions;
    }
}
