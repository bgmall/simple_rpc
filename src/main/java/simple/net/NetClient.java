package simple.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import simple.net.callback.ClientCallState;
import simple.net.callback.MessageCallback;
import simple.net.exception.ConnectionException;
import simple.net.exception.SendTimeoutException;
import simple.net.protocol.message.CallbackMessage;
import simple.net.protocol.message.MessageDecoder;
import simple.net.protocol.message.MessageEncoder;
import simple.net.protocol.message.NetMessage;
import simple.util.NettyUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NetClient {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NetClient.class);

    private static final String CHANNEL_STATE_AWARE_HANDLER = "channel_state_aware_handler";
    private static final String CHANNEL_STATE_HANDLER = "channle_state_handler";
    private static final String MESSAGE_DECODER = "message_decoder";
    private static final String MESSAGE_ENCODER = "message_encoder";
    private static final String MESSAGE_HEARTBEAT = "message_heartbeat";
    private static final String MESSAGE_CALLBACK_HANDLER = "message_callback_handler";
    private static final String MESSAGE_HANDLER = "message_handler";

    /**
     * Tick count of each wheel instance for timer.
     */
    private static final int DEFAULT_TICKS_PER_WHEEL = 2048;
    /**
     * Tick duration for timer.
     */
    private static final int DEFAULT_TICK_DURATION = 100;

    private Bootstrap bootstrap;

    private NetClientOptions clientOptions;

    private String remoteAddress;

    private String host;

    private int port;

    private NetChannelStateHandler stateHandler;

    private NetMessageHandler messageHandler;

    private ChannelPool channelPool;

    private AtomicLong correlationId = new AtomicLong(1);

    private final ConcurrentMap<Long, ClientCallState> requestMap = new ConcurrentHashMap<>();

    private static EventLoopGroup workerGroup;

    private static Timer timer;

    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger();

    private static Timer createTimer() {
        Timer timer = new HashedWheelTimer(Executors.defaultThreadFactory(), DEFAULT_TICK_DURATION,
                TimeUnit.MILLISECONDS, DEFAULT_TICKS_PER_WHEEL);
        return timer;
    }

    public Timer getTimer() {
        return timer;
    }

    private static EventLoopGroup createWorkerGroup(int eventLoopThreads) {
        if (NettyUtil.isLinuxPlatform()) {
            return new EpollEventLoopGroup(eventLoopThreads, new DefaultThreadFactory("NetClientWorkerIoThread"));
        } else {
            return new NioEventLoopGroup(eventLoopThreads, new DefaultThreadFactory("NetClientWorkerIoThread"));
        }
    }

    public NetClient(NetClientOptions clientOptions, String host, int port) {
        this.host = host;
        this.port = port;
        this.remoteAddress = host + ":" + port;
        this.clientOptions = clientOptions;
    }

    public void start() {
        // first client
        if (INSTANCE_COUNT.incrementAndGet() == 1) {
            if (timer == null) {
                timer = createTimer();
            }
            if (workerGroup == null) {
                workerGroup = createWorkerGroup(clientOptions.getEventLoopThreads());
            }
        }

        Class<? extends Channel> channelClass;
        if (workerGroup instanceof EpollEventLoopGroup) {
            channelClass = EpollSocketChannel.class;
        } else {
            channelClass = NioSocketChannel.class;
        }

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(channelClass);
        bootstrap.handler(createNetClientChannelInitializer());
        bootstrap.option(ChannelOption.SO_REUSEADDR, clientOptions.isReuseAddress());
        bootstrap.option(ChannelOption.SO_SNDBUF, clientOptions.getSendBufferSize());
        bootstrap.option(ChannelOption.SO_RCVBUF, clientOptions.getReceiveBufferSize());
        bootstrap.option(ChannelOption.SO_KEEPALIVE, clientOptions.isKeepAlive());
        bootstrap.option(ChannelOption.TCP_NODELAY, clientOptions.isTcpNoDelay());
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOptions.getConnectTimeout());

        if (channelPool == null) {
            channelPool = new SingleChannelPool(this);
        }
    }

    public void shutdown() {
        if (channelPool != null) {
            channelPool.close();
            channelPool = null;
        }

        // to check instance count
        if (INSTANCE_COUNT.decrementAndGet() == 0) {
            if (timer != null) {
                timer.stop();
                timer = null;
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                workerGroup = null;
            }
        }
    }

    ChannelFuture connect() {
        if (bootstrap == null) {
            throw new IllegalStateException("client didn't start");
        }

        bootstrap.remoteAddress(host, port);
        return bootstrap.connect();
    }

    public long getNextCorrelationId() {
        return correlationId.getAndIncrement();
    }

    public ClientCallState removePendingRequest(long seqId) {
        return requestMap.remove(seqId);
    }

    public void registerPendingRequest(long seqId, ClientCallState state) {
        if (requestMap.containsKey(seqId)) {
            throw new IllegalArgumentException("state already registered");
        }
        requestMap.put(seqId, state);
    }

    public NetClientOptions getClientOptions() {
        return clientOptions;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setStateHandler(NetChannelStateHandler stateHandler) {
        this.stateHandler = stateHandler;
    }

    public void setMessageHandler(NetMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public ChannelPool getChannelPool() {
        return channelPool;
    }

    public void setChannelPool(ChannelPool channelPool) {
        this.channelPool = channelPool;
    }

    private ChannelInitializer<Channel> createNetClientChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ChannelPipeline channelPipe = ch.pipeline();

                channelPipe.addLast(MESSAGE_ENCODER, new MessageEncoder());

                int idleTimeoutSeconds = getClientOptions().getIdleTimeoutSeconds();
                channelPipe.addLast(CHANNEL_STATE_AWARE_HANDLER, new IdleStateHandler(idleTimeoutSeconds, 0, 0));
                channelPipe.addLast(CHANNEL_STATE_HANDLER, stateHandler);
                int maxFrameLength = getClientOptions().getMaxFrameLength();
                channelPipe.addLast(MESSAGE_DECODER, new MessageDecoder(maxFrameLength));
                int heartBeatIntervalMills = getClientOptions().getHeartBeatIntervalMills();
                channelPipe.addLast(MESSAGE_HEARTBEAT, new HeartBeatMessageClientHandler(heartBeatIntervalMills));
                channelPipe.addLast(MESSAGE_CALLBACK_HANDLER, new NetClientCallbackHandler(NetClient.this));
                channelPipe.addLast(MESSAGE_HANDLER, messageHandler);
            }
        };
    }

    public void sendMessage(NetMessage message) {
        NetConnection connection = channelPool.choose(message);
        connection.writeAndFlush(message);
    }

    public void sendMessage(NetMessage message, MessageCallback callback) {
        if (!(message instanceof CallbackMessage)) {
            if (callback != null) {
                callback.exceptionCaught(new IllegalArgumentException("message must be callbackMessage"));
            }
            return;
        }
        NetConnection connection = channelPool.choose(message);
        if (connection.invalidChannel()) {
            if (callback != null) {
                callback.exceptionCaught(createConnectionException());
            }
            return;
        }

        CallbackMessage callbackMessage = (CallbackMessage) message;
        long nextCorrelationId = getNextCorrelationId();
        ClientCallState clientCallState = new ClientCallState();
        clientCallState.setCallback(callback);
        clientCallState.setCallbackId(nextCorrelationId);
        if (callbackMessage.getTimeoutMills() > 0) {
            Timeout timeout = createTimeout(clientCallState, callbackMessage.getTimeoutMills(), message.getMsgId());
            clientCallState.setTimeout(timeout);
        }
        registerPendingRequest(nextCorrelationId, clientCallState);
        connection.writeAndFlush(message);
    }

    private Timeout createTimeout(ClientCallState clientCallState, long timeoutMills, int msgId) {
        return getTimer().newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                ClientCallState callState = removePendingRequest(clientCallState.getCallbackId());
                if (callState != null) {
                    logger.error("send msg[{}] to remote address[{}] timeout", msgId, getRemoteAddress());
                    clientCallState.handleException(createSendTimeoutException(msgId));
                }
            }
        }, timeoutMills, TimeUnit.MILLISECONDS);
    }

    private SendTimeoutException createSendTimeoutException(int msgId) {
        return new SendTimeoutException("send msg[" + msgId + "] to remote address[" + getRemoteAddress() + "] timeout");
    }

    private ConnectionException createConnectionException() {
        return new ConnectionException("channel to remote address[" + getRemoteAddress() + "] is closed");
    }
}
