package simple.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import simple.util.NettyUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RpcClient extends Bootstrap {

    /**
     * Tick count of each wheel instance for timer.
     */
    private static final int DEFAULT_TICKS_PER_WHEEL = 2048;
    /**
     * Tick duration for timer.
     */
    private static final int DEFAULT_TICK_DURATION = 100;
    /**
     * The timer.
     */
    private static Timer timer = createTimer(); // 初始化定时器

    private static final int CLIENT_EVENTLOOP_THREADS =
            Runtime.getRuntime().availableProcessors();

    private static EventLoopGroup workerGroup = createWorkerGroup(CLIENT_EVENTLOOP_THREADS);

    private RpcClientOptions rpcClientOptions;

    private final Map<Long, RpcClientCallState> requestMap = new ConcurrentHashMap<>();

    private AtomicLong correlationId = new AtomicLong(1);

    private String host;

    private int port;

    private String address;

    private static Timer createTimer() {
        Timer timer = new HashedWheelTimer(Executors.defaultThreadFactory(), DEFAULT_TICK_DURATION,
                TimeUnit.MILLISECONDS, DEFAULT_TICKS_PER_WHEEL);
        return timer;
    }

    public static Timer getTimer() {
        return timer;
    }

    private static EventLoopGroup createWorkerGroup(int eventLoopThreads) {
        if (NettyUtil.isLinuxPlatform()) {
            return new EpollEventLoopGroup(eventLoopThreads);
        } else {
            return new NioEventLoopGroup(eventLoopThreads);
        }
    }

    public RpcClient(RpcClientOptions rpcClientOptions, String host, int port) {
        this.address = host + ":" + port;
        this.remoteAddress(host, port);
        this.rpcClientOptions = rpcClientOptions;

        Class<? extends Channel> channelClass;
        if (workerGroup instanceof EpollEventLoopGroup) {
            channelClass = EpollSocketChannel.class;
        } else {
            channelClass = NioSocketChannel.class;
        }

        this.group(workerGroup);
        this.channel(channelClass);
        this.handler(new RpcClientChannelInitializer(this));
        this.option(ChannelOption.SO_REUSEADDR, rpcClientOptions.isReuseAddress());
        this.option(ChannelOption.SO_SNDBUF, rpcClientOptions.getSendBufferSize());
        this.option(ChannelOption.SO_RCVBUF, rpcClientOptions.getReceiveBufferSize());
        this.option(ChannelOption.SO_KEEPALIVE, rpcClientOptions.isKeepAlive());
        this.option(ChannelOption.TCP_NODELAY, rpcClientOptions.isTcpNoDelay());
        this.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, rpcClientOptions.getConnectTimeout());
    }

    public RpcClientOptions getRpcClientOptions() {
        return rpcClientOptions;
    }

    public RpcClientCallState removePendingRequest(long seqId) {
        return requestMap.remove(seqId);
    }

    public void registerPendingRequest(long seqId, RpcClientCallState state) {
        if (requestMap.containsKey(seqId)) {
            throw new IllegalArgumentException("State already registered");
        }
        requestMap.put(seqId, state);
    }

    public long getNextCorrelationId() {
        return correlationId.getAndIncrement();
    }

    public String getAddress() {
        return this.address;
    }
}
